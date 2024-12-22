package database;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Users_Database {
    private Map<String, byte[]> users_database = new HashMap<>(); // key-data
    Lock rl = new ReentrantLock();
    Lock wl = new ReentrantLock();

    public void put(String key, byte[] value) {
        wl.lock();

        try {
            users_database.put(key, value);
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

}
