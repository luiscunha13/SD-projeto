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

    public void readData(){
        clearTerminal();
        System.out.println("READ DATA \n");

        System.out.println("Key: ");
        String key = sc.nextLine();
    }

    public void storeData(){
        clearTerminal();
        System.out.println("STORE DATA \n");

        System.out.println("Key: ");
        String key = sc.nextLine();

        System.out.println("Data: ");
        String data = sc.nextLine();


    }

    public void readMultiData(){

    }

    public void storeMultiData(){

    }

    public void clearTerminal(){
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void OptionsHandler(){
        String input, input1, input2;
        boolean exit=false;
        byte[] bytes=null;

        while(running){
            int option = Options();

                switch(option){
                    case 1: // store data
                        System.out.println("Enter key:");
                        input = sc.nextLine();
                        System.out.println("Enter data:");
                        input1 = sc.nextLine();
                        bytes = input1.getBytes();
                        //...
                        break;
                    case 2: // read data
                        System.out.println("Enter key:");
                        input = sc.nextLine();
                        //...
                        break;
                    case 3: // store multi data
                        Map<String, byte[]> pairs = new HashMap<>();
                        while(!exit){
                            System.out.println("Enter key or write exit to stop:");
                            input = sc.nextLine();
                            if(input.equals("exit"))
                                exit = true;
                            else{
                                System.out.println("Enter data:");
                                input1 = sc.nextLine();
                                bytes = input1.getBytes();
                                pairs.put(input,bytes);
                            }
                        }
                        //...
                        break;
                    case 4: // read multi data
                        Set<String> list = new HashSet<>();
                        System.out.println("Enter key or write exit to stop:");

                        while(!exit){
                            System.out.println("Enter key :");
                            input = sc.nextLine();
                            if(input.equals("exit"))
                                exit = true;
                            else{
                                list.add(input);
                            }
                        }

                        //...
                        break;
                    case 5: // exit
                        running=false;
                    default:
                        break;

                }



        }



    }
}
