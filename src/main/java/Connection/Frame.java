package Connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Frame {

    private FrameType type; // 0-login 1-register 2-get 3-put 4-multiget 5-multiput
    private boolean answer; //false-cliente->server  true-server->client
    private Object data;

    public Frame(FrameType type, boolean answer, Object data){
        this.type=type;
        this.answer=answer;
        this.data=data;
    }

    public FrameType getType() {
        return type;
    }

    public boolean getAnswer() {
        return answer;
    }

    public Object getData() {
        return data;
    }

    public void send(DataOutputStream out) throws IOException {
        out.writeByte(type.toByte());
        out.writeBoolean(answer);
        switch(type){
            case Login: // o login e o registo mandam a mesma cena (username e password) e recebem na mesma um boolean
            case Register:
                if(answer){
                    out.writeBoolean((boolean) data);
                    out.flush();
                }
                else
                    User.send((User) data, out);
                break;
            case Get:
                if(!answer)
                    PutOne.send((PutOne) data, out);
                break;
            case Put:
                if(answer){
                    byte[] value = (byte[]) data;
                    out.writeInt(value.length);
                    out.write(value);
                }
                else
                    out.writeUTF((String) data);

                out.flush();
                break;
            case MultiGet:
                if(answer)
                    sendMap(data,out);
                break;
            case MultiPut:
                if(answer)
                    sendMap(data,out);
                else{
                    sendSet(data,out);
                }
                break;
        }

    }

    public static Frame receive(DataInputStream in) throws IOException{
        FrameType type = FrameType.fromByte(in.readByte());
        boolean answer = in.readBoolean();
        Object data = null;
        switch(type){
            case Login:
            case Register:
                if(answer)
                    data = in.readBoolean();
                else
                    data = User.receive(in);
                break;
            case Get:
                if(!answer)
                    data = PutOne.receive(in);
                break;
            case Put:
                if(answer){
                    int len = in.readInt();
                    byte[] value = new byte[len];
                    in.readFully(value);
                    data = value;
                }
                else
                    data = in.readUTF();
                break;
            case MultiGet:
                if(!answer)
                    data = receiveMap(in);
                break;
            case MultiPut:
                if(answer)
                    data = receiveMap(in);
                else
                    data = receiveSet(in);
                break;
        }

        return new Frame(type,answer,data);

    }


    public static void sendMap(Object data,DataOutputStream out) throws IOException {
        Map<String, byte[]> map = (Map<String, byte[]>) data;

        out.writeInt(map.size());

        for (Map.Entry<String, byte[]> entry : map.entrySet()) {
            out.writeUTF(entry.getKey());

            byte[] value = entry.getValue();
            out.writeInt(value.length);
            out.write(value);
        }

        out.flush();
    }

    public static Map<String, byte[]> receiveMap(DataInputStream in) throws IOException {
        Map<String, byte[]> map = new HashMap<>();

        int size = in.readInt();

        for (int i = 0; i < size; i++) {

            String key = in.readUTF();

            int len = in.readInt();
            byte[] value = new byte[len];
            in.readFully(value);

            map.put(key, value);
        }

        return map;

    }

    public static void sendSet(Object data, DataOutputStream out) throws IOException {
        Set<String> set = (Set<String>) data;

        out.writeInt(set.size());

        for (String s : set ) {
            out.writeUTF(s);
        }
        out.flush();

    }

    public static Set<String> receiveSet(DataInputStream in) throws IOException {
        Set<String> set = new HashSet<>();

        int size = in.readInt();

        for(int i=0;i<size;i++){
            set.add(in.readUTF());
        }

        return set;
    }


}
