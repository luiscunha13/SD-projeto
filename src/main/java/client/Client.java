package client;

import Connection.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Client {
    private static Socket socket = null;
    private static final String HOST = "localhost";
    private static final int PORT = 6666;
    private static DataOutputStream out;
    private static DataInputStream in;
    private Lock ls = new ReentrantLock();
    private Lock lr = new ReentrantLock();
    private ConcurrentHashMap<Integer,Object> replies = new ConcurrentHashMap<>();
    private Condition replyCondition = ls.newCondition();
    private Lock lockId = new ReentrantLock();
    private int idRequest = 0;

    ExecutorService threadPool = Executors.newFixedThreadPool(5);


    public Client() throws IOException{

    }

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
        System.out.println("### Run method started ###");
        try {
            while (true) {
                System.out.println("[Thread " + Thread.currentThread().getId() + "] Waiting to deserialize frame...");
                lr.lock();
                try {
                    Frame res = Frame.deserialize(in);
                    System.out.println("[Thread " + Thread.currentThread().getId() +
                            "] Received frame: " + res.getId() +
                            " Type: " + res.getType() +
                            " Data: " + res.getData());

                    ls.lock();
                    try {
                        replies.put(res.getId(), res.getData());
                        System.out.println("Added response to replies map. Signaling waiting threads.");
                        replyCondition.signalAll();
                    } finally {
                        ls.unlock();
                    }
                } finally {
                    lr.unlock();
                }
            }
        } catch (IOException e) {
            System.out.println("### Run method error: " + e.getMessage() + " ###");
            e.printStackTrace();
        }
    }

    private Object awaitReply(int requestId) throws InterruptedException {
        ls.lock();
        try {
            while (!replies.containsKey(requestId)) {
                System.out.println("Waiting for reply to request ID: " + requestId);
                replyCondition.await();
            }
            System.out.println("Received reply for request ID: " + requestId);
            return replies.get(requestId);
        } finally {
            ls.unlock();
        }
    }

    private void executeThreadPool(Frame f, DataOutputStream out){
        ls.lock();
        try{
            threadPool.execute(() -> {
                try {
                    f.serialize(out);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        finally {
            ls.unlock();
        }
    }

    public boolean login(String username, String password) throws IOException, InterruptedException {
        User u = new User(username,password);
        Frame f = new Frame(getAndIncrement(), FrameType.Login,false,u);

        executeThreadPool(f,out);

        return (Boolean) awaitReply(f.getId());
    }

    public boolean register(String username, String password) throws IOException, InterruptedException {
        User u = new User(username,password);
        Frame f = new Frame(getAndIncrement(), FrameType.Register,false,u);

        System.out.println("Sending id: " + f.getId());
        executeThreadPool(f,out);
        System.out.println("Registration request sent successfully");

        System.out.println("Awaiting registration response...");
        boolean result = (Boolean) awaitReply(f.getId());
        System.out.println("Registration completed with result: " + result);

        return result;
    }

    public void put(String key, byte[] value) throws IOException {
        PutOne p = new PutOne(key, value);
        Frame f = new Frame(getAndIncrement(), FrameType.Put,false,p);

        executeThreadPool(f,out);
    }

    public byte[] get(String key) throws IOException, InterruptedException {
        Frame f = new Frame(getAndIncrement(), FrameType.Get,false,key);

        executeThreadPool(f,out);

        return (byte[]) awaitReply(f.getId());
    }

    public void multiPut(Map<String,byte[]> pairs) throws IOException{
        Frame f = new Frame(getAndIncrement(), FrameType.MultiPut,false,pairs);

        executeThreadPool(f,out);
    }

    public Map<String, byte[]> multiGet(Set<String> keys) throws IOException, InterruptedException{
        Frame f = new Frame(getAndIncrement(), FrameType.MultiGet,false,keys);

        executeThreadPool(f,out);

        return (Map<String, byte[]>) awaitReply(f.getId());
    }

    /*
    public byte[] getWhen(String key, String keyCond, byte[] valueCond){

    }
    */

    public void exit() throws IOException {
        Frame f = new Frame(getAndIncrement(), FrameType.Close,false,null);

        executeThreadPool(f,out);
        
        threadPool.shutdown();
        socket.close();
    }

    public static void main(String[] args) {
        try {
            socket = new Socket(HOST,PORT);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            Client client = new Client();
            Thread receiverThread = new Thread(() -> {
                System.out.println("### Receiver thread starting ###");
                try {
                    client.run();
                } catch (IOException e) {
                    System.out.println("### Receiver thread error: " + e.getMessage() + " ###");
                    e.printStackTrace();
                }
            }, "ReceiverThread");

            receiverThread.setDaemon(false); // Make sure it's not a daemon thread 
            receiverThread.start();

            Client_Interface ci = new Client_Interface();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}


