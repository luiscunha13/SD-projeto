package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class User {
    public String username;
    public String password;

    //Lock l = new ReentrantLock(); ver se é preciso um lock para os gets

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

    public User receive(DataInputStream in) throws IOException{
        String username = in.readUTF();
        String password = in.readUTF();
        User u = new User(username,password);
        return u;
    }
}
