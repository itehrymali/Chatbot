import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class Client extends JFrame {
    Socket socket;
    BufferedReader br;
    PrintWriter out;

    // GUI components
    private JLabel heading = new JLabel("Client Area");
    private JTextArea messageArea = new JTextArea();
    private JTextField messageInput = new JTextField();
    private Font font = new Font("Roboto", Font.PLAIN, 20);

    // Constructor
    public Client() {
        try {
            System.out.println("Sending request to server...");
            socket = new Socket("127.0.0.1", 7777);
            System.out.println("Connection established with server.");

            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());

            createGUI(); // Setup GUI
            handleEvents(); // Handle user input events

            startReading(); // Start reading messages from the server

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleEvents() {
        messageInput.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                // Not needed
            }

            @Override
            public void keyPressed(KeyEvent e) {
                // Not needed
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String contentToSend = messageInput.getText();
                    messageArea.append("Me: " + contentToSend + "\n");
                    out.println(contentToSend);
                    out.flush();
                    messageInput.setText(""); // Clear the input field
                }
            }
        });
    }

    private void createGUI() {
        // Set frame properties
        this.setTitle("Client Messenger");
        this.setSize(600, 600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set up components
        heading.setFont(font);
        messageArea.setFont(font);
        messageInput.setFont(font);

        heading.setHorizontalAlignment(SwingConstants.CENTER);
        heading.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        messageArea.setEditable(false);

        // Set layout and add components
        this.setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(messageArea);
        this.add(heading, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
        this.add(messageInput, BorderLayout.SOUTH);

        this.setVisible(true);
    }

    // Start reading messages from the server
    public void startReading() {
        Runnable r1 = () -> {
            System.out.println("Reader started...");
            try {
                while (true) {
                    String msg = br.readLine();
                    if (msg == null || msg.equals("exit")) {
                        System.out.println("Server terminated the chat.");
                        JOptionPane.showMessageDialog(this, "Server terminated the chat.");
                        messageInput.setEnabled(false);
                        socket.close();
                        break;
                    }
                    SwingUtilities.invokeLater(() -> {
                        messageArea.append("Server: " + msg + "\n");
                    });
                }
            } catch (IOException e) {
                System.out.println("Connection is closed.");
            }
        };
        new Thread(r1).start();
    }

    public static void main(String[] args) {
        System.out.println("This is Client... Starting Client.");
        new Client();
    }
}
