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

        String data = client.readData(key);

        if(data.equals("null"))
            System.out.println("The key " + key + " does not exist");
        else
            System.out.println("Data: "+ data);

        pressAnyKey();
        clientMenu();
    }

    public void storeData() throws IOException {
        clearTerminal();
        System.out.println("STORE DATA \n");

        System.out.println("Key: ");
        String key = sc.nextLine();

        System.out.println("Data: ");
        String data = sc.nextLine();

        boolean b = client.storeData(key, data);

        if(b)
            System.out.println("Values stored successfully");

        pressAnyKey();
        clientMenu();

    }

    public void readMultiData() throws IOException {
        clearTerminal();
        System.out.println("READ MULTI DATA \n");

        Set<String> list = new HashSet<>();
        String input;

        while(true){
            System.out.println("Enter key or write exit to stop:");
            input = sc.nextLine();
            if(input.equals("exit"))
                break;
            else{
                list.add(input);
            }
        }

        Map<String, String> m = client.readMultiData(list);

        for (Map.Entry<String,String> e: m.entrySet()){
            if(e.getValue().equals("null"))
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

        Map<String, String> pairs = new HashMap<>();
        String key, data;

        while(true){
            System.out.println("Enter key or write exit to stop:");
            key = sc.nextLine();
            if(key.equals("exit"))
                break;
            else{
                System.out.println("Enter data:");
                data = sc.nextLine();
                pairs.put(key,data);
            }
        }

        boolean b = client.storeMultiData(pairs);

        if(b)
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
