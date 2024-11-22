package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 6666;

    private static DataOutputStream out;
    private static DataInputStream in;

    public boolean running=true; //condição de paragem do handler
    public boolean login=false;

    public boolean login(String username, String password) throws IOException {
        out.writeInt(0);
        out.writeUTF(username);
        out.writeUTF(password);
        out.flush();

        String answer = in.readUTF();
        while(!answer.equals("login")){
            answer = in.readUTF();
        }
        return in.readBoolean();

    }

    public boolean register(String username, String password) throws IOException {
        out.writeInt(1);
        out.writeUTF(username);
        out.writeUTF(password);
        out.flush();

        String answer = in.readUTF();
        while(!answer.equals("register")){
            answer = in.readUTF();
        }
        return in.readBoolean();
    }

    public byte[] get(String key) throws IOException {
        out.writeInt(2);
        out.writeUTF(key);
        out.flush();

        String answer = in.readUTF();
        while(!answer.equals("read")){
            answer = in.readUTF();
        }

        return in.readAllBytes();
    }

    public void put(String key, byte[] value) throws IOException {
        out.writeInt(3);
        out.writeUTF(key);
        out.write(value);
        out.flush();

        String answer = in.readUTF();
        while(!answer.equals("store")){
            answer = in.readUTF();
        }

    }

    public Map<String, byte[]> multiGet(Set<String> keys) throws IOException{
        out.writeInt(4);
        out.writeInt(keys.size());
        for (String s: keys)
            out.writeUTF(s);

        String answer = in.readUTF();
        while(!answer.equals("readmulti")){
            answer = in.readUTF();
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

        String answer = in.readUTF();
        while(!answer.equals("storemulti")){
            answer = in.readUTF();
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


