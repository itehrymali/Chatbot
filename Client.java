import java.awt.BorderLayout;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
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

public class Server extends JFrame {
    ServerSocket serverSocket;
    Socket socket;
    BufferedReader br;
    PrintWriter out;

    // GUI Components
    private JLabel heading = new JLabel("Server Area");
    private JTextArea messageArea = new JTextArea();
    private JTextField messageInput = new JTextField();
    private Font font = new Font("Roboto", Font.PLAIN, 20);

    // Constructor
    public Server() {
        try {
            serverSocket = new ServerSocket(7777);
            System.out.println("Server is ready to accept connection.");
            System.out.println("Waiting for client...");
            socket = serverSocket.accept(); // Wait for a client to connect
            System.out.println("Connection established with client.");

            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());

            createGUI(); // Setup the GUI
            handleEvents(); // Handle user input events

            startReading(); // Start reading messages from the client

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createGUI() {
        // Set frame properties
        this.setTitle("Server Messenger");
        this.setSize(600, 600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set up components
        heading.setFont(font);
        messageArea.setFont(font);
        messageInput.setFont(font);

        heading.setHorizontalAlignment(SwingConstants.CENTER);
        heading.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        messageArea.setEditable(false); // Prevent editing of received messages

        // Layout setup
        this.setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(messageArea); // Make messageArea scrollable
        this.add(heading, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
        this.add(messageInput, BorderLayout.SOUTH);

        this.setVisible(true); // Display the GUI
    }

    private void handleEvents() {
        // Listen for Enter key in the input field
        messageInput.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) { // Check if Enter key is pressed
                    String contentToSend = messageInput.getText();
                    messageArea.append("Me: " + contentToSend + "\n"); // Display in message area
                    out.println(contentToSend); // Send to client
                    out.flush();
                    messageInput.setText(""); // Clear the input field
                }
            }
        });
    }

    public void startReading() {
        // Thread to read data from the client
        Runnable r1 = () -> {
            System.out.println("Reader started...");
            try {
                while (true) {
                    String msg = br.readLine();
                    if (msg == null || msg.equals("exit")) { // Exit condition
                        System.out.println("Client terminated the chat.");
                        JOptionPane.showMessageDialog(this, "Client terminated the chat.");
                        messageInput.setEnabled(false); // Disable input field
                        socket.close();
                        break;
                    }
                    // Update the message area on the Event Dispatch Thread
                    SwingUtilities.invokeLater(() -> {
                        messageArea.append("Client: " + msg + "\n");
                    });
                }
            } catch (Exception e) {
                System.out.println("Connection is closed.");
            }
        };
        new Thread(r1).start();
    }

    public static void main(String[] args) {
        System.out.println("This is Server... Starting Server.");
        new Server();
    }
}
