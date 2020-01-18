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

    public static Socket startServer(int port) {
        try {
            ServerSocket server = new ServerSocket(port);
            Socket socket = server.accept();
            return socket;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static InputStream inputStream(Socket socket) {
        try {
            InputStream input = socket.getInputStream();
            return input;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static OutputStream outputStream(Socket socket) {
        try {
            OutputStream output = socket.getOutputStream();
            return output;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void usage() {
        System.out.println("Usage:");
        System.out.println("\tRemoteShell <port> <command>\n");
        System.out.println("\tport:\t\tListen port");
        System.out.println("\tcommand:\tCommand to execute when client");
        System.out.println("\t\t\tconnect, default: bash\n");
    }

    public static void main(String []args) {
        if (args.length < 1) {
            usage();
            return;
        }
        String commandStr = "bash";
        if (args.length > 1) {
            commandStr = args[1];
        }
        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port: " + args[0]);
            usage();
            return;
        }

        Socket socket = startServer(port);
        // Read from client
        InputStream input = inputStream(socket);
        // Write output to client
        OutputStream output = outputStream(socket);

        try{
            Process command = Runtime.getRuntime().exec(commandStr);
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
