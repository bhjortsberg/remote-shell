import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class RemoteShell {

    private int port;
    private String commandStr = "bash";
    private Socket socket;

    public RemoteShell(String [] args) {
        if (args.length < 1) {
            usage();
            System.exit(1);;
        }
        if (args.length > 1) {
            commandStr = args[1];
        }
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port: " + args[0]);
            usage();
            System.exit(1);;
        }
    }

    private void usage() {
        System.out.println("Usage:");
        System.out.println("\tRemoteShell <port> <command>\n");
        System.out.println("\tport:\t\tListen port");
        System.out.println("\tcommand:\tCommand to execute when client");
        System.out.println("\t\t\tconnect, default: bash\n");
    }

    public void startServer() {
        try {
            ServerSocket server = new ServerSocket(port);
            socket = server.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public InputStream inputStream() {
        try {
            InputStream input = socket.getInputStream();
            return input;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public OutputStream outputStream() {
        try {
            OutputStream output = socket.getOutputStream();
            return output;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getCommand() {
        return commandStr;
    }

    public static void main(String []args) {

        RemoteShell shell = new RemoteShell(args);
        shell.startServer();

        // Read from client
        InputStream input = shell.inputStream();
        // Write output to client
        OutputStream output = shell.outputStream();

        try{
            Process command = Runtime.getRuntime().exec(shell.getCommand());
            // Reading output from Process
            BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(command.getInputStream()));
            // Write data to the Process
            OutputStream commandStream = command.getOutputStream();
            final BufferedWriter writer = new BufferedWriter(
                                            new OutputStreamWriter(commandStream));


            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] buffer = new byte[1024];
                    try {
                        int bytesRead;
                        while (command.isAlive() &&
                                ((bytesRead = input.read(buffer)) != -1))
                        {
                            buffer[bytesRead] = '\n';
                            if (command.isAlive()) {
                                writer.write(new String(buffer));
                                writer.flush();
                                // Sleep to make the command exit
                                // in case 'exit' was written
                                Thread.sleep(200);
                            } else {
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();

            String line = "";
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                line += "\n";
                output.write(line.getBytes());
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
