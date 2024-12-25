package database;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Users_Database {
    private Map<String, byte[]> users_database = new HashMap<>(); // key-data
    Lock rl = new ReentrantLock();
    Lock wl = new ReentrantLock();
    Condition c = rl.newCondition();
    private Map<String, LinkedList<WaitingCondition>> waitingConditions = new HashMap<>();

    private static class WaitingCondition {
        byte[] expectedValue;
        Condition condition;
        int waitingThreads;

        public WaitingCondition(byte[] expectedValue, Condition condition) {
            this.expectedValue = expectedValue;
            this.condition = condition;
            this.waitingThreads = 1;
        }

        public boolean match(byte[] value) {
            return Arrays.equals(this.expectedValue, value);
        }
    }

    public void put(String key, byte[] value) {
        wl.lock();
        try {
            users_database.put(key, value);

            LinkedList<WaitingCondition> conditions = waitingConditions.get(key);
            if (conditions != null) {
                for (WaitingCondition wc : conditions) {
                    if (wc.match(value)) {
                        wc.condition.signal();
                    }
                }
            }
        } finally {
            wl.unlock();
        }
    }

    public byte[] get(String key) {
        byte[] answer = null;
        rl.lock();
        try {
            if (users_database.containsKey(key))
                answer = users_database.get(key);
        } finally {
            rl.unlock();
        }

        return answer;
    }

    public void multiPut(Map<String, byte[]> pairs) {
        wl.lock();
        try {
            users_database.putAll(pairs);

            for(Map.Entry<String, byte[]> entry : pairs.entrySet()){
                LinkedList<WaitingCondition> conditions = waitingConditions.get(entry.getKey());
                if (conditions != null) {
                    for (WaitingCondition wc : conditions) {
                        if (wc.match(entry.getValue())) {
                            wc.condition.signal();
                        }
                    }
                }
            }
            users_database.putAll(pairs);
        } finally {
            wl.unlock();
        }
    }

    public Map<String, byte[]> multiGet(Set<String> keys) {
        Map<String, byte[]> m = new HashMap<>();
        String no = "null";
        rl.lock();
        try {
            for (String s : keys)
                if (users_database.containsKey(s))
                    m.put(s, users_database.get(s));
                else
                    m.put(s, no.getBytes());
        } finally {
            rl.unlock();
        }

        return m;
    }

    public byte[] getWhen(String key, String keyCond, byte[] valueCond){
        WaitingCondition cond = null;

        rl.lock();
        try{
            if(verifyCondition(keyCond, valueCond)) // aqui vê logo se por acaso a condição se verifica
                return users_database.get(key);
        }finally {
            rl.unlock();
        }

        wl.lock();
        try{ // criar uma lista de conditions (se ainda não existir) para meter no map de conditions
            LinkedList<WaitingCondition> conditions = waitingConditions.computeIfAbsent(keyCond,l -> new LinkedList<>());
            for(WaitingCondition c : conditions){  // ver se já há uma condição igual
                if(c.match(valueCond)){
                    cond = c;
                    c.waitingThreads++;
                }
            }

            if(cond==null){ // caso não haja nenhuma condição igual, criar uma e meter na lista
                cond = new WaitingCondition(valueCond,wl.newCondition());
                conditions.add(cond);
            }

            while(!verifyCondition(keyCond,valueCond)) //await
                cond.condition.await();
            
            byte[] out;
            rl.lock();
            try{
                out = users_database.get(key);
            }finally {
                rl.unlock();
            }

            cond.waitingThreads--;

            if(cond.waitingThreads==0){  // remover as cenas se já não forem precisas
                conditions.remove(cond);
                if(conditions.isEmpty())
                    waitingConditions.remove(keyCond);
            }

            return out;

        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } finally {
            wl.unlock();
        }
    }

    private boolean verifyCondition(String keyCond, byte[] valueCond) {
        byte[] currentValue = users_database.get(keyCond);
        return currentValue != null && Arrays.equals(currentValue, valueCond);
    }

}
