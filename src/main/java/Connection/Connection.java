package Connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Connection {
    Socket socket;
    
    DataInputStream in;
    DataOutputStream out;
    Lock ls = new ReentrantLock();
    Lock lr = new ReentrantLock();

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
    }

    public void send(Frame f) throws IOException{
        ls.lock();
        try{
            if(!socket.isClosed()) {
                f.serialize(out);
                out.flush();
            }
        }finally{
            ls.unlock();
        }
    }

    public Frame receive() throws IOException{
        lr.lock();
        try{
            return Frame.deserialize(in);
        }finally {
            lr.unlock();
        }
    }

    public void close(){
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
