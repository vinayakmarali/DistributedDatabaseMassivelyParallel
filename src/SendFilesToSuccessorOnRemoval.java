import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class SendFilesToSuccessorOnRemoval implements Runnable {
    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(5011);
            while (true) {
                Socket socket = serverSocket.accept();
                String path = "/home/stu4/s1/vkm7895/" + socket.getLocalAddress().getHostName() + "/";

                ArrayList<String> fileNames = new ArrayList<String>();
                ArrayList<StringBuffer> stringBuffers = new ArrayList<StringBuffer>();

                File file = new File(path);
                File[] files = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    StringBuffer stringBuffer = new StringBuffer();
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(path + files[i].getName()));
                    String line = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuffer.append(line + "\n");
                    }
                    fileNames.add(files[i].getName());
                    stringBuffers.add(stringBuffer);
                }

                deleteAllFiles(path);
                System.out.println("Deleted all files");
                System.out.println("Sending all files to successor");
                sendFilesToSuccessor(fileNames, stringBuffers);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject("Moved all files to successor...Node can be deleted!!");
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFilesToSuccessor(ArrayList<String> fileNames, ArrayList<StringBuffer> stringBuffers) {
        NameAndBufferObjects nameAndBufferObjects = new NameAndBufferObjects();
        nameAndBufferObjects.names = fileNames;
        nameAndBufferObjects.stringBuffers = stringBuffers;

        try {
            //receive adjusted files
            Socket socket = new Socket(Process.successor, 5010);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject("remove");
            objectOutputStream.flush();

            objectOutputStream.writeObject(nameAndBufferObjects);
            objectOutputStream.flush();
            System.out.println("Content of files sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteAllFiles(String path) {
        File[] files = new File(path).listFiles();
        for (File f : files) {
            f.delete();
        }
    }
}
