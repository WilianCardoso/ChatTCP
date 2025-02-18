import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class AppChatCliente extends JFrame {
    private JTextArea taChat;
    private JTextArea taUsers;
    private JTextField tfMessage;
    private JTextField tfRecipient;
    private JButton btnSend;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String serverAdress;
    private int port = 12345;

    public AppChatCliente() {
        setTitle("Chat Client");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Área de chat
        taChat = new JTextArea();
        taChat.setEditable(false);
        add(new JScrollPane(taChat), BorderLayout.CENTER);

        // Área de usuários online
        taUsers = new JTextArea();
        taUsers.setEditable(false);
        taUsers.setPreferredSize(new Dimension(150, 0)); // Define largura fixa
        add(new JScrollPane(taUsers), BorderLayout.EAST);

        // Painel inferior para mensagens
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Campo para destinatário
        JPanel recipientPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        recipientPanel.add(new JLabel("Para:"));
        tfRecipient = new JTextField(10);
        recipientPanel.add(tfRecipient);
        panel.add(recipientPanel, BorderLayout.NORTH);

        // Campo para mensagem
        JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        messagePanel.add(new JLabel("Mensagem:"));
        tfMessage = new JTextField(20);
        messagePanel.add(tfMessage);
        panel.add(messagePanel, BorderLayout.CENTER);

        // Botão de envio
        btnSend = new JButton("Enviar");
        btnSend.addActionListener(e -> sendMessage());
        panel.add(btnSend, BorderLayout.EAST);

        add(panel, BorderLayout.SOUTH);

        // Conectar ao servidor
        connectToServer();

        // Iniciar a thread de recebimento de mensagens
        new Thread(new MessageReceiver()).start();
    }

    private void connectToServer() {
        try {
            serverAdress = JOptionPane.showInputDialog("Digite o IP do servidor:");
            socket = new Socket(serverAdress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Enviar nome do usuário
            String name = JOptionPane.showInputDialog("Digite seu nome:");
            setTitle("Chat Client - " + name);
            out.println(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String recipient = tfRecipient.getText();
        String message = tfMessage.getText();
        if (!message.isEmpty() && !recipient.isEmpty()) {
            out.println("/send " + recipient + " " + message);
            taChat.append("Você (para " + recipient + "): " + message + "\n");
            tfMessage.setText("");
        }
    }

    private class MessageReceiver implements Runnable {
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/users ")) {
                        updateUserList(message.substring(7));
                    } else {
                        taChat.append(message + "\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateUserList(String users) {
        SwingUtilities.invokeLater(() -> {
            taUsers.setText(users.replace(",", "\n"));
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AppChatCliente().setVisible(true));
    }
}