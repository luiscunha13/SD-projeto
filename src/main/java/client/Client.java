package client;

import Connection.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
    private Map<Integer,Frame> replies = new HashMap<>();
    private Queue<Frame> repliesToPrint = new LinkedList<>();
    private Condition replyCondition = ls.newCondition();
    private Lock lockId = new ReentrantLock();
    private int idRequest = 0;

    ExecutorService threadPool = Executors.newFixedThreadPool(5);

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
        try {
            while (true) {
                Frame res = Frame.deserialize(in);
                ls.lock();
                try {
                    replies.put(res.getId(), res);
                    replyCondition.signalAll();
                } finally {
                    ls.unlock();
                }

                if(res.getType()==FrameType.Get || res.getType()==FrameType.MultiGet){
                    repliesToPrint.add(res);
                }

                if(res.getId()==-1) break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Object awaitReply(int requestId) throws InterruptedException {
        ls.lock();
        try {
            while (!replies.containsKey(requestId)) {
                replyCondition.await();
            }
            return replies.get(requestId).getData();
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

    public List<Frame> getRepliesToPrint(){
        List<Frame> rep = new ArrayList<>();

        while(!repliesToPrint.isEmpty())
            rep.add(repliesToPrint.poll());

        return rep;
    }

    public boolean login(String username, String password) throws InterruptedException {
        User u = new User(username,password);
        Frame f = new Frame(getAndIncrement(), FrameType.Login,false,u);

        executeThreadPool(f,out);

        return (Boolean) awaitReply(f.getId());
    }

    public boolean register(String username, String password) throws InterruptedException {
        User u = new User(username,password);
        Frame f = new Frame(getAndIncrement(), FrameType.Register,false,u);

        executeThreadPool(f,out);

        return (boolean) (Boolean) awaitReply(f.getId());
    }

    public void put(String key, byte[] value) {
        PutOne p = new PutOne(key, value);
        Frame f = new Frame(getAndIncrement(), FrameType.Put,false,p);

        executeThreadPool(f,out);
    }

    public int get(String key) {
        int i = getAndIncrement();
        Frame f = new Frame(i, FrameType.Get,false,key);

        executeThreadPool(f,out);

        return i;
    }

    public void multiPut(Map<String,byte[]> pairs) {
        Frame f = new Frame(getAndIncrement(), FrameType.MultiPut,false,pairs);

        executeThreadPool(f,out);
    }

    public int multiGet(Set<String> keys) {
        int i = getAndIncrement();
        Frame f = new Frame(i, FrameType.MultiGet,false,keys);

        executeThreadPool(f,out);

        return i;
    }

    public int getWhen(String key, String keyCond, byte[] valueCond){
        int i = getAndIncrement();
        GetWhen g = new GetWhen(key, keyCond, valueCond);
        Frame f = new Frame(i, FrameType.GetWhen,false,g);

        executeThreadPool(f,out);

        return i;

    }

    public void exit() throws IOException, InterruptedException {
        Frame f = new Frame(-1, FrameType.Close,false,null);

        executeThreadPool(f,out);
        awaitReply(-1);

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
                try {
                    client.run();
                } catch (IOException e) {
                    System.out.println("### Receiver thread error: " + e.getMessage() + " ###");
                    e.printStackTrace();
                }
            }, "ReceiverThread");

            receiverThread.setDaemon(false); // Make sure it's not a daemon thread 
            receiverThread.start();

            Client_Interface ci = new Client_Interface(client);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}


