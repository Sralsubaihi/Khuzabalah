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



public class ServerConnection implements Runnable{
    private Socket server;
    private BufferedReader in;
    private PrintWriter out;
      
    public ServerConnection (Socket s) throws IOException{
        server=s;
        in= new BufferedReader (new InputStreamReader(server.getInputStream())); 
    out=new PrintWriter(server.getOutputStream(),true); 
    }

    public void run(){
        
            String serverResponse;
            try {
                while(true){
                serverResponse = in.readLine(); 
                if (serverResponse==null) break;
                System.out.println("Server says: "+serverResponse);
                }
                
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally{
                try {
                    in.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }     
    }           
}
