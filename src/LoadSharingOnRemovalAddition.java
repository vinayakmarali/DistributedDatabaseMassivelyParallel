import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class LoadSharingOnRemovalAddition implements Runnable {
    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(5003);
            while (true) {
                Socket socket = serverSocket.accept();
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                String requestType = (String) objectInputStream.readObject();
                String hostName = socket.getLocalAddress().getHostName();
                String folderToLookFor = "/home/stu4/s1/vkm7895/" + hostName + "/";
                if (requestType.equals("sendMeMyRecords")) {
                    removeDataToTransfer(folderToLookFor);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void removeDataToTransfer(String folder) {
        //Part 1-------------------------------------------------------------------------
        System.out.println("Connecting to controller to get header details");
        try {
            Socket socket = new Socket("newyork.cs.rit.edu", 8000);
            System.out.println("Connected to controller");
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            Process.fileHeaders = (Map<String, ArrayList<String>>) objectInputStream.readObject();
            System.out.println("Header received");

            Process.primaryKeys = (Map<String, String>) objectInputStream.readObject();
            System.out.println("Primary keys received");

            System.out.println("Printing headers after sending:");
            for (Map.Entry<String, ArrayList<String>> e : Process.fileHeaders.entrySet()) {
                System.out.println("File name: " + e.getKey());
                ArrayList<String> temp = e.getValue();
                for (int i = 0; i < temp.size(); i++) {
                    System.out.println("Header : " + temp.get(i));
                }
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        //Part 2--------------------------------------------------------------------------

        if (Process.fileHeaders != null) {
            File[] files = new File(folder).listFiles();

            Map<String, ArrayList<StringBuffer>> map = new HashMap<String, ArrayList<StringBuffer>>();

            ArrayList<String> fileNames = new ArrayList<String>();
            ArrayList<StringBuffer> stringBuffers = new ArrayList<StringBuffer>();
            ArrayList<StringBuffer> localBuffers = new ArrayList<StringBuffer>();
            ArrayList<String> localFileNames = new ArrayList<String>();

            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().contains(".csv")) {
                    System.out.println("Get header for file : " + files[i].getName());
                    ArrayList<String> header = Process.fileHeaders.get(files[i].getName());

                    System.out.println("header is :");
                    System.out.println("header length = " + header.size());
                    for (String s : header) {
                        System.out.println(s);
                    }


                    int IDrowNumber = getRowID(header, Process.primaryKeys.get(files[i].getName()));
                    System.out.println("ID row number is: " + IDrowNumber);
                    int lineCount = 0;
                    int localContent = 0;

                    StringBuffer stringBuffer = new StringBuffer();
                    StringBuffer localStringBuffer = new StringBuffer();

                    try {
                        File file = files[i];
                        String name = file.getName();
                        BufferedReader bufferedReaderForFile = new BufferedReader(new FileReader(file));
                        String line = "";
                        int t = 0;
                        while ((line = bufferedReaderForFile.readLine()) != null) {
                            if (t == 0) {
                                t = 1;
                                continue;
                            }
                            String[] delimit = line.split(",");
                            String ID = delimit[IDrowNumber];
                            int hash = getHash(ID);
                            if (hash > Process.maxRange || hash < Process.minRange) {
                                lineCount++;
                                stringBuffer.append(line + "\n");
                            } else {
                                localContent++;
                                localStringBuffer.append(line + "\n");
                            }
                        }
                        if (lineCount > 0) {
                            fileNames.add(name);
                            stringBuffer.insert(0, header + "\n");
                            stringBuffers.add(stringBuffer);
                        }
                        if (localContent > 0) {
                            localFileNames.add(name);
                            localStringBuffer.insert(0, header + "\n");
                            localBuffers.add(localStringBuffer);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            //Part 3 -------------------------------------------------------------------------------
            System.out.println("Arranging local files....");
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                f.delete();
            }
            System.out.println("    Deleted local files");
            System.out.println("    Creating new files with new contents");
            createFiles(folder, localBuffers, localFileNames);
            System.out.println("All local files adjusted successfully");

            //Part 4-------------------------------------------------------------------------------
            sendToPredecessor(fileNames, stringBuffers);

            System.out.println("All content sent to respective nodes..");
        } else {
            System.out.println("No file to share load...");
        }
    }

    private void sendToPredecessor(ArrayList<String> fileNames, ArrayList<StringBuffer> stringBuffers) {
        String sendToPred = Process.predecessor;
        try {
            Socket socket = new Socket(sendToPred, 5010);
            System.out.println("Connected to " + sendToPred);
            NameAndBufferObjects nameAndBufferObjects = new NameAndBufferObjects();
            nameAndBufferObjects.names = fileNames;
            nameAndBufferObjects.stringBuffers = stringBuffers;


            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject("add");
            objectOutputStream.flush();

            objectOutputStream.writeObject(nameAndBufferObjects);
            objectOutputStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createFiles(String folder, ArrayList<StringBuffer> localBuffers, ArrayList<String> localFileNames) {
        System.out.println("        Opening folder and creating files");
        for (int i = 0; i < localBuffers.size(); i++) {
            String filePath = folder + localFileNames.get(i);
            StringBuffer stringBuffer = localBuffers.get(i);
            try {
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(filePath)));
                bufferedWriter.write(stringBuffer.toString());
                bufferedWriter.close();
                System.out.println("File " + filePath + " created.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int getHash(String id) {
        int hash = id.hashCode();
        hash = hash % Process.maxRange;
        return hash;
    }

    private int getRowID(ArrayList<String> header, String id) {
        int count = 0;
        for (int i = 0; i < header.size(); i++) {
            if (header.get(i).equals(id)) {
                return i;
            }
        }
        return 0;
    }
}
