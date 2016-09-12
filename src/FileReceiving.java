import java.io.*;
import java.net.Socket;

public class FileReceiving {

    Socket socket;
    public String fileToReceive;
    int size = 21474800;

    public void receiveFile() {
        int bytesRead;
        int currentByte = 0;
        try {
            byte[] byteBufferArray = new byte[size];
            InputStream inputStream = socket.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(fileToReceive);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            bytesRead = inputStream.read(byteBufferArray, 0, byteBufferArray.length);
            currentByte = bytesRead;
            do {
                bytesRead =
                        inputStream.read(byteBufferArray, currentByte, (byteBufferArray.length - currentByte));
                if (bytesRead >= 0) currentByte += bytesRead;
            } while (bytesRead > -1);

            System.out.println("In here");
            bufferedOutputStream.write(byteBufferArray, 0, currentByte);
            bufferedOutputStream.flush();
            System.out.println("File received: " + fileToReceive
                    + " size : " + currentByte + " bytes ");
            bufferedOutputStream.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*byte[] mybytearray = new byte[size];
        InputStream is = null;
        try {
            is = socket.getInputStream();
            FileOutputStream fos = new FileOutputStream(fileToReceive);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            int bytesRead = is.read(mybytearray, 0, mybytearray.length);
            bos.write(mybytearray, 0, bytesRead);
            bos.close();
            //socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }
}