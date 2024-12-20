package client;

import Connection.Frame;
import Connection.PutOne;
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

    public void put(String key, byte[] value) throws IOException {
        PutOne p = new PutOne(key, value);
        Frame f = new Frame(2,false,p);
        f.send(out);
    }

    public byte[] get(String key) throws IOException {
        Frame f = new Frame(3,false,key);
        f.send(out);

        try{
            Frame res = Frame.receive(in);

            return (byte[])res.getData();
        }catch(Exception e){
            return null;
        }
    }

    public void multiPut(Map<String,byte[]> pairs) throws IOException{
        Frame f = new Frame(4,false,pairs);
        f.send(out);


    }

    public Map<String, byte[]> multiGet(Set<String> keys) throws IOException{
        Frame f = new Frame(5,false,keys);
        f.send(out);

        try{
            Frame res = Frame.receive(in);

            return (Map<String, byte[]>) res.getData();
        }catch(Exception e){
            return null;
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


