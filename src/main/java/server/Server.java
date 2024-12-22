package server;

import Connection.Connection;
import database.Users;
import database.Users_Database;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.*;

public class Server {
    Lock l = new ReentrantLock();
    private final Condition sessionAvailable = l.newCondition();
    private final int maxSessions;
    private int activeSessions = 0;

    public Server(int maxSessions) {
        this.maxSessions = maxSessions;
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


