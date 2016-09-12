import java.io.*;
import java.net.Socket;
import java.util.Scanner;


public class ControllerClient {
    static String controller = "newyork.cs.rit.edu";
    static int controllerInitPort = 5000;
    static int controllerQueryPort = 10000;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter operation:");
        System.out.println("1) Add Node");
        System.out.println("2) Remove Node");
        System.out.println("3) Add File");
        System.out.println("4) Query");
        int operation = scanner.nextInt();
        switch (operation) {
            case 1:
                System.out.println("Enter server name : ");
                scanner = new Scanner(System.in);
                String serverName = scanner.nextLine();
                serverName = serverName + ".cs.rit.edu";
                System.out.println("Enter range");
                int range = scanner.nextInt();
                serverName = serverName + ":" + range;
                sendInitializationRequestToController(serverName);
                break;

            case 2:
                System.out.println("Enter server name to remove : ");
                scanner = new Scanner(System.in);
                String serverNameToDelete = scanner.nextLine();
                serverNameToDelete = serverNameToDelete + ".cs.rit.edu";
                System.out.println("Sending request to controller");
                removeServer(serverNameToDelete);
                break;

            case 3:
                scanner = new Scanner(System.in);
                System.out.println("Enter file name (csv) to upload: ");
                String path = "/Users/vinayakmarali/Downloads/DS-TermProject-5/";
                path = path + scanner.nextLine();
                String ID = scanner.nextLine();
                sendFile(path, ID);
                break;

            case 4:
                scanner = new Scanner(System.in);
                System.out.println("Query format is:");
                System.out.println("select <*/column_name> from <file_name> where <column_name> '=/>/</like/between' <condition>");
                System.out.println("Enter query:");
                String query = scanner.nextLine();
                sendQuery(query);
                break;
        }
    }

    private static void sendQuery(String query) {
        try {
            Socket socket = new Socket(controller, controllerQueryPort);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(query);
            objectOutputStream.flush();

            System.out.println("Query sent to server....wait for response");

            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            StringBuffer stringBuffer = (StringBuffer) objectInputStream.readObject();
            //System.out.println(stringBuffer.toString());

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File("/Users/vinayakmarali/Downloads/DS-TermProject-5/download/out.csv")));

            //write contents of StringBuffer to a file
            bufferedWriter.write(stringBuffer.toString());
            bufferedWriter.close();


            System.out.println("response received");


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void removeServer(String serverNameToDelete) {
        try {
            Socket socket = new Socket(controller, controllerInitPort);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject("REMOVE");
            objectOutputStream.flush();

            objectOutputStream.writeObject(serverNameToDelete);
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendFile(String path, String ID) {
        FileSending fileSending = new FileSending();
        fileSending.fileName = path;
        try {
            Socket socket = new Socket(controller, controllerInitPort);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject("FILE");
            objectOutputStream.flush();

            String[] fileNameSplit = path.split("/");
            String fileName = fileNameSplit[fileNameSplit.length - 1];

            int size = (int) new File(path).length();
            fileName = fileName + ":" + size + ":" + ID;
            objectOutputStream.writeObject(fileName);
            objectOutputStream.flush();

            fileSending.socket = socket;
            fileSending.sendFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendInitializationRequestToController(String serverName) {

        try {
            Socket socket = new Socket(controller, controllerInitPort);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject("INIT");
            objectOutputStream.flush();

            objectOutputStream.writeObject(serverName);
            objectOutputStream.flush();
            socket.close();
        } catch (IOException e) {
            System.out.println("Cannot connect to CONTROLLER");
        }
    }
}
