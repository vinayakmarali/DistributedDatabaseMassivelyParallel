import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

public class ControllerHeaderMapSend implements Runnable {
    int port = 8000;

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();

                System.out.println("Printing headers before sending:");
                for (Map.Entry<String, ArrayList<String>> e : Process.fileHeaders.entrySet()) {
                    System.out.println("File name: " + e.getKey());
                    ArrayList<String> temp = e.getValue();
                    for (int i = 0; i < temp.size(); i++) {
                        System.out.println("Header : " + temp.get(i));
                    }
                }

                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(Controller.fileHeaders);
                objectOutputStream.flush();
                System.out.println("Header details sent to server");

                objectOutputStream.writeObject(Controller.primaryKey);
                objectOutputStream.flush();
                System.out.println("Primary keys sent");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
