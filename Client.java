import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 9090;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public boolean running=true; //condição de paragem do handler
    public boolean login=false;

    private void connectToServer(){
        try{
            socket = new Socket("localhost",6666);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public int Options(){

        if(login){
            System.out.println("1: Login");
            System.out.println("2: Register");
            System.out.println("3: Exit");
        }
        else{
            System.out.println("1: Store data");
            System.out.println("2: Read data");
            System.out.println("3: Store multi data");
            System.out.println("4: Read multi data");
            System.out.println("5: Exit");

        }
        int op=-1;
        Scanner is = new Scanner(System.in);

        while(op==-1){
            System.out.print("Option: ");
            try {
                op = is.nextInt();
            }
            catch (InputMismatchException e) {
                op = -1;
            }
            if (op<0 || (login && op>5) || (!login && op>3)) {
                System.out.println("Invalid option");
                op = -1;
            }
        }

        return op;
    }

    public void OptionsHandler(){
        Scanner scanner = new Scanner(System.in);
        String input, input1, input2;
        boolean exit=false;
        byte[] bytes=null;

        while(running){
            int option = Options();

            if(!login){
                switch(option){
                    case 1: // login
                        System.out.println("Enter username:");
                        input = scanner.nextLine();
                        System.out.println("Enter password:");
                        input1 = scanner.nextLine();

                        //...
                        break;
                    case 2: // register
                        System.out.println("Enter username:");
                        input = scanner.nextLine();
                        System.out.println("Enter password:");
                        input1 = scanner.nextLine();
                        System.out.println("Confirm password:");
                        input2 = scanner.nextLine();

                        if(input1.equals(input2)){
                            //meter no pacote
                        }
                        else{
                            System.out.println("Password not confirmed...");
                        }
                        //...
                        break;
                    case 3: // exit
                        running=false;
                    default:
                        break;
                }
            }
            else{
                switch(option){
                    case 1: // store data
                        System.out.println("Enter key:");
                        input = scanner.nextLine();
                        System.out.println("Enter data:");
                        input1 = scanner.nextLine();
                        bytes = input1.getBytes(StandardCharsets.UTF_8); // não sei se é bem assim que eles querem
                        //...
                        break;
                    case 2: // read data
                        System.out.println("Enter key:");
                        input = scanner.nextLine();
                        //...
                        break;
                    case 3: // store multi data
                        Map<String, byte[]> pairs = new HashMap<>();
                        while(!exit){
                            System.out.println("Enter key or write exit to stop:");
                            input = scanner.nextLine();
                            if(input.equals("exit"))
                                exit = true;
                            else{
                                System.out.println("Enter data:");
                                input1 = scanner.nextLine();
                                bytes = input1.getBytes(StandardCharsets.UTF_8);
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
                            input = scanner.nextLine();
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

    public static void main(String[] args){
        Client client = new Client();

        client.connectToServer();

    }


}


