import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;


public class ControllerSendQuery implements Runnable {
    boolean doesHaveConditions = false;
    String fileName;
    ArrayList<String> columns;
    String sendQueryTo;
    Integer IdVal;

    @Override
    public void run() {
        if (!doesHaveConditions) {
            try {
                Socket socket = new Socket(sendQueryTo, 10001);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                QueryObject queryObject = new QueryObject();
                queryObject.columns = columns;
                queryObject.fileName = fileName;
                queryObject.IdVal = IdVal;
                objectOutputStream.writeObject(queryObject);
                objectOutputStream.flush();
                System.out.println("Query object sent");

                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                StringBuffer stringBuffer = (StringBuffer) objectInputStream.readObject();

               /* while (Controller.othersAreWriting) {

                }*/
                Controller.othersAreWriting = true;
                Controller.finalOut.append(stringBuffer.toString());
                System.out.println(stringBuffer.toString());
                System.out.println("Output written");
                Controller.othersAreWriting = false;

                Controller.queryParsed.remove(0);
                System.out.println("removed from list");


            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
