package server;

import Connection.*;
import database.Users;
import database.Users_Database;

import java.net.SocketException;

class ClientHandler implements Runnable {
    private Connection con;
    private Users users;
    private Users_Database users_database;
    private Server server;

    public ClientHandler(Connection con, Users users, Users_Database users_database, Server server) {
        this.con = con;
        this.users = users;
        this.users_database = users_database;
        this.server = server;
    }

    public void run() {
        try {
            while (true) {
                Frame f = con.receive();
                int id = f.getId();
                if(f.getType()==FrameType.Close) {
                    con.send(new Frame(id, FrameType.Close,true,""));
                    con.close();
                    break;
                }
                server.addRequest(new Request(f, con, users, users_database));
            }
        } catch (SocketException e) {
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        server.clientDisconnected();
    }
}
