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
    public static void main(String []args) {
        Socket socket = startServer(Integer.parseInt(args[0]));
        // Read from client
        InputStream input = inputStream(socket);
        // Write output to client
        OutputStream output = outputStream(socket);

        String commandStr = args[1];
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
