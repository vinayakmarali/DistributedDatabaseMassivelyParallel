import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ControllerToHandleQuery implements Runnable {
    static Integer sync = new Integer(0);

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(10000);
            while (true) {
                Socket socket = serverSocket.accept();
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                try {
                    String query = (String) objectInputStream.readObject();
                    System.out.println("Query accepted...");
                    ControllerQueryParse queryParse = new ControllerQueryParse();
                    queryParse.query = query;
                    queryParse.client = socket;
                    Thread queryProcessing = new Thread(queryParse);
                    queryProcessing.start();

                    Thread.sleep(1000);
                    synchronized (sync) {
                        System.out.println("Finished!!");
                        queryProcessing.stop();
                    }
/*
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    objectOutputStream.writeObject(Controller.finalOut);
                    objectOutputStream.flush();
                    Controller.finalOut = null;*/

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
