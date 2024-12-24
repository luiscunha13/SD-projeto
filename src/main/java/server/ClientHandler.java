package server;

import Connection.*;
import database.Users;
import database.Users_Database;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

class ClientHandler implements Runnable {
    private Connection con;
    private Users users;
    private Users_Database users_database;
    private Server server;
    public boolean login = false; //muda para true quando iniciar sessão
    //false ->mostra as opções de registar e de login , true -> mostra o resto das opções

    public ClientHandler(Connection con, Users users, Users_Database users_database, Server server) throws IOException {
        this.con = con;
        this.users = users;
        this.users_database = users_database;
        this.server = server;
    }

    @Override
    public void run() {

        int exit = 0;

        while (exit == 0) {
            try {
                Frame f = con.receive();
                int id = f.getId();
                switch (f.getType()) {
                    case FrameType.Login: { //login
                        User u = (User) f.getData();

                        if (users.login(u.getUsername(), u.getPassword()))
                            login = true;

                        con.send(new Frame(id, FrameType.Login,true,login));

                        break;
                    }
                    case FrameType.Register: { //register
                        User u = (User) f.getData();

                        if (users.register(u.getUsername(), u.getPassword()))
                            login = true;

                        con.send(new Frame(id, FrameType.Register,true,login));

                        break;
                    }
                    case FrameType.Put: { //put
                        PutOne p = (PutOne) f.getData();
                        String key = p.getKey();
                        byte[] value = p.getValue();
                        users_database.put(key, value);

                        break;
                    }
                    case FrameType.Get: { //get
                        String key = (String) f.getData();
                        byte[] data = users_database.get(key);
                        con.send(new Frame(id, FrameType.Get,true,data));

                        break;
                    }
                    case FrameType.MultiPut: { //multiput
                        Map<String,byte[]> map = (Map<String, byte[]>) f.getData();
                        users_database.multiPut(map);

                        break;
                    }
                    case FrameType.MultiGet: { //multiget
                        Set<String> set = (Set<String>) f.getData();
                        Map<String,byte[]> map = users_database.multiGet(set);
                        con.send(new Frame(id, FrameType.MultiGet,true,map));

                        break;
                    }
                    case FrameType.GetWhen:{
                        GetWhen g = (GetWhen) f.getData();
                        byte[] b = users_database.getWhen(g.getKey(),g.getKeyCond(),g.getValueCond());
                        con.send(new Frame(id, FrameType.GetWhen,true,b));
                        break;
                    }
                    case FrameType.Close: { //close
                        con.send(new Frame(id, FrameType.Close,true,""));
                        con.close();
                        exit = 1;
                        break;
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        server.clientDisconnected();
    }
}
