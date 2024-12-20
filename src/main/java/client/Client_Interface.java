package client;

import java.io.IOException;
import java.util.*;
import menu.Menu;

public class Client_Interface {

    public boolean login=false;
    public boolean running=false;
    Scanner sc = new Scanner(System.in);

    Client client = new Client();

    Menu menu;

    public Client_Interface() throws IOException {
        clearTerminal();
        this.menu = new Menu(new String[]{
                "\nAUTHENTICATION OPTIONS\n",
                "Login",
                "Register",
                "Exit"
        });
        menu.setHandler(1, this::login);
        menu.setHandler(2,this::register);
        menu.setHandler(3, () -> System.exit(0));

        menu.execute();
    }

    public void login() throws IOException {
        clearTerminal();
        System.out.println("LOG IN \n");

        System.out.println("Username: ");
        String username = sc.nextLine();

        System.out.println("Password: ");
        String password = sc.nextLine();

        if(!client.login(username,password)){
            System.out.println("Invalid login");
            new Client_Interface();
        }

        this.clientMenu();
    }

    public void register() throws IOException {
        clearTerminal();
        System.out.println("REGISTER \n");

        System.out.println("Username: ");
        String username = sc.nextLine();

        System.out.println("Password: ");
        String password = sc.nextLine();

        System.out.println("Repeat password: ");
        String repassword = sc.nextLine();

        if(! password.equals(repassword)){
            System.out.println("Password not matching");
            new Client_Interface();
        }

        if(!client.register(username,password)){
            System.out.println("Username already exists");
            new Client_Interface();
        }

        this.clientMenu();
    }

    public void clientMenu() throws IOException {
        clearTerminal();
        this.menu = new Menu(new String[]{
                "\nCLIENT MENU\n",
                "Read data",
                "Store data",
                "Read multi data",
                "Store multi data",
                "Exit"
        });
        menu.setHandler(1, this::readData);
        menu.setHandler(2,this::storeData);
        menu.setHandler(3, this::readMultiData);
        menu.setHandler(4,this::storeMultiData);
        menu.setHandler(5, () -> System.exit(0));

        menu.execute();
    }

    public void readData() throws IOException {
        clearTerminal();
        System.out.println("READ DATA \n");

        System.out.println("Key: ");
        String key = sc.nextLine();

        byte[] data = client.get(key);

        if(data==null)
            System.out.println("The key " + key + " does not exist");
        //else
            //System.out.println("Data: "+ data);   ver uma forma de fazer parse aos dados que chegam

        pressAnyKey();
        clientMenu();
    }

    public void storeData() throws IOException {
        clearTerminal();
        System.out.println("STORE DATA \n");

        System.out.println("Key: ");
        String key = sc.nextLine();

        System.out.println("Data: ");
        String dataString = sc.nextLine();

        client.put(key, dataString.getBytes()); //verificar se essa função é a adequada

        System.out.println("Values stored successfully");

        pressAnyKey();
        clientMenu();

    }

    public void readMultiData() throws IOException {
        clearTerminal();
        System.out.println("READ MULTI DATA \n");

        Set<String> set = new HashSet<>();
        String input;

        while(true){
            System.out.println("Enter key or write exit to stop:");
            input = sc.nextLine();
            if(input.equals("exit"))
                break;
            else{
                set.add(input);
            }
        }

        Map<String, byte[]> m = client.multiGet(set);

        for (Map.Entry<String,byte[]> e: m.entrySet()){
            if(e.getValue()==null)
                System.out.println("The key " + e.getKey() + " does not exist");
            else
                System.out.println("Key: " + e.getKey() + "   Data: " + e.getValue());
        }

        pressAnyKey();
        clientMenu();
    }

    public void storeMultiData() throws IOException {
        clearTerminal();
        System.out.println("STORE MULTI DATA \n");

        Map<String, byte[]> pairs = new HashMap<>();
        String key, dataString;

        while(true){
            System.out.println("Enter key or write exit to stop:");
            key = sc.nextLine();
            if(key.equals("exit"))
                break;
            else{
                System.out.println("Enter data:");
                dataString = sc.nextLine();
                pairs.put(key, dataString.getBytes()); //igual ao de cima
            }
        }

        client.multiPut(pairs);

        System.out.println("Values stored successfully");

        pressAnyKey();
        clientMenu();
    }

    public void clearTerminal(){
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void pressAnyKey(){
        System.out.println("Press any key to continue");
        String x = sc.nextLine();
    }

}
