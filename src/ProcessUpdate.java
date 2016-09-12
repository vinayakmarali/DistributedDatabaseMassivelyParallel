import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class ProcessUpdate implements Runnable {
    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(5001);
            while (true) {
                System.out.println("Waiting for controller's command");
                Socket socket = serverSocket.accept();
                System.out.println("Connection accepted to update");
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                NodeDetails nodeDetails = (NodeDetails) objectInputStream.readObject();

                //setting values to process at startup or during new node addition
                Process.ID = nodeDetails.nodeID;
                Process.myName = nodeDetails.nodeName;
                Process.minRange = nodeDetails.range.minRange;
                Process.maxRange = nodeDetails.range.maxRange;
                Process.successor = nodeDetails.successor;
                Process.predecessor = nodeDetails.predecessor;
                Process.limit = nodeDetails.limit;
                Process.printServerDetails();
                if (nodeDetails.isNew && !(Process.myName.equals(Process.successor) && Process.myName.equals(Process.predecessor))) {
                    System.out.println("New process....");
                    System.out.println("Asking successor to send records..." + Process.successor);
                    Socket requestFiles = new Socket(Process.successor, 5003);

                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(requestFiles.getOutputStream());
                    objectOutputStream.writeObject("sendMeMyRecords");
                    System.out.println("Request to get data sent to " + Process.successor);
                }
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
