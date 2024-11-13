import java.io.IOException;
import java.util.*;

public class Client_Interface {

    public boolean login=false;
    public boolean running=false;
    Scanner sc = new Scanner(System.in);

    Client client = new Client();

    Menu menu;

    public Client_Interface(){
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

    public void clientMenu(){
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

        client.readData(key);
    }

    public void storeData() throws IOException {
        clearTerminal();
        System.out.println("STORE DATA \n");

        System.out.println("Key: ");
        String key = sc.nextLine();

        System.out.println("Data: ");
        String data = sc.nextLine();

        client.storeData(key, data);

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

        client.readMultiData(list);
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

        client.storeMultiData(pairs);
    }

    public void clearTerminal(){
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

}
