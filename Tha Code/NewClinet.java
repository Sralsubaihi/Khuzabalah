package network;

/**
 *
 * @author Srals
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NewClinet implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    
    public boolean hasAnswered = false;
    public long answerTime; // <-- لتسجيل وقت الإجابة


    public NewClinet(Socket socket) {
        this.socket = socket;
        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // أول رسالة يستقبلها هي اسم المستخدم
            this.username = in.readLine();
            System.out.println("New client username: " + username);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }

    public void sendMessage(String message) {
        out.println(message);
    }





    @Override
public void run() {
    try {
        String message;
        while ((message = in.readLine()) != null) {
            System.out.println("Received from " + username + ": " + message);

            if (message.equals("play")) {
                NewServer.handlePlayRequest(this);
            } else if (message.startsWith("ANSWER:")) {
                NewServer.handleAnswer(this, message.substring(7).trim());
            } else if (message.equals("LEAVE")) { 
                break; // اللاعب يريد المغادرة
            }
        }
    } catch (IOException e) {
        System.out.println("Client " + username + " disconnected.");
    } finally {
        NewServer.removePlayer(this);
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}}
