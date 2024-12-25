package server;

import Connection.*;
import database.*;

import java.util.Map;
import java.util.Set;

class Request {
    private final Frame frame;
    private final Connection connection;
    private final Users users;
    private final Users_Database users_database;
    public boolean login = false; //muda para true quando iniciar sessão
    //false ->mostra as opções de registar e de login , true -> mostra o resto das opções

    public Request(Frame frame, Connection connection, Users users, Users_Database usersDatabase) {
        this.frame = frame;
        this.connection = connection;
        this.users = users;
        this.users_database = usersDatabase;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void process() {
        int id = frame.getId();
        try {
            switch (frame.getType()) {
                case FrameType.Login: { //login
                    User u = (User) frame.getData();

                    if (users.login(u.getUsername(), u.getPassword()))
                        login = true;

                    connection.send(new Frame(id, FrameType.Login,true,login));

                    break;
                }
                case FrameType.Register: { //register
                    User u = (User) frame.getData();

                    if (users.register(u.getUsername(), u.getPassword()))
                        login = true;

                    connection.send(new Frame(id, FrameType.Register,true,login));

                    break;
                }
                case FrameType.Put: { //put
                    PutOne p = (PutOne) frame.getData();
                    String key = p.getKey();
                    byte[] value = p.getValue();
                    users_database.put(key, value);

                    break;
                }
                case FrameType.Get: { //get
                    String key = (String) frame.getData();
                    byte[] data = users_database.get(key);
                    connection.send(new Frame(id, FrameType.Get,true,data));

                    break;
                }
                case FrameType.MultiPut: { //multiput
                    Map<String,byte[]> map = (Map<String, byte[]>) frame.getData();
                    users_database.multiPut(map);

                    break;
                }
                case FrameType.MultiGet: { //multiget
                    Set<String> set = (Set<String>) frame.getData();
                    Map<String,byte[]> map = users_database.multiGet(set);
                    connection.send(new Frame(id, FrameType.MultiGet,true,map));

                    break;
                }
                case FrameType.GetWhen: {
                    GetWhen g = (GetWhen) frame.getData();
                    byte[] b = users_database.getWhen(g.getKey(),g.getKeyCond(),g.getValueCond());
                    connection.send(new Frame(id, FrameType.GetWhen,true,b));
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
