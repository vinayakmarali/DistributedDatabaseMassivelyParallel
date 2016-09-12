import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class ProcessReceiveFile implements Runnable {
    static int size = 21474800;

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(5002);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Ready to accept file");
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                String name = (String) objectInputStream.readObject();
                if (!Process.files.contains(name)) {
                    Process.files.add(name);
                }
                String hostName = socket.getLocalAddress().getHostName();
                String path = "/home/stu4/s1/vkm7895/" + hostName + "/";

                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append((String) objectInputStream.readObject());

                System.out.println("Content received");
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(path + name)));
                bufferedWriter.write(stringBuffer.toString());
                bufferedWriter.flush();
                bufferedWriter.close();

               /* FileReceiving fileReceiving = new FileReceiving();
                fileReceiving.socket = socket;
                System.out.println("Receiving: " + path + name);
                fileReceiving.fileToReceive = path + name;

                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject("OK");
                objectOutputStream.flush();

                System.out.println("Calling receiving method");
                fileReceiving.receiveFile();
                System.out.println("File received!!");

                // ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject("received");
                objectOutputStream.flush();*/
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
