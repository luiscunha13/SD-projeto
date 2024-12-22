package client;

import Connection.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Client {
    private static Socket socket = null;
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
        Frame f = new Frame(FrameType.Login,false,u);
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
        Frame f = new Frame(FrameType.Register,false,u);
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
        Frame f = new Frame(FrameType.Put,false,p);
        f.send(out);
    }

    public byte[] get(String key) throws IOException {
        Frame f = new Frame(FrameType.Get,false,key);
        f.send(out);

        try{
            Frame res = Frame.receive(in);

            return (byte[])res.getData();
        }catch(Exception e){
            return null;
        }
    }

    public void multiPut(Map<String,byte[]> pairs) throws IOException{
        Frame f = new Frame(FrameType.MultiPut,false,pairs);
        f.send(out);


    }

    public Map<String, byte[]> multiGet(Set<String> keys) throws IOException{
        Frame f = new Frame(FrameType.MultiGet,false,keys);
        f.send(out);

        try{
            Frame res = Frame.receive(in);

            return (Map<String, byte[]>) res.getData();
        }catch(Exception e){
            return null;
        }
    }

    public void exit() throws IOException {
        Frame f = new Frame(FrameType.Close,false,null);
        f.send(out);
        socket.close();
    }


    public static void main(String[] args) throws IOException {
        try{
            socket = new Socket(HOST,PORT);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            Client_Interface ci = new Client_Interface();

        }catch(Exception e){
            e.printStackTrace();
        }
    }


}


