package Connection;

import client.User;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Frame {

    private int type; // 0-login 1-register 2-get 3-put 4-multiget 5-multiput
    private boolean answer; //false-cliente->server  true-server->client
    private Object data;

    public Frame(int type, boolean answer, Object data){
        this.type=type;
        this.answer=answer;
        this.data=data;
    }

    public int getType() {
        return type;
    }

    public boolean getAnswer() {
        return answer;
    }

    public Object getData() {
        return data;
    }

    public void send(DataOutputStream out) throws IOException {
        out.writeInt(type);
        out.writeBoolean(answer);
        switch(type){
            case 0: // o login e o registo mandam a mesma cena (username e password) e recebem na mesma um boolean
            case 1:
                if(answer)
                    out.writeBoolean((boolean) data);
                else
                    User.send((User) data, out);
                break;
            case 2:

                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
        }

    }

}
