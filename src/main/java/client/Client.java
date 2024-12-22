package client;

import Connection.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Client {
    private static Socket socket = null;
    private static final String HOST = "localhost";
    private static final int PORT = 6666;
    private static DataOutputStream out;
    private static DataInputStream in;
    private Lock l = new ReentrantLock();
    private ConcurrentHashMap<Integer,Object> replies = new ConcurrentHashMap<>();
    private Lock lockId = new ReentrantLock();
    private int idRequest = 0;

    private int getAndIncrement() {
        lockId.lock();
        try{
            idRequest++;
        } finally {
            lockId.unlock();
        }
        return idRequest;
    }

    private void run() throws IOException {
        while(true){
            Frame res = Frame.receive(in);
            replies.put(res.getId(), res.getData());
        }
    }

    public boolean login(String username, String password) throws IOException {
        User u = new User(username,password);
        Frame f = new Frame(getAndIncrement(), FrameType.Login,false,u);
        l.lock();
        try{
            f.send(out);
        }
        finally {
            l.unlock();
        }

        return (Boolean) replies.get(f.getId());
    }

    public boolean register(String username, String password) throws IOException {
        User u = new User(username,password);
        Frame f = new Frame(getAndIncrement(), FrameType.Register,false,u);
        l.lock();
        try {
            f.send(out);
        } finally {
            l.unlock();
        }
        return (Boolean) replies.get(f.getId());
    }

    public void put(String key, byte[] value) throws IOException {
        PutOne p = new PutOne(key, value);
        Frame f = new Frame(getAndIncrement(), FrameType.Put,false,p);
        l.lock();
        try {
            f.send(out);
        } finally {
            l.unlock();
        }
    }

    public byte[] get(String key) throws IOException {
        Frame f = new Frame(getAndIncrement(), FrameType.Get,false,key);
        l.lock();
        try {
            f.send(out);
        } finally {
            l.unlock();
        }
        return (byte[]) replies.get(f.getId());
    }

    public void multiPut(Map<String,byte[]> pairs) throws IOException{
        Frame f = new Frame(getAndIncrement(), FrameType.MultiPut,false,pairs);
        l.lock();
        try {
            f.send(out);
        } finally {
            l.unlock();
        }
    }

    public Map<String, byte[]> multiGet(Set<String> keys) throws IOException{
        Frame f = new Frame(getAndIncrement(), FrameType.MultiGet,false,keys);
        l.lock();
        try {
            f.send(out);
        } finally {
            l.unlock();
        }
        return (Map<String, byte[]>) replies.get(f.getId());
    }

    /*
    public byte[] getWhen(String key, String keyCond, byte[] valueCond){

    }
    */

    public void exit() throws IOException {
        Frame f = new Frame(getAndIncrement(), FrameType.Close,false,null);
        l.lock();
        try {
            f.send(out);
        } finally {
            l.unlock();
        }
        socket.close();
    }


    public static void main(String[] args) throws IOException {
        try{
            socket = new Socket(HOST,PORT);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            Client_Interface ci = new Client_Interface();

        }catch(Exception e){
            e.printStackTrace();
        }
    }


}


