package server;

import database.Users;
import database.Users_Database;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

class ClientHandler implements Runnable {
    private Socket socket;
    private Users users;
    private Users_Database users_database;

    private DataOutputStream out;
    private DataInputStream in;

    private Server server;

    public boolean running = true; //condição de paragem do handler
    public boolean login = false; //muda para true quando iniciar sessão
    //false ->mostra as opções de registar e de login , true -> mostra o resto das opções

    public ClientHandler(Socket socket, Users users, Users_Database users_database, Server server) throws IOException {
        this.socket = socket;
        this.users = users;
        this.users_database = users_database;
        this.server = server;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            int exit = 0;

            while (exit == 0) {
                int choice = in.readInt();
                switch (choice) {
                    case 0: { //login
                        String username = in.readUTF();
                        String password = in.readUTF();
                        if (users.login(username, password))
                            login = true;
                        out.writeUTF("login");
                        out.writeBoolean(login);
                        break;
                    }
                    case 1: { //register
                        String username = in.readUTF();
                        String password = in.readUTF();
                        if (users.register(username, password))
                            login = true;
                        out.writeUTF("register");
                        out.writeBoolean(login);
                        break;
                    }
                    case 2: { //read
                        String key = in.readUTF();
                        byte[] data = users_database.get(key);

                        out.writeUTF("read");
                        out.write(data);

                        break;
                    }
                    case 3: { //write
                        String key = in.readUTF();
                        byte[] data = in.readAllBytes();
                        users_database.put(key, data);

                        out.writeUTF("store");

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

                        break;
                    }
                    case 6: { //close
                        exit = 1;
                        break;
                    }
                }
                out.flush();
                server.clientDisconnected();
            }

        } catch (Exception e) {
            System.out.println(e);
        } finally {
            try {
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            server.clientDisconnected();
        }
    }
}
