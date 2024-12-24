package Connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class GetWhen {

    private String key;
    private String keyCond;
    private byte[] valueCond;

    public GetWhen(String key, String keyCond, byte[] valueCond){
        this.key = key;
        this.keyCond = keyCond;
        this.valueCond = valueCond;
    }

    public String getKey() {
        return key;
    }

    public String getKeyCond(){
        return keyCond;
    }

    public byte[] getValueCond() {
        return valueCond;
    }

    public static void send(GetWhen g, DataOutputStream out) throws IOException {
        out.writeUTF(g.key);
        out.writeUTF(g.keyCond);
        out.writeInt(g.valueCond.length);
        out.write(g.valueCond);
        out.flush();
    }

    public static GetWhen receive(DataInputStream in) throws IOException{
        String key = in.readUTF();
        String keyCond = in.readUTF();
        int len = in.readInt();
        byte[] valueCond = new byte[len];
        in.readFully(valueCond);
        return new GetWhen(key, keyCond, valueCond);
    }
}
