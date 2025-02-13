
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class AppChatServidor {
    private static final int PORT = 12345;
    private static ServerSocket serverSocket;
    private static Map<String, PrintWriter> clients = new HashMap<>();

    public static void main(String[] args) throws Exception {
        try{
            serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor aguardando conexão...");

            while(true){
                Socket clienteSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clienteSocket.getInetAddress());

                // Criação da nova thread para cada cliente conectado
                new Thread(new ClienteHandler(clienteSocket)).start();

            }
            
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // -----------------------------------------------------------------------------------
    private static class ClienteHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String clientName;

        public ClienteHandler(Socket socket){
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void run(){
                try {
                // Receber o nome do cliente
                out.println("Digite seu nome:");
                clientName = in.readLine();
                synchronized (clients) {
                    clients.put(clientName, out);
                }
                System.out.println(clientName + " entrou no chat.");

                // Enviar mensagem do cliente para o servidor
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/send")) {
                        // Comando para enviar mensagem para outro cliente
                        String[] parts = message.split(" ", 3);
                        if (parts.length == 3) {
                            String target = parts[1];
                            String msg = parts[2];
                            sendMessageToClient(target, msg);
                        }
                    } else {
                        System.out.println(clientName + ": " + message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Remover cliente da lista ao desconectar
                synchronized (clients) {
                    clients.remove(clientName);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendMessageToClient(String target, String message){
            PrintWriter targetOut = clients.get(target);
            if(targetOut != null){
                targetOut.println(clientName + " diz: " + message);
            }else{
                out.println("Usuário "+ target + "não encontrado.");
            }
        }
    }
}
