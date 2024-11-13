import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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


