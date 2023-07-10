import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 1234);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        
        System.out.println(reader.readLine()); // Welcome message
        
        System.out.print("Please Enter your username: ");
        String username = consoleReader.readLine();
        writer.println(username);
        
        System.out.println("You have joined the chat,to exit please Enter 'logout'.");
        
        Thread receiverThread = new Thread(() -> {
            try {
                String serverMessage;
                while ((serverMessage = reader.readLine()) != null) {
                    System.out.println(serverMessage);
                }
                socket.close();
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        receiverThread.start();
        
        String clientMessage;
        do {
            clientMessage = consoleReader.readLine();
            writer.println(clientMessage);
        } while (!clientMessage.equalsIgnoreCase("logout"));
        
        receiverThread.interrupt();
        socket.close();
        System.exit(0);
    }
}
