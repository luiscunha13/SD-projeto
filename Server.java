import java.io.DataInputStream;
import java.io.DataOutputStream;
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

    void multiPut(Map<String, byte[]> pairs){
        l.lock();

        try{
            users_database.putAll(pairs);
        }finally{
            l.unlock();
        }
    }

    Map<String, byte[]> multiGet(Set<String> keys){
        Map<String, byte[]> m = new HashMap<>();

        try{
            for(String s : keys)
                if(users_database.containsKey(s))
                    m.put(s, users_database.get(s));
                else
                    m.put(s,null);
        }finally{
            l.unlock();
        }

        return m;
    }


}

class ClientHandler implements Runnable{
    private Socket socket;
    private Users users;
    private Users_Database users_database;

    private DataOutputStream out;
    private DataInputStream in;

    private Server server;

    public boolean running=true; //condição de paragem do handler
    public boolean login=false; //muda para true quando iniciar sessão
    //false ->mostra as opções de registar e de login , true -> mostra o resto das opções

    public ClientHandler(Socket socket, Users users, Users_Database users_database, Server server) throws IOException{
        this.socket = socket;
        this.users = users;
        this.users_database = users_database;
        this.server = server;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run(){
        try{
            int exit=0;

            while(exit==0){
                String message = in.readUTF();
                switch(message){
                    case "login": {
                        String username = in.readUTF();
                        String password = in.readUTF();
                        if (users.login(username, password))
                            login = true;
                        out.writeUTF("login");
                        out.writeBoolean(login);
                        break;
                    }
                    case "register": {
                        String username = in.readUTF();
                        String password = in.readUTF();
                        if (users.register(username, password))
                            login = true;
                        out.writeUTF("register");
                        out.writeBoolean(login);
                        break;
                    }
                    case "read": {
                        String key = in.readUTF();
                        byte[] data = users_database.get(key);
                        String answer;
                        if(data == null)
                            answer = "null";
                        else
                            answer = Arrays.toString(data);

                        out.writeUTF("read");
                        out.writeUTF(answer);

                        break;
                    }
                    case "store":{
                        String key = in.readUTF();
                        String data = in.readUTF();
                        byte[] bdata = data.getBytes();
                        users_database.put(key,bdata);

                        out.writeUTF("store");

                        break;
                    }
                    case "readmulti":{
                        int size = in.readInt();
                        Set<String> s = new HashSet<>();
                        for(int i=0;i<size;i++)
                            s.add(in.readUTF());

                        Map<String,byte[]> m = users_database.multiGet(s);

                        out.writeUTF("readmulti");
                        out.writeInt(m.size());
                        for (Map.Entry<String,byte[]> e: m.entrySet()){
                            out.writeUTF(e.getKey());
                            byte[] data = e.getValue();
                            String answer;
                            if(data==null)
                                answer = "null";
                            else
                                answer =  Arrays.toString(data); // ver depois se é assim que se converte para string
                            out.writeUTF(answer);
                        }

                        break;
                    }
                    case "storemulti":{
                        int size = in.readInt();
                        Map<String,byte[]> m = new HashMap<>();
                        for(int i=0;i<size;i++){
                            String key = in.readUTF();
                            String data = in.readUTF();
                            byte[] bdata = data.getBytes();
                            m.put(key,bdata);
                        }

                        users_database.multiPut(m);

                        out.writeUTF("storemulti");

                        break;
                    }
                    case "exit": {
                        exit=1;
                        break;
                    }
                }
                out.flush();
                server.clientDisconnected();
            }

        }catch (Exception e){
            System.out.println(e);
        }finally {
            try{
                socket.close();

            }catch(IOException e){
                e.printStackTrace();
            }
            server.clientDisconnected();
        }
    }
}


public class Server {
    Lock l = new ReentrantLock();
    private final Condition sessionAvailable = l.newCondition();
    int maxSessions;
    private int activeSessions = 0;

    public void clientDisconnected() {
        l.lock();
        try {
            activeSessions--;
            sessionAvailable.signal();
        } finally {
            l.unlock();
        }
    }

    public void Main(String[] args) throws IOException{
        ServerSocket ss = null;
        maxSessions = Integer.parseInt(args[0]);

        try{
            ss = new ServerSocket(6666);
            Users users = new Users();
            Users_Database users_database = new Users_Database();

            System.out.println("Server listening for clients on port: " + ss.getLocalPort());

            while(true){
                Socket socket = ss.accept();

                l.lock();
                try {
                    while (activeSessions >= maxSessions) {
                        sessionAvailable.await();
                    }
                    activeSessions++;
                } finally {
                    l.unlock();
                }

                new Thread(new ClientHandler(socket,users,users_database,this)).start();
            }

        } catch(Exception e){
            e.printStackTrace();
        } finally {
            if (ss != null && !ss.isClosed()) {
                ss.close();
            }
        }
    }

}


