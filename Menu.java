import java.io.IOException;
import java.util.*;

public class Menu {

    public interface Handler {
        public void execute() throws IOException;
    }

    private List<String> options;
    private List<Handler> handlers;

    public Menu(String[] l){
        this.options = Arrays.asList(l);
        this.handlers = new ArrayList<>();
        this.options.forEach(s-> {
            this.handlers.add(() -> System.out.println("\nATENÇÃO: Opção não implementada!"));
        }); // fui buscar isto ao projeto de poo mas nem sei o que significa
    }

    public void execute() {
        int op=-1;
        while(op!=0){
            showMenu();
            op = readOption();

            if (op>0) {
                this.handlers.get(op-1).execute();
            }
        }
    }

    /** Apresentar o menu */
    private void showMenu() {
        for (int i=0; i<this.options.size(); i++) {
            if(i==0){
                System.out.println(this.options.get(i));
            }else{
                System.out.print("("+i+")");
                System.out.print(" - ");
                System.out.println(this.options.get(i));
            }

        }
    }


    private int readOption() {
        int op;
        Scanner is = new Scanner(System.in);

        System.out.print("Option: ");
        try {
            op = is.nextInt();
        }
        catch (InputMismatchException e) {
            op = -1;
        }
        if (op<0 || op>this.options.size()) {
            System.out.println("Invalid option");
            op = -1;
        }
        return op;
    }


    public void setHandler(int i, Handler h) {
        this.handlers.set(i-1, h);
    }

}
