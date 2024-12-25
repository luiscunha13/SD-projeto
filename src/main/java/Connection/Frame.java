package Connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Frame {
    private Integer id;
    private FrameType type; // 0-login 1-register 2-get 3-put 4-multiget 5-multiput
    private boolean answer; //false-cliente->server  true-server->client
    private Object data;

    public Frame(Integer id, FrameType type, boolean answer, Object data){
        this.id = id;
        this.type=type;
        this.answer=answer;
        this.data=data;
    }

    public Integer getId() {
        return id;
    }

    public FrameType getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(id);
        out.writeByte(type.toByte());
        out.writeBoolean(answer);
        switch(type){
            case Login:
            case Register:
                if(answer)
                    out.writeBoolean((boolean) data);
                else
                    User.send((User) data, out);
                break;
            case Put:
                if(!answer)
                    PutOne.send((PutOne) data, out);
                break;
            case Get:
                if(answer){
                    byte[] value = (byte[]) data;
                    out.writeInt(value.length);
                    out.write(value);
                }
                else
                    out.writeUTF((String) data);
                break;
            case MultiPut:
                if(!answer)
                    serializeMap(data,out);
                break;
            case MultiGet:
                if(answer)
                    serializeMap(data,out);
                else
                    serializeSet(data,out);
                break;
            case GetWhen:
                if(answer){
                    byte[] value = (byte[]) data;
                    out.writeInt(value.length);
                    out.write(value);
                }
                else
                    GetWhen.send((GetWhen) data, out);
                break;
            case Close:
                if(answer)
                    out.writeUTF((String) data);
                break;
        }
    }

    public static Frame deserialize(DataInputStream in) throws IOException{
        int id = in.readInt();
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
            case Put:
                if(!answer)
                    data = PutOne.receive(in);
                break;
            case Get:
                if(answer){
                    int len = in.readInt();
                    byte[] value = new byte[len];
                    in.readFully(value);
                    data = value;
                }
                else
                    data = in.readUTF();
                break;
            case MultiPut:
                if(!answer)
                    data = deserializeMap(in);
                break;
            case MultiGet:
                if(answer)
                    data = deserializeMap(in);
                else
                    data = deserializeSet(in);
                break;
            case GetWhen:
                if(answer){
                    int len = in.readInt();
                    byte[] value = new byte[len];
                    in.readFully(value);
                    data = value;
                }
                else
                    data = GetWhen.receive(in);

                break;
            case Close:
                if(answer)
                    data = in.readUTF();
        }

        return new Frame(id,type,answer,data);
    }


    public static void serializeMap(Object data, DataOutputStream out) throws IOException {
        Map<String, byte[]> map = (Map<String, byte[]>) data;

        out.writeInt(map.size());

        for (Map.Entry<String, byte[]> entry : map.entrySet()) {
            out.writeUTF(entry.getKey());

            byte[] value = entry.getValue();
            out.writeInt(value.length);
            out.write(value);
        }
    }

    public static Map<String, byte[]> deserializeMap(DataInputStream in) throws IOException {
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

    public static void serializeSet(Object data, DataOutputStream out) throws IOException {
        Set<String> set = (Set<String>) data;

        out.writeInt(set.size());

        for (String s : set ) {
            out.writeUTF(s);
        }
    }

    public static Set<String> deserializeSet(DataInputStream in) throws IOException {
        Set<String> set = new HashSet<>();

        int size = in.readInt();

        for(int i=0;i<size;i++){
            set.add(in.readUTF());
        }

        return set;
    }

}
