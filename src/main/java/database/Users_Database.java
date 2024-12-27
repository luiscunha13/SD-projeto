package database;

import java.util.*;
import java.util.concurrent.locks.*;

public class Users_Database {
    private Map<String, byte[]> users_database = new HashMap<>();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();
    private final Map<String, LinkedList<WaitingCondition>> waitingConditions = new HashMap<>();

    private static class WaitingCondition {
        final byte[] expectedValue;
        final Condition waitingForValue;
        int waitingThreads;

        public WaitingCondition(byte[] expectedValue, Condition condition) {
            this.expectedValue = expectedValue;
            this.waitingForValue = condition;
            this.waitingThreads = 1;
        }

        public boolean match(byte[] value) {
            return Arrays.equals(this.expectedValue, value);
        }
    }

    public void put(String key, byte[] value) {
        writeLock.lock();
        try {
            users_database.put(key, value);
            signalWaitingThreads(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    public byte[] get(String key) {
        readLock.lock();
        try {
            return users_database.get(key);
        } finally {
            readLock.unlock();
        }
    }

    public void multiPut(Map<String, byte[]> pairs) {
        writeLock.lock();
        try {
            users_database.putAll(pairs);
            // Sinalizar para cada par
            for (Map.Entry<String, byte[]> entry : pairs.entrySet()) {
                signalWaitingThreads(entry.getKey(), entry.getValue());
            }
        } finally {
            writeLock.unlock();
        }
    }

    public Map<String, byte[]> multiGet(Set<String> keys) {
        Map<String, byte[]> result = new HashMap<>();
        readLock.lock();
        try {
            for (String key : keys) {
                byte[] value = users_database.get(key);
                result.put(key, value);
            }
            return result;
        } finally {
            readLock.unlock();
        }
    }

    public byte[] getWhen(String key, String keyCond, byte[] valueCond) {
        WaitingCondition cond = null;
        writeLock.lock();
        try {
            if (verifyCondition(keyCond, valueCond)) {
                return users_database.get(key);
            }

            LinkedList<WaitingCondition> conditions = waitingConditions.computeIfAbsent(
                    keyCond,
                    k -> new LinkedList<>()
            );

             for (WaitingCondition existing : conditions) {
                if (existing.match(valueCond)) {
                    cond = existing;
                    existing.waitingThreads++;
                    break;
                }
            }

            if (cond == null) {
                cond = new WaitingCondition(valueCond, writeLock.newCondition());
                conditions.add(cond);
            }

            try {
                while (!verifyCondition(keyCond, valueCond)) {
                    cond.waitingForValue.await();
                }

                byte[] result = users_database.get(key);

                cond.waitingThreads--;
                if (cond.waitingThreads == 0) {
                    conditions.remove(cond);
                    if (conditions.isEmpty()) {
                        waitingConditions.remove(keyCond);
                    }
                }
                else if (cond.waitingThreads > 0){
                    cond.waitingForValue.signal();
                }

                return result;
            }
            catch (InterruptedException e) {
                cond.waitingThreads--;
                if (cond.waitingThreads == 0) {
                    conditions.remove(cond);
                    if (conditions.isEmpty()) {
                        waitingConditions.remove(keyCond);
                    }
                }
                Thread.currentThread().interrupt();
                return null;
            }
        } finally {
            writeLock.unlock();
        }
    }

    private void signalWaitingThreads(String key, byte[] value) {
        LinkedList<WaitingCondition> conditions = waitingConditions.get(key);
        if (conditions != null) {
            for (WaitingCondition wc : conditions) {
                if (wc.match(value)) {
                    wc.waitingForValue.signal();
                }
            }
        }
    }

    private boolean verifyCondition(String keyCond, byte[] valueCond) {
        return Arrays.equals(users_database.get(keyCond), valueCond);
    }
}