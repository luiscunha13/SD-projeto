package Connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PutOne {

    public String key;

    public byte[] value;

    public PutOne(String key, byte[] value){
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public byte[] getValue() {
        return value;
    }

    public static void send(PutOne p, DataOutputStream out) throws IOException {
        out.writeUTF(p.key);
        out.writeInt(p.value.length);
        out.write(p.value);
        out.flush();
    }

    public static PutOne receive(DataInputStream in) throws IOException{
        String key = in.readUTF();
        int len = in.readInt();
        byte[] value = new byte[len];
        in.readFully(value);
        return new PutOne(key,value);
    }
}
