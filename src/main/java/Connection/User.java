package Connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class User {
    public String username;
    public String password;

    public User(String username, String password){
        this.username = username;
        this.password = password;
    }

    public String getUsername(){
        return this.username;
    }

    public String getPassword(){
        return this.password;
    }

    public static void send(User u, DataOutputStream out) throws IOException {
        out.writeUTF(u.username);
        out.writeUTF(u.password);
    }

    public static User receive(DataInputStream in) throws IOException{
        String username = in.readUTF();
        String password = in.readUTF();
        return new User(username,password);
    }
}
