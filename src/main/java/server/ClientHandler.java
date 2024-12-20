package server;

import Connection.*;
import Connection.User;
import database.Users;
import database.Users_Database;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class ClientHandler implements Runnable {
    private Connection con;
    private Users users;
    private Users_Database users_database;

    private DataOutputStream out;
    private DataInputStream in;
    Lock ls = new ReentrantLock();
    Lock lr = new ReentrantLock();

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
                switch (f.getType()) {
                    case 0: { //login
                        User u = (User) f.getData();

                        if (users.login(u.getUsername(), u.getPassword()))
                            login = true;

                        con.send(new Frame(0,true,login));

                        break;
                    }
                    case 1: { //register
                        User u = (User) f.getData();
                        String username;
                        String password;

                        if (users.register(u.getUsername(), u.getPassword()))
                            login = true;

                        con.send(new Frame(1,true,login));

                        break;
                    }
                    case 2: { //put
                        PutOne p = (PutOne) f.getData();
                        String key = p.getKey();
                        byte[] value = p.getValue();
                        users_database.put(key, value);

                        break;
                    }
                    case 3: { //get
                        String key = (String) f.getData();
                        byte[] data = users_database.get(key);
                        con.send(new Frame(3,true,data));

                        break;
                    }
                    case 4: { //multiput
                        Map<String,byte[]> map = (Map<String, byte[]>) f.getData();
                        users_database.multiPut(map);

                        break;
                    }
                    case 5: { //multiget
                        Set<String> set = (Set<String>) f.getData();
                        Map<String,byte[]> map = users_database.multiGet(set);
                        con.send(new Frame(5,true,map));

                        break;
                    }
                    case 6: { //close
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
