package server;

import Connection.*;
import client.User;
import database.Users;
import database.Users_Database;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
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
                    case 2: { //get
                        String key;
                        try {
                            key = in.readUTF();
                        } finally {
                            lr.unlock();
                        }

                        byte[] data = users_database.get(key);

                        ls.lock();
                        try {
                            out.writeInt(2);
                            out.writeInt(data.length);
                            out.write(data);
                            out.flush();
                        } finally {
                            lr.unlock();
                        }

                        break;
                    }
                    case 3: { //put
                        String key;
                        int len;
                        byte[] data;

                        try {
                            key = in.readUTF();
                            len
                                    data = in.readFully();
                        } finally {
                            lr.unlock();
                        }
                        String key = in.readUTF();
                        byte[] data = in.readFully();
                        users_database.put(key, data);

                        out.writeInt(3);
                        out.flush();
                        break;
                    }
                    case 4: { //multiread
                        int size = in.readInt();
                        Set<String> s = new HashSet<>();
                        for (int i = 0; i < size; i++)
                            s.add(in.readUTF());

                        Map<String, byte[]> m = users_database.multiGet(s);

                        out.writeUTF("readmulti");
                        out.writeInt(m.size());
                        for (Map.Entry<String, byte[]> e : m.entrySet()) {
                            out.writeUTF(e.getKey());
                            out.write(e.getValue());
                        }
                        out.flush();
                        break;
                    }
                    case 5: { //multiwrite
                        int size = in.readInt();
                        Map<String, byte[]> m = new HashMap<>();
                        for (int i = 0; i < size; i++) {
                            String key = in.readUTF();
                            byte[] data = in.readAllBytes();
                            m.put(key, data);
                        }

                        users_database.multiPut(m);

                        out.writeUTF("storemulti");
                        out.flush();
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

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.clientDisconnected();
    }
}
