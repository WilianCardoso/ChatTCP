import java.io.*;
import java.net.*;
import java.util.*;

public class AppChatServidor {
    private static final int PORT = 12345;
    private static ServerSocket serverSocket;
    private static Map<String, PrintWriter> clients = new HashMap<>();

    public static void main(String[] args) throws Exception {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor aguardando conexões...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClienteHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClienteHandler implements Runnable {
        private BufferedReader in;
        private PrintWriter out;
        private String clientName;

        public ClienteHandler(Socket socket) {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                clientName = in.readLine();
                synchronized (clients) {
                    clients.put(clientName, out);
                    broadcastUserList();
                    broadcastMessage("[Servidor]: " + clientName + " entrou no chat.");
                }

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/send ")) {
                        String[] parts = message.split(" ", 3);
                        if (parts.length == 3) {
                            sendMessageToClient(parts[1], parts[2]);
                        }
                    } else {
                        broadcastMessage(clientName + ": " + message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                synchronized (clients) {
                    clients.remove(clientName);
                    broadcastUserList();
                    broadcastMessage("[Servidor]: " + clientName + " saiu do chat.");
                }
            }
        }

        private void sendMessageToClient(String target, String message) {
            PrintWriter targetOut = clients.get(target);
            if (targetOut != null) {
                targetOut.println(clientName + " diz: " + message);
            } else {
                out.println("Usuário " + target + " não encontrado.");
            }
        }

        private void broadcastMessage(String message) {
            synchronized (clients) {
                for (PrintWriter clientOut : clients.values()) {
                    clientOut.println(message);
                }
            }
        }

        private void broadcastUserList() {
            synchronized (clients) {
                String userList = "/users " + String.join(",", clients.keySet());
                for (PrintWriter clientOut : clients.values()) {
                    clientOut.println(userList);
                }
            }
        }
    }
}
