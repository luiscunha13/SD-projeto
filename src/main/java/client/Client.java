package client;

import Connection.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.*;

public class Client {
    private Socket socket = null;
    private final String HOST = "localhost";
    private final int PORT = 6666;
    private Connection con;

    private Lock lReply = new ReentrantLock();
    private Condition replyCondition = lReply.newCondition();
    private Map<Integer,Frame> replies = new HashMap<>();

    private Queue<Frame> repliesToPrint = new LinkedList<>();

    private Lock lId = new ReentrantLock();
    private int idRequest = -1;

    private Thread receiverThread;
    private final Thread[] workerThreads = new Thread[5];

    private Queue<Frame> frameQueue = new LinkedList<>();
    private Lock lQueue = new ReentrantLock();
    private final Condition notEmpty = lQueue.newCondition();

    private Lock getFrameKeysLock = new ReentrantLock();
    private Map<Integer, String> getFrameKeys = new HashMap<>();

    private int getAndIncrement() {
        lId.lock();
        try{
            idRequest++;
        } finally {
            lId.unlock();
        }
        return idRequest;
    }

    private void initReceiverThread() {
        receiverThread = new Thread(() -> {
            try{
                while (true) {
                    Frame res = con.receive();

                    lReply.lock();
                    try {
                        replies.put(res.getId(), res);
                        replyCondition.signalAll();
                    } finally {
                        lReply.unlock();
                    }

                    if(res.getType()==FrameType.Get || res.getType()==FrameType.MultiGet || res.getType()==FrameType.GetWhen){
                        repliesToPrint.add(res);
                    }

                    if(res.getId()==-1) break;
                }
            } catch(IOException e){
                System.err.println("Falha na ligação ao servidor. A encerrar o cliente.");
                shutdownWorkers();
                shutdownReceiverThread();
                try {socket.close();} catch(IOException ignored){}
                System.exit(1);
            }
        });
        receiverThread.start();
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
        lReply.lock();
        try {
            while (!replies.containsKey(requestId)) {
                replyCondition.await();
            }
            return replies.get(requestId).getData();
        } finally {
            lReply.unlock();
        }
    }

    private void initWorkerThreads() {
        for (int i = 0; i < workerThreads.length; i++) {
            workerThreads[i] = new Thread(() -> {
                while (true) {
                        Frame frame;
                        lQueue.lock();
                        try {
                            while (frameQueue.isEmpty())
                                notEmpty.await();
                            frame = frameQueue.remove();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        } finally {
                            lQueue.unlock();
                        }

                        try {
                            if (frame != null) {
                                con.send(frame);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }
                }
            });
            workerThreads[i].start();
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
            notEmpty.signal();
        } finally {
            lQueue.unlock();
        }
    }

    private void addKey(int id, String key) {
        getFrameKeysLock.lock();
        try{
            getFrameKeys.put(id, key);
        } finally {
            getFrameKeysLock.unlock();
        }
    }

    public String consultKey(int id) {
        getFrameKeysLock.lock();
        try {
            String key = getFrameKeys.get(id);
            getFrameKeys.remove(id);
            return key;
        } finally {
            getFrameKeysLock.unlock();
        }
    }

    public List<Frame> getRepliesToPrint(){
        List<Frame> rep = new ArrayList<>(repliesToPrint);
        repliesToPrint.clear();
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

        addKey(i, key);
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

        addKey(i, key);
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
        try {
            socket = new Socket(HOST, PORT);

            con = new Connection(socket);

            initReceiverThread();
            initWorkerThreads();
        }
        catch (ConnectException e){
            System.err.println("Não foi possível conectar ao servidor.");
            System.exit(1);
        }
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


