import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class RemoveNode implements Runnable {
    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(5050);
            Socket socket = serverSocket.accept();
            System.out.println("Request received from controller to shut down....");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
