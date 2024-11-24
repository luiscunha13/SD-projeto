package client;

import Connection.Frame;
import Connection.User;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 6666;

    private static DataOutputStream out;
    private static DataInputStream in;
    Lock ls = new ReentrantLock();
    Lock lr = new ReentrantLock();

    public boolean running=true; //condição de paragem do handler
    public boolean login=false;

    public boolean login(String username, String password) throws IOException {
        User u = new User(username,password);
        Frame f = new Frame(0,false,u);
        f.send(out);

        try{
            Frame res = Frame.receive(in);

            return (Boolean)res.getData();
        }catch(Exception e){
            return false;
        }
    }

    public boolean register(String username, String password) throws IOException {
        User u = new User(username,password);
        Frame f = new Frame(1,false,u);
        f.send(out);

        try{
            Frame res = Frame.receive(in);

            return (Boolean)res.getData();
        }catch(Exception e){
            return false;
        }
    }

    public byte[] get(String key) throws IOException {
        ls.lock();
        try{
            out.writeInt(2);
            out.writeUTF(key);
            out.flush();
        }finally {
            ls.unlock();
        }

        lr.lock();
        try{
            int answer = in.readInt();
            while(answer!=2){
                answer = in.readInt();
            }

            int len = in.readInt();
            byte[] data = new byte[len];
            in.readFully(data);

            return data;
        }finally {
            lr.unlock();
        }
    }

    public void put(String key, byte[] value) throws IOException {
        out.writeInt(3);
        out.writeUTF(key);
        out.write(value);
        out.flush();

        int answer = in.readInt();
        while(answer!=3){
            answer = in.readInt();
        }

    }

    public Map<String, byte[]> multiGet(Set<String> keys) throws IOException{
        out.writeInt(4);
        out.writeInt(keys.size());
        for (String s: keys)
            out.writeUTF(s);

        int answer = in.readInt();
        while(answer!=4){
            answer = in.readInt();
        }

        int size = in.readInt();
        Map<String,byte[]> m = new HashMap<>();
        for(int i=0;i<size;i++){
            String key = in.readUTF();
            byte[] data = in.readAllBytes();
            m.put(key,data);
        }

        return m;
    }

    public void multiPut(Map<String,byte[]> pairs) throws IOException{
        out.writeInt(6);
        out.writeInt(pairs.size());
        for (Map.Entry<String,byte[]> e: pairs.entrySet()){
            out.writeUTF(e.getKey());
        //    out.writeUTF(e.getValue());
        }

        int answer = in.readInt();
        while(answer!=6){
            answer = in.readInt();
        }

    }


    public static void main(String[] args){
        try{
            Socket socket = new Socket(HOST,PORT);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            Client_Interface ci = new Client_Interface();


        }catch(Exception e){
            e.printStackTrace();
        }

    }


}


