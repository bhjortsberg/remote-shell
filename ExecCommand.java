import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Scanner;

public class ExecCommand {

    public static void main(String []args) {
        StringBuffer output = new StringBuffer();
        String commandStr = args[0];
        try{
            Process command = Runtime.getRuntime().exec(commandStr);
            // Reading output from Process
            BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(command.getInputStream()));
            // Write data to the Process
            OutputStream instream = command.getOutputStream();
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(instream));

            Scanner scan = new Scanner(System.in);

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (command.isAlive()) {
                        String input = scan.nextLine();
                        input += "\n";
                        if (command.isAlive()) {
                            try {
                                writer.write(input);
                                writer.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    System.out.println("Returning, close thread");
                }
            });
            thread.start();

            String line = "";
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            command.waitFor();
            thread.join();
            System.out.println("program exit");
        }
        catch(IOException e){
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
