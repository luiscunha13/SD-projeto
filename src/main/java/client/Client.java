package client;

import Connection.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
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
    private Thread receiverThread;
    private final Thread[] workerThreads = new Thread[5];
    private Queue<Frame> frameQueue = new LinkedList<>();
    private Lock lQueue = new ReentrantLock();

    private int getAndIncrement() {
        lockId.lock();
        try{
            idRequest++;
        } finally {
            lockId.unlock();
        }
        return idRequest;
    }

    private void initReceiverThread() throws IOException {
        receiverThread = new Thread(() -> {
            try{
                while (true) {
                    Frame res;
                    res = Frame.deserialize(in);

                    ls.lock();
                    try {
                        replies.put(res.getId(), res);
                        replyCondition.signalAll();
                    } finally {
                        ls.unlock();
                    }

                    if(res.getType()==FrameType.Get || res.getType()==FrameType.MultiGet || res.getType()==FrameType.GetWhen){
                        repliesToPrint.add(res);
                    }

                    if(res.getId()==-1) break;
                }
            }catch(IOException e){
                throw new RuntimeException(e);
            }
        });
        receiverThread.start();
        System.out.println("iniciei receiver thread");
    }

    private void shutdownReceiverThread(){
        receiverThread.interrupt();

        try {
            receiverThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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

    private void initWorkerThreads() {
        for (int i = 0; i < workerThreads.length; i++) {
            workerThreads[i] = new Thread(() -> {
                while (true) {
                    try {
                        Frame frame;
                        if(!frameQueue.isEmpty()){
                            System.out.println("tem frame");
                            lQueue.lock();
                            try {
                                frame = frameQueue.remove();
                            }finally {
                                lQueue.unlock();
                            }
                            frame.serialize(out);
                            out.flush();
                            System.out.println("mandei frame");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }
            });
            workerThreads[i].start();
            System.out.println("iniciei worker thread "+i);
        }
    }

    public void shutdownWorkers() {

        for (Thread worker : workerThreads) {
            worker.interrupt();
        }

        frameQueue.clear();

        for (Thread worker : workerThreads) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void addFrame(Frame frame) {
        lQueue.lock();
        try {
            frameQueue.add(frame);
        } finally {
            lQueue.unlock();
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

        addFrame(f);

        return (Boolean) awaitReply(f.getId());
    }

    public boolean register(String username, String password) throws InterruptedException {
        User u = new User(username,password);
        Frame f = new Frame(getAndIncrement(), FrameType.Register,false,u);

        addFrame(f);
        System.out.println("adicionei frame");

        if(frameQueue.isEmpty())
            System.out.println("adicionei mas continua vazio");


        return (boolean) (Boolean) awaitReply(f.getId());
    }

    public void put(String key, byte[] value) {
        PutOne p = new PutOne(key, value);
        Frame f = new Frame(getAndIncrement(), FrameType.Put,false,p);

        addFrame(f);
    }

    public int get(String key) {
        int i = getAndIncrement();
        Frame f = new Frame(i, FrameType.Get,false,key);

        addFrame(f);

        return i;
    }

    public void multiPut(Map<String,byte[]> pairs) {
        Frame f = new Frame(getAndIncrement(), FrameType.MultiPut,false,pairs);

        addFrame(f);
    }

    public int multiGet(Set<String> keys) {
        int i = getAndIncrement();
        Frame f = new Frame(i, FrameType.MultiGet,false,keys);

        addFrame(f);

        return i;
    }

    public int getWhen(String key, String keyCond, byte[] valueCond){
        int i = getAndIncrement();
        GetWhen g = new GetWhen(key, keyCond, valueCond);
        Frame f = new Frame(i, FrameType.GetWhen,false,g);

        addFrame(f);

        return i;

    }

    public void exit() throws IOException, InterruptedException {
        Frame f = new Frame(-1, FrameType.Close,false,null);

        addFrame(f);
        awaitReply(-1);

        shutdownWorkers();
        shutdownReceiverThread();
        socket.close();
    }

    public void start() throws IOException {
        socket = new Socket(HOST,PORT);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());

        initReceiverThread();
        initWorkerThreads();
    }

    public static void main(String[] args) {
        try {
            Client client = new Client();

            client.start();

            Client_Interface ci = new Client_Interface(client);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}


