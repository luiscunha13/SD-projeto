package database;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Users_Database {
    private Map<String, byte[]> users_database = new HashMap<>(); // key-data
    Lock l = new ReentrantLock();

    void put(String key, byte[] value) {
        l.lock();

        try {
            users_database.put(key, value);
        } finally {
            l.unlock();
        }
    }

    byte[] get(String key) {
        byte[] answer = null;
        l.lock();

        try {
            if (users_database.containsKey(key))
                answer = users_database.get(key);
        } finally {
            l.unlock();
        }

        return answer;
    }

    void multiPut(Map<String, byte[]> pairs) {
        l.lock();

        try {
            users_database.putAll(pairs);
        } finally {
            l.unlock();
        }
    }

    Map<String, byte[]> multiGet(Set<String> keys) {
        Map<String, byte[]> m = new HashMap<>();

        try {
            for (String s : keys)
                if (users_database.containsKey(s))
                    m.put(s, users_database.get(s));
                else
                    m.put(s, null);
        } finally {
            l.unlock();
        }

        return m;
    }


}
