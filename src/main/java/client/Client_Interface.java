package client;

import java.io.IOException;
import java.util.*;

import Connection.Frame;
import Connection.FrameType;
import menu.Menu;

public class Client_Interface {

    Scanner sc = new Scanner(System.in);
    Client client;
    Menu menu;

    public Client_Interface(Client c) throws IOException, InterruptedException {
        this.client = c;
        authenticationMenu();
    }

    public void authenticationMenu() throws IOException, InterruptedException {
        //clearTerminal();
        this.menu = new Menu(new String[]{
                "AUTHENTICATION OPTIONS\n",
                "Login",
                "Register",
                "Exit"
        });
        menu.setHandler(1, this::login);
        menu.setHandler(2, this::register);
        menu.setHandler(3, this::exit);

        menu.execute();
    }

    public void login() throws IOException, InterruptedException {
        clearTerminal();
        System.out.println("LOG IN \n");

        System.out.println("Username: ");
        String username = sc.nextLine();

        System.out.println("Password: ");
        String password = sc.nextLine();

        if(!client.login(username,password)){
            System.out.println("Invalid login");
            pressAnyKey();
            authenticationMenu();
        }

        this.clientMenu();
    }

    public void register() throws IOException, InterruptedException {
        clearTerminal();
        System.out.println("REGISTER \n");

        System.out.print("Username: ");
        String username = sc.nextLine();

        System.out.print("Password: ");
        String password = sc.nextLine();

        System.out.print("Repeat password: ");
        String repassword = sc.nextLine();

        if(!password.equals(repassword)){
            System.out.println("\nPassword not matching");
            pressAnyKey();
            authenticationMenu();
        }

        if(!client.register(username,password)){
            System.out.println("\nUsername already exists");
            pressAnyKey();
            authenticationMenu();
        }

        System.out.println("\nRegistered successfully");
        pressAnyKey();

        this.clientMenu();
    }

    public void clientMenu() throws IOException, InterruptedException {
        clearTerminal();
        this.menu = new Menu(new String[]{
                "CLIENT MENU\n",
                "Read data",
                "Store data",
                "Read multi data",
                "Store multi data",
                "GetWhen",
                "Read replies",
                "Exit"
        });
        menu.setHandler(1, this::readData);
        menu.setHandler(2, this::storeData);
        menu.setHandler(3, this::readMultiData);
        menu.setHandler(4, this::storeMultiData);
        menu.setHandler(5, this::getWhen);
        menu.setHandler(6, this::readReplies);
        menu.setHandler(7, this::exit);

        menu.execute();
    }

    public void readData() throws IOException, InterruptedException {
        clearTerminal();
        System.out.println("READ DATA \n");

        System.out.print("Key: ");
        String key = sc.nextLine();

        int id = client.get(key);

        System.out.println("\nRequest id: ["+id +"]");


        pressAnyKey();
        clientMenu();
    }

    public void storeData() throws IOException, InterruptedException {
        clearTerminal();
        System.out.println("STORE DATA \n");

        System.out.print("Key: ");
        String key = sc.nextLine();

        System.out.print("Data: ");
        String dataString = sc.nextLine();

        client.put(key, dataString.getBytes());

        System.out.println("\nValues stored successfully");

        pressAnyKey();
        clientMenu();
    }

    public void readMultiData() throws IOException, InterruptedException {
        clearTerminal();
        System.out.println("READ MULTI DATA \n");

        Set<String> set = new HashSet<>();
        String input;
        System.out.println("Enter key or write exit to stop:");

        while(true){
            System.out.print("Key: ");
            input = sc.nextLine();
            if(input.equals("exit"))
                break;
            else{
                set.add(input);
            }
        }

        int id = client.multiGet(set);

        System.out.println("\nRequest id: ["+id +"]");

        pressAnyKey();
        clientMenu();
    }

    public void storeMultiData() throws IOException, InterruptedException {
        clearTerminal();
        System.out.println("STORE MULTI DATA \n");

        Map<String, byte[]> pairs = new HashMap<>();
        String key, dataString;
        System.out.println("Enter key or write exit to stop:");

        while(true){
            System.out.print("Key: ");
            key = sc.nextLine();
            if(key.equals("exit"))
                break;
            else{
                System.out.print("Data: ");
                dataString = sc.nextLine();
                pairs.put(key, dataString.getBytes());
            }
        }

        client.multiPut(pairs);

        System.out.println("\nValues stored successfully");

        pressAnyKey();
        clientMenu();
    }

    public void getWhen() throws IOException, InterruptedException {
        clearTerminal();
        System.out.println("GETWHEN \n");

        System.out.print("Key: ");
        String key = sc.nextLine();

        System.out.print("KeyCond: ");
        String keyCond = sc.nextLine();

        System.out.print("ValueCond: ");
        String valuecond = sc.nextLine();

        int id = client.getWhen(key, keyCond, valuecond.getBytes());

        System.out.println("\nRequest id: ["+id +"]");

        pressAnyKey();
        clientMenu();
    }

    public void readReplies() throws IOException, InterruptedException {
        List<Frame> l = client.getRepliesToPrint();

        clearTerminal();
        System.out.println("READ REPLIES \n");

        for(Frame f: l)
            printReply(f);

        pressAnyKey();
        clientMenu();
    }

    public void printReply(Frame f){

        System.out.println("["+f.getId()+"]");

        if(f.getType()== FrameType.Get){
            byte[] data = (byte[]) f.getData();

            if(data==null)
                System.out.println("The key does not exist");
            else {
                String dataS = new String(data);
                System.out.println("Data: " + dataS);
            }
        }
        else if(f.getType()== FrameType.MultiGet){
            Map<String, byte[]> m = (Map<String, byte[]>) f.getData();
            String aux;
            System.out.println();

            for (Map.Entry<String,byte[]> e: m.entrySet()) {
                aux = new String(e.getValue());
                if (aux.equals("null"))
                    System.out.println("The key does not exist");
                else
                    System.out.println("Key: " + e.getKey() + "   Data: " + aux);
            }
        }
        else {
            byte[] data = (byte[]) f.getData();
            String dataS = new String(data);
            System.out.println("Data: " + dataS);
        }

        System.out.println();
    }

    public void exit() throws IOException, InterruptedException {
        client.exit();
        System.exit(0);
    }

    public void clearTerminal(){
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void pressAnyKey(){
        System.out.println("\nPress any key to continue");
        String x = sc.nextLine();
    }

}
