import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static List<ClientHandler> clients = new ArrayList<>();
    private static ServerSocket serverSocket;
    
    public static void main(String[] args) throws IOException {
        serverSocket = new ServerSocket(1234);
        System.out.println("Server started on port 1234");
        
        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client connected: " + clientSocket);
            
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            clients.add(clientHandler);
            clientHandler.start();
        }
    }
    
    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
        
        System.out.println("Broadcasting: " + message); // Log the broadcasted message in server console
    }
    
    public static void sendPrivateMessage(String message, ClientHandler sender, String recipient) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equalsIgnoreCase(recipient)) {
                client.sendMessage("[Private] " + sender.getUsername() + ": " + message);
                sender.sendMessage("[Private] You to " + recipient + ": " + message);
                return;
            }
        }
        
        sender.sendMessage("User " + recipient + " not found or not currently connected.");
    }
    
    public static void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        System.out.println("Client disconnected: " + clientHandler.getUsername()); // Log client disconnection in server console
    }

    public static List<ClientHandler> getClients() {
        return clients;
    }

    public static void stopServer() throws IOException {
        serverSocket.close();
    }
}

class ClientHandler extends Thread {
    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;
    private boolean isFirstMessage = true;

    public ClientHandler(Socket socket) throws IOException {
        clientSocket = socket;
        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        writer = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    public void run() {
        try {
            writer.println("Welcome to the chat server!");

            do {
                if (isFirstMessage) {
                    writer.println("Please enter your username:");
                    isFirstMessage = false;
                } else {
                    writer.println("Enter your message:");
                }

                String clientMessage = reader.readLine();

                if (username == null) {
                    username = clientMessage;
                    Server.broadcast(username + " has joined the chat!", this);
                    System.out.println("New client connected: " + username); // Print client name in server console
                } else {
                    if (clientMessage.equalsIgnoreCase("logout")) {
                        break;
                    } else if (clientMessage.toLowerCase().startsWith("@")) {
                        processPrivateMessage(clientMessage);
                    } else if (clientMessage.equalsIgnoreCase("users")) {
                        sendConnectedUsers();
                    } else {
                        Server.broadcast(username + ": " + clientMessage, this);
                    }
                }
            } while (true);

            Server.removeClient(this);
            clientSocket.close();
            Server.broadcast(username + " has left the chat.", this);
            System.out.println("Client disconnected: " + username); // Print client name in server console
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        writer.println(message);
    }

    public String getUsername() {
        return username;
    }

    private void sendConnectedUsers() {
        List<ClientHandler> clients = Server.getClients();
        StringBuilder users = new StringBuilder("Connected users:\n");
        for (ClientHandler client : clients) {
            users.append(client.getUsername()).append("\n");
        }
        writer.println(users.toString());
    }

    private void processPrivateMessage(String message) {
        int spaceIndex = message.indexOf(" ");
        if (spaceIndex != -1) {
            String recipient = message.substring(1, spaceIndex);
            String privateMessage = message.substring(spaceIndex + 1);
            Server.sendPrivateMessage(privateMessage, this, recipient);
        } else {
            writer.println("Invalid private message format. Please use '@username message'.");
        }
    }
}
