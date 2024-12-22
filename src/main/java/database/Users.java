package database;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Users {
    private Map<String, String> users = new HashMap<>(); // username-password
    Lock l = new ReentrantLock();

    public boolean register(String username, String password) {
        boolean out;
        l.lock();
        try {
            if (users.containsKey(username)) {
                out = false;
            } else {
                users.put(username, password);
                out = true;
            }
        } finally {
            l.unlock();
        }
        return out;
    }

    public boolean login(String username, String password) {
        boolean out;
        l.lock();
        try {
            if (!users.containsKey(username)) {
                out = false;
            } else {
                out = users.get(username).equals(password);
            }
        } finally {
            l.unlock();
        }
        return out;
    }

}
