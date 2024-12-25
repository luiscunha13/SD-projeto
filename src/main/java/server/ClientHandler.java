package server;

import Connection.*;
import database.Users;
import database.Users_Database;

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

    @Override
    public void run() {

        while (true) {
            try {
                Frame f = con.receive();
                System.out.println("recebi frame");
                int id = f.getId();
                if(f.getType()==FrameType.Close) {
                    con.send(new Frame(id, FrameType.Close,true,""));
                    con.close();
                    break;
                }
                server.addRequest(new Request(f, con, users, users_database));
                System.out.println("adicionei request ch");
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        server.clientDisconnected();
    }
}
