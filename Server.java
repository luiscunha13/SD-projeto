import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.*;

class Users{
    private Map<String, String> users = new HashMap<>(); // username-password
    Lock l = new ReentrantLock();

    public boolean register(String username, String password){
        boolean out;
        l.lock();
        try{
            if (users.containsKey(username)) {
                out= false;
            }
            else {
                users.put(username, password);
                out=true;
            }
        } finally{
            l.unlock();
        }

        return out;
    }
    public boolean login(String username, String password){
        boolean out;
        l.lock();

        try{
            if (!users.containsKey(username)) {
                out=false;
            }
            else{
                out=users.get(username).equals(password);
            }
        }finally{
           l.unlock();
        }

        return out;
    }

}

class Users_Database{
    private Map<String, byte[]> users_database = new HashMap<>(); // key-data
    Lock l = new ReentrantLock();

    void put(String key, byte[] value){
        l.lock();

        try{
            users_database.put(key,value);
        }finally{
            l.unlock();
        }
    }

    byte[] get(String key){
        byte[] answer=null;
        l.lock();

        try{
            if(users_database.containsKey(key))
                answer = users_database.get(key);
        }finally{
            l.unlock();
        }

        return answer;
    }

    // funções que estão no enunciado
}

public class Server {
    Lock l = new ReentrantLock();

    public void Main(String[] args) throws IOException{
        Server server = new Server();
        server.start(server);
    }

    private void start(Server server) throws IOException {
        ServerSocket socket = null;

        try{
            socket = new ServerSocket(6666);
            Users users = new Users();
            Users_Database users_database = new Users_Database();

            System.out.println("Server listening for clients on port: " + socket);

            while(true){
                Socket clientSocket = socket.accept();

                new Thread(new ClientHandler(clientSocket,server,users,users_database)).start();
            }

        } catch(Exception e){
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

}

class ClientHandler implements Runnable{
    private Socket client_socket;
    private Server server;
    private Users users;
    private Users_Database users_database;

    public boolean running=true; //condição de paragem do handler
    public boolean login=false; //muda para true quando iniciar sessão
                                //false ->mostra as opções de registar e de login , true -> mostra o resto das opções

    public ClientHandler(Socket client_socket,Server server, Users users, Users_Database users_database){
        this.client_socket = client_socket;
        this.server = server;
        this.users = users;
        this.users_database = users_database;
    }

    @Override
    public void run(){


        while (running) {
            //mostrar o menu das opções no terminal

        }
    }



}
