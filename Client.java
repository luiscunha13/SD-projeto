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
;
    public boolean running=true; //condição de paragem do handler
    public boolean login=false;

    public boolean login(String username, String password) throws IOException {
        out.writeUTF("login");
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
        out.writeUTF("register");
        out.writeUTF(username);
        out.writeUTF(password);
        out.flush();

        String answer = in.readUTF();
        while(!answer.equals("register")){
            answer = in.readUTF();
        }
        return in.readBoolean();
    }

    public String readData(String key) throws IOException { // ver depois se tem de ser byte[] ou pode ser string
        out.writeUTF("read");
        out.writeUTF(key);
        out.flush();

        String answer = in.readUTF();
        while(!answer.equals("read")){
            answer = in.readUTF();
        }

        return answer;

    }

    public String storeData(String key, String data) throws IOException { // aqui afinal não deve devolver string
        out.writeUTF("store");
        out.writeUTF(key);
        out.writeUTF(data);
        out.flush();

        String answer = in.readUTF();
        while(!answer.equals("store")){
            answer = in.readUTF();
        }
        return answer;
    }

    public Map<String, String> readMultiData(Set<String> keys) throws IOException{
        out.writeUTF("readmulti");
        out.writeInt(keys.size());
        for (String s: keys)
            out.writeUTF(s);
        
    }

    public void storeMultiData(Map<String,String> keysdata) throws IOException{
        out.writeUTF("storemulti");
        out.writeInt(keysdata.size());
        for (Map.Entry<String,String> e: keysdata.entrySet()){
            out.writeUTF(e.getKey());
            out.writeUTF(e.getValue());
        }
    }


    public static void main(String[] args){
        try{
            Socket socket = new Socket(HOST,PORT);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            Client_Interface ci = new Client_Interface();

            ci.OptionsHandler();


        }catch(Exception e){
            e.printStackTrace();
        }

    }


}


