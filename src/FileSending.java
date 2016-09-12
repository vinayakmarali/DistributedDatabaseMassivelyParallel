import java.io.*;
import java.net.Socket;

public class FileSending {
    Socket socket;
    String fileName;

    public void sendFile() {
        BufferedInputStream bufferedInputStream = null;
        FileInputStream fileInputStream = null;
        OutputStream outputStream = null;
        System.out.println("In here " + fileName);
        try {
            File file = new File(fileName);
            byte[] byteBufferArray = new byte[(int) file.length()];
            fileInputStream = new FileInputStream(file);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            bufferedInputStream.read(byteBufferArray, 0, byteBufferArray.length);
            outputStream = socket.getOutputStream();
            System.out.println("Sending file :" + fileName + " with size :" + byteBufferArray.length + " bytes");
            outputStream.write(byteBufferArray, 0, byteBufferArray.length);
            outputStream.flush();

            System.out.println("File sent successfully");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*File myFile = new File(fileName);
        byte[] mybytearray = new byte[(int) myFile.length()];
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(myFile));
            bis.read(mybytearray, 0, mybytearray.length);
            OutputStream os = socket.getOutputStream();
            os.write(mybytearray, 0, mybytearray.length);
            os.flush();
            //socket.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }
}


