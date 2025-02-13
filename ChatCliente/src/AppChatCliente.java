import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class AppChatCliente extends JFrame {
    private JTextArea taChat;
    private JTextField tfMessage;
    private JTextField tfRecipient;
    private JButton btnSend;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String serverAdress = "10.74.241.66"; // Endereço do servidor
    private int port = 12345; // Porta do servidor

    public AppChatCliente() {
        // Configurações da janela
        setTitle("Chat Client");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        taChat = new JTextArea();
        taChat.setEditable(false);
        add(new JScrollPane(taChat), BorderLayout.CENTER);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Adicionando o label e o campo de texto para o destinatário
        JPanel recipientPanel = new JPanel();
        recipientPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel lblRecipient = new JLabel("Destinatário:");
        recipientPanel.add(lblRecipient);
        tfRecipient = new JTextField(15);
        recipientPanel.add(tfRecipient);
        panel.add(recipientPanel, BorderLayout.NORTH);

        // Adicionando o label e o campo de texto para a mensagem
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel lblMessage = new JLabel("Mensagem:");
        messagePanel.add(lblMessage);
        tfMessage = new JTextField(20);
        messagePanel.add(tfMessage);
        panel.add(messagePanel, BorderLayout.CENTER);

        // Botão de envio
        btnSend = new JButton("Enviar");
        panel.add(btnSend, BorderLayout.EAST);

        add(panel, BorderLayout.SOUTH);

        // Ação do botão de enviar
        btnSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // Conectar ao servidor
        connectToServer();

        // Iniciar a thread de recebimento de mensagens
        new Thread(new MessageReceiver()).start();
    }

    private void connectToServer() {
        try {
            // Selecionar o Servidor

            String iphost = JOptionPane.showInputDialog("Digite o ip do servidor:");
            serverAdress = iphost;
            socket = new Socket(serverAdress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Enviar o nome do cliente
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
            out.println("/send " + recipient + " " + message); // Envia a mensagem para o servidor
            taChat.append("Você (para " + recipient + "): " + message + "\n");
            tfMessage.setText("");
        }
    }

    private class MessageReceiver implements Runnable {
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    taChat.append(message + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new AppChatCliente().setVisible(true);
            }
        });
    }
}
