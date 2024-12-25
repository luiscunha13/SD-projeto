package server;

import Connection.Connection;
import database.Users;
import database.Users_Database;
import Connection.Frame;
import Connection.FrameType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.*;

public class Server {
    Lock l = new ReentrantLock();
    private final Condition sessionAvailable = l.newCondition();
    private final int maxSessions;
    private int activeSessions = 0;
    private final Thread[] workerThreads;
    private final int numThreads = 5;
    private Queue<Request> requestQueue = new LinkedList<>();
    private Lock lQueue = new ReentrantLock();

    public Server(int maxSessions) {
        this.maxSessions = maxSessions;
        this.workerThreads = new Thread[numThreads];
    }

    private void initWorkerThreads() {
        for (int i = 0; i < workerThreads.length; i++) {
            workerThreads[i] = new Thread(() -> {
                while (true) {
                    try {
                        Request request;
                        lQueue.lock();
                        try {
                            request = requestQueue.remove();
                        }finally {
                            lQueue.unlock();
                        }
                        request.process();
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }
            });
            workerThreads[i].start();
        }
    }

    public void addRequest(Request request) {
        lQueue.lock();
        try {
            requestQueue.add(request);
        } finally {
            lQueue.unlock();
        }
    }

    public void clientConnect() throws InterruptedException {
        l.lock();
        try {
            while (activeSessions >= maxSessions) {
                sessionAvailable.await();
            }
            activeSessions++;
        } finally {
            l.unlock();
        }
    }

    public void clientDisconnected() {
        l.lock();
        try {
            activeSessions--;
            sessionAvailable.signal();
        } finally {
            l.unlock();
        }
    }

    public void start() throws IOException{
        ServerSocket ss = null;

        try{
            ss = new ServerSocket(6666);
            Users users = new Users();
            Users_Database users_database = new Users_Database();
            initWorkerThreads();

            System.out.println("Server listening for clients on port: " + ss.getLocalPort());

            while(true){
                Socket socket = ss.accept();
                Connection con = new Connection(socket);

                clientConnect();

                new Thread(new ClientHandler(con,users,users_database,this)).start();
            }

        } catch(Exception e){
            e.printStackTrace();
        } finally {
            if (ss != null && !ss.isClosed()) {
                ss.close();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(Integer.parseInt(args[0]));
        server.start();
    }

}


