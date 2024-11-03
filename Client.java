import java.io.*;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

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
        int option = Options();

        
    }

    public static void main(String[] args){
        Client client = new Client();

        client.connectToServer();

    }


}


