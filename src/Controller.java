import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Controller {
    static CircularLinkedListNode root = null;
    static int count = 0;
    static CircularLinkedListNode serverAdded, newServerSuccessor, newServerPredecessor;
    static String fileName;
    static int maxLimit;
    static Map<String, ArrayList<String>> fileHeaders = new HashMap<String, ArrayList<String>>();
    static ArrayList<NodeDetails> nodes = new ArrayList<NodeDetails>();
    static ArrayList<StringBuffer> buffers = new ArrayList<StringBuffer>();
    static ArrayList<String> files = new ArrayList<String>();
    static String lastServerAdded = null;
    static Map<String, String> primaryKey = new HashMap<String, String>();
    static StringBuffer finalOut = new StringBuffer();
    static boolean othersAreWriting = false;
    static ArrayList<Integer> queryParsed = new ArrayList<Integer>();

    public static void main(String[] args) {
        Thread sendMapThread = new Thread(new ControllerHeaderMapSend());
        sendMapThread.start();

        Thread controllerToHandeQuery = new Thread(new ControllerToHandleQuery());
        controllerToHandeQuery.start();
/*

        Thread controllerQueryParse = new Thread(new ControllerQueryParse());
        controllerQueryParse.start();
*/

      /*  Thread controllerSendQuery = new Thread(new ControllerSendQuery());
        controllerSendQuery.start();*/

        int controllerInitPort = 5000;
        try {
            ServerSocket serverSocket = new ServerSocket(controllerInitPort);
            while (true) {
                Socket socket = serverSocket.accept();
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

                String requestType = (String) objectInputStream.readObject();

                if (requestType.equals("INIT")) {
                    String name = addServer(objectInputStream, socket);
                    updateListOfServers();
                    printServers();
                } else if (requestType.equals("FILE")) {
                    receiveFile(objectInputStream, socket);
                    System.out.println("Number of serves in system : " + count);
                    System.out.println("Preparing to distribute records among servers...");
                    int numberOfRecords = findNumberOfRecords();
                    System.out.println("Number of records in file : " + numberOfRecords);
                    distributeRecords(numberOfRecords);
                    //createFilesLocally(socket);
                    sendToRespectiveServers(socket);
                } else if (requestType.equals("REMOVE")) {
                    String serverNameToRemove = (String) objectInputStream.readObject();
                    System.out.println("Processing request to remove server : " + serverNameToRemove);
                    removeServer(serverNameToRemove);

                    //printCircular();
                    updateListOfServers();
                    printServers();
                }
            }
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }

    }

   /* private static void printCircular() {
        System.out.println("Circular print");
        CircularLinkedListNode r = root.succ;
        CircularLinkedListNode t = r;

        do {
            System.out.println(r.nodeDetails.nodeName);
            System.out.println(r.nodeDetails.range.minRange);
            System.out.println(r.nodeDetails.range.maxRange);
            System.out.println(r.nodeDetails.successor);
            System.out.println(r.nodeDetails.predecessor);
            r = r.succ;
        } while (r != t);

       *//* if (r.succ.nodeDetails.nodeName.equals(r.pred.nodeDetails.nodeName)) {
            System.out.println(r.nodeDetails.nodeName);
        } else {
            while (r.succ != t) {
                System.out.println(r.nodeDetails.nodeName);
                System.out.println(r.nodeDetails.range.minRange);
                System.out.println(r.nodeDetails.range.maxRange);
                System.out.println(r.nodeDetails.successor);
                System.out.println(r.nodeDetails.predecessor);
                r = r.succ;
            }
        }*//*
        System.out.println();
    }*/


    private static void removeServer(String serverNameToRemove) {
        CircularLinkedListNode node = root;
        if (node.nodeDetails.nodeName.equals(serverNameToRemove)) {
            System.out.println("Cannot remove bootstrap server");
        } else {
            CircularLinkedListNode nodeToRemove = reachUpToThatNode(serverNameToRemove);
            if (nodeToRemove != null) {
                sendAllFilesToSuccessor(nodeToRemove.nodeDetails.nodeName);

                nodeToRemove.succ.nodeDetails.predecessor = nodeToRemove.pred.nodeDetails.nodeName;
                nodeToRemove.pred.nodeDetails.successor = nodeToRemove.succ.nodeDetails.nodeName;

                nodeToRemove.succ.nodeDetails.range.minRange = nodeToRemove.nodeDetails.range.minRange;

                nodeToRemove.pred.succ = nodeToRemove.succ;
                nodeToRemove.succ.pred = nodeToRemove.pred;


                System.out.println("remove and stop process at " + nodeToRemove.nodeDetails.nodeName);
                try {
                    Socket socket = new Socket(nodeToRemove.nodeDetails.nodeName, 5050);
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("Sending update to :" + nodeToRemove.succ.nodeDetails.nodeName + "-" + nodeToRemove.pred.nodeDetails.nodeName);
                sendUpdateToSuccAndPred(nodeToRemove.succ.nodeDetails, nodeToRemove.pred.nodeDetails);
                System.out.println("Update sent to successor and predecessor");

            }
        }
        count--;
    }

    private static void sendUpdateToSuccAndPred(NodeDetails succ, NodeDetails pred) {
        try {
            if (succ.nodeName.equals(pred.nodeName)) {
                System.out.println("Trying to connect to : " + succ.nodeName);
                Socket s = new Socket(succ.nodeName, 5001);
                ObjectOutputStream sobjectOutputStream = new ObjectOutputStream(s.getOutputStream());
                sobjectOutputStream.writeObject(succ);
                sobjectOutputStream.flush();
                System.out.println("Successor updated successfully");
            } else {
                System.out.println("Trying to connect to : " + succ.nodeName);
                Socket s = new Socket(succ.nodeName, 5001);
                ObjectOutputStream sobjectOutputStream = new ObjectOutputStream(s.getOutputStream());
                sobjectOutputStream.writeObject(succ);
                sobjectOutputStream.flush();
                System.out.println("Successor updated successfully");

                System.out.println("Trying to connect to : " + pred.nodeName);
                Socket p = new Socket(pred.nodeName, 5001);
                ObjectOutputStream pobjectOutputStream = new ObjectOutputStream(p.getOutputStream());
                pobjectOutputStream.writeObject(pred);
                pobjectOutputStream.flush();
                System.out.println("Predecessor updated successfully");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendAllFilesToSuccessor(String sendCommandTo) {
        try {
            //calling SendFilesToSuccessorOnRemoval
            Socket socket = new Socket(sendCommandTo, 5011);
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            String message = (String) objectInputStream.readObject();
            System.out.println(message);


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static CircularLinkedListNode reachUpToThatNode(String serverNameToRemove) {
        CircularLinkedListNode temp = root.succ;
        CircularLinkedListNode nodeToReturn;
        while (temp != root) {
            if (temp.nodeDetails.nodeName.equals(serverNameToRemove)) {
                return temp;
            }
            temp = temp.succ;
        }
        return null;
    }

    private static void sendToRespectiveServers(Socket socket) {
        System.out.println("Transfering files...");

        for (int i = 0; i < nodes.size(); i++) {
            StringBuffer buffer = buffers.get(i);
            NodeDetails nodeDetails = nodes.get(i);
            System.out.println("Sending to  :  " + nodeDetails.nodeName);
            buffer.insert(0, fileHeaders.get(fileName) + "\n");
            sendDataFromBuffer(buffer, nodeDetails.nodeName);

        }
        /*String fileName = "/home/stu13/s13/yj8359/" + hostName + "/";
        File folderPath = new File(fileName);
        File[] listOfAllFilesinFolder = folderPath.listFiles();
        System.out.println("Number of files to send: " + listOfAllFilesinFolder.length);
        for (File f : listOfAllFilesinFolder) {
            if (f.getName().contains("cs.rit.edu")) {
                String[] name = f.getName().split("-");
                String actualFileName = name[1];
                System.out.println("Actual file name : " + actualFileName);
                System.out.println("Send this file to : " + name[0]);
                try {
                    Socket fileTransfer = new Socket(name[0], 5002);
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileTransfer.getOutputStream());
                    objectOutputStream.writeObject(actualFileName);
                    objectOutputStream.flush();
                    System.out.println("File name sent!!");


                    ObjectInputStream objectInputStream = new ObjectInputStream(fileTransfer.getInputStream());
                    String msg = (String) objectInputStream.readObject();
                    System.out.println(msg);
                    System.out.println("Here");
                    FileSending fileSending = new FileSending();
                    fileSending.socket = fileTransfer;
                    fileSending.fileName = fileName + f.getName();
                    // FileSending.socket = fileTransfer;
                    //FileSending.fileName = fileName + f.getName();
                    System.out.println("Calling send method");
                    fileSending.sendFile();

                    // ObjectInputStream objectInputStream = new ObjectInputStream(fileTransfer.getInputStream());
                    String out = (String) objectInputStream.readObject();
                    if (out.equals("received")) {
                        System.out.println("DONE!");
                    }
                    //f.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("All files have been successfully distributed!!!");*/
    }

    private static void sendDataFromBuffer(StringBuffer buffer, String nodeName) {
        String string = buffer.toString();
        try {
            Socket socket = new Socket(nodeName, 5002);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            //objectOutputStream.writeObject("Salaries.csv");
            objectOutputStream.writeObject(Controller.fileName);
            objectOutputStream.flush();
            System.out.println("File name sent!!");

            objectOutputStream.writeObject(string);
            objectOutputStream.flush();
            System.out.println("File sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printServers() {
        System.out.println();
        System.out.println("===========================NODE DETAILS===========================");
        for (NodeDetails nodeDetails : nodes) {
            System.out.println("ID -> " + nodeDetails.nodeID);
            System.out.println("Name -> " + nodeDetails.nodeName);
            System.out.println("Range -> " + nodeDetails.range.minRange + "-" + nodeDetails.range.maxRange);
            System.out.println("Successor ->" + nodeDetails.successor);
            System.out.println("Predecessor ->" + nodeDetails.predecessor);
            System.out.println();
        }
        System.out.println("====================================================================");
    }

    private static void createFilesLocally(Socket socket) {
        for (int i = 0; i < nodes.size(); i++) {
            System.out.println("NodeName: " + nodes.get(i).nodeName);
            String name = nodes.get(i).nodeName;
            String[] extractName = name.split(".");
            String fileName = name;
            fileName = fileName + "-" + Controller.fileName;
            String hostName = socket.getLocalAddress().getHostName();
            fileName = "/home/stu4/s1/vkm7895/" + hostName + "/" + fileName;
            //fileName = "/Users/YASH/IdeaProjects/DS-TermProject/download" + fileName;
            try {
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(fileName)));
                bufferedWriter.write(buffers.get(i).toString());
                bufferedWriter.flush();
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void updateListOfServers() {
        nodes = new ArrayList<NodeDetails>();
        System.out.println("COUNT" + count);
        /*CircularLinkedListNode r = root.succ;
        CircularLinkedListNode t = r;

        do {
            System.out.println(r.nodeDetails.nodeName);
            System.out.println(r.nodeDetails.range.minRange);
            System.out.println(r.nodeDetails.range.maxRange);
            System.out.println(r.nodeDetails.successor);
            System.out.println(r.nodeDetails.predecessor);
            nodes.add(r.nodeDetails);
            r = r.succ;
        } while (r != t);
*/
        if (count == 1) {
            root.nodeDetails.limit = maxLimit;
            nodes.add(root.nodeDetails);
            buffers.add(new StringBuffer());
        } else {
            CircularLinkedListNode temp = root.succ;
            CircularLinkedListNode firstNode = temp;
            while (true) {
                NodeDetails tempNode = temp.nodeDetails;
                tempNode.limit = maxLimit;
                nodes.add(tempNode);
                buffers.add(new StringBuffer());
                if (temp.succ == firstNode) {
                    break;
                }
                temp = temp.succ;
            }
        }
    }

    private static void distributeRecords(int numberOfRecords) {
        int numberOfRecordsInEachServer = numberOfRecords % maxLimit;
        buffers = new ArrayList<StringBuffer>();
        for (int i = 0; i < maxLimit; i++) {
            StringBuffer stringBuffer = new StringBuffer();
            buffers.add(stringBuffer);
        }

        String fileToDistribute = "/home/stu4/s1/vkm7895/newyork/" + fileName;
        //String fileToDistribute = "/Users/YASH/IdeaProjects/DS-TermProject/download/" + fileName;
        int columnNumberForID = columnNumberForID(fileName);
        int count = 0;
        try {
            BufferedReader fileReader = new BufferedReader(new FileReader(fileToDistribute));
            String line = "";
            while ((line = fileReader.readLine()) != null) {
                if (count == 0) {
                    count = 1;
                    continue;
                }
                String[] row = line.split(",");
                int range = findHashOfRecord(row[columnNumberForID]);
                //System.out.println("Hash range is : " + range);
                int serverNum = findServer(range);
                //int serverNum = Integer.parseInt(row[columnNumberForID]) % Controller.count;
                buffers.get(serverNum).append(line + "\n");
            }
            new File(fileToDistribute).delete();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Cannot read file");
        }

    }

    private static int findServer(int range) {
        int serverNum = 0;
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).range.minRange <= range && nodes.get(i).range.maxRange >= range) {
                serverNum = i;
                return serverNum;
            }
        }
        return serverNum;
    }

    private static int findHashOfRecord(String s) {
        int hash = s.hashCode();
        hash = hash % maxLimit;
        return hash;
    }

    private static int columnNumberForID(String fileName) {
        int columnNumber = 0;
        ArrayList<String> columns = fileHeaders.get(fileName);
        for (String s : columns) {
            if (s.toLowerCase().equals(primaryKey.get(fileName))) {
                System.out.println();
                System.out.println("ID Column number is" + columnNumber);
                return columnNumber;
            }
            columnNumber++;
        }
        return 0;
    }

    private static int findNumberOfRecords() {
        int lineCount = 0;
        String fileToParse = "/home/stu4/s1/vkm7895/newyork/" + fileName;
        try {
            BufferedReader fileReader = new BufferedReader(new FileReader(fileToParse));
            String line = "";
            while ((line = (fileReader.readLine())) != null) {
                //find Header
                if (lineCount == 0) {
                    String[] columns = line.split(",");
                    ArrayList<String> columnNames = new ArrayList<String>();
                    for (String s : columns) {
                        columnNames.add(s);
                    }
                    fileHeaders.put(fileName, columnNames);
                }
                lineCount++;
            }
            /*System.out.println("HEader:");
            for (String c : fileHeaders.get(fileName)) {
                System.out.println("HEader :" + c);
            }*/
        } catch (Exception e) {
            System.out.println("Cannot read file");
        }
        return lineCount;
    }

    private static void receiveFile(ObjectInputStream objectInputStream, Socket socket) {
        try {
            String fileName = (String) objectInputStream.readObject();
            String hostName = socket.getLocalAddress().getHostName();
            String[] nameAndSize = fileName.split(":");

            fileName = nameAndSize[0];
            Controller.fileName = fileName;
            if (!files.contains(fileName)) {
                files.add(fileName);
                primaryKey.put(fileName, nameAndSize[2].toLowerCase());
            }
            int size = Integer.parseInt(nameAndSize[1]);
            System.out.println("File: " + fileName + " Size : " + size);

            FileReceiving fileReceiving = new FileReceiving();
            fileReceiving.socket = socket;
            fileReceiving.fileToReceive = "/home/stu4/s1/vkm7895/" + hostName + "/" + fileName;
            // FileReceiving.socket = socket;
            //FileReceiving.fileToReceive = "/home/stu13/s13/yj8359/" + hostName + "/" + fileName;
            //FileReceiving.fileToReceive = "/Users/YASH/IdeaProjects/DS-TermProject/download/" + fileName;
            fileReceiving.receiveFile();

            System.out.println("Preparing to distribute file records over the servers.....");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String addServer(ObjectInputStream objectInputStream, Socket socket) {
        System.out.println("Request to add new server accepted from admin");

        String nodeNameRange = null;
        String name = null;
        try {
            nodeNameRange = (String) objectInputStream.readObject();
            String[] nameAndRange = nodeNameRange.split(":");
            System.out.println("Create server: " + nameAndRange[0] + " with range: " + nameAndRange[1]);
            name = nameAndRange[0];
            socket.close();
            NodeDetails nodeDetail = new NodeDetails();
            nodeDetail.nodeName = nameAndRange[0];
            if (root == null) {
                Range range = new Range();
                range.minRange = 0;
                range.maxRange = Integer.valueOf(nameAndRange[1]);
                maxLimit = Integer.valueOf(nameAndRange[1]);
                nodeDetail.range = range;

                nodeDetail.nodeID = 0;
                count++;

                nodeDetail.predecessor = nameAndRange[0];
                nodeDetail.successor = nameAndRange[0];

                root = new CircularLinkedListNode();
                root.nodeDetails = nodeDetail;
                root.pred = null;
                root.succ = null;
                serverAdded = root;
                newServerSuccessor = newServerPredecessor = null;
            } else {
                lastServerAdded = nameAndRange[0];
                CircularLinkedListNode node = findPositioninChord(Integer.parseInt(nameAndRange[1]));
                //System.out.println("Position:" + position);
                CircularLinkedListNode tempNode = new CircularLinkedListNode();
                if (root.succ == null && root.pred == null) {
                    root.succ = tempNode;
                    root.pred = tempNode;
                    tempNode.succ = root;
                    tempNode.pred = root;
                } else {
                    tempNode.succ = node.succ;
                    tempNode.pred = node;
                    node.succ.pred = tempNode;
                    node.succ = tempNode;
                }

                nodeDetail.nodeID = count++;

                Range range = new Range();
                range.maxRange = Integer.parseInt(nameAndRange[1]);
                range.minRange = tempNode.succ.nodeDetails.range.minRange;
                nodeDetail.range = range;

                tempNode.succ.nodeDetails.range.minRange = Integer.parseInt(nameAndRange[1]) + 1;

                nodeDetail.successor = tempNode.succ.nodeDetails.nodeName;
                nodeDetail.predecessor = tempNode.pred.nodeDetails.nodeName;

                tempNode.succ.nodeDetails.predecessor = nameAndRange[0];
                tempNode.pred.nodeDetails.successor = nameAndRange[0];

                tempNode.nodeDetails = nodeDetail;
                serverAdded = tempNode;
                newServerSuccessor = tempNode.succ;
                newServerPredecessor = tempNode.pred;
            }
            //print();
            System.out.println("Adding server and adjusting successor and predecessor.......");
            addAndAdjust();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }

    private static void addAndAdjust() {
        //new server
        String server = serverAdded.nodeDetails.nodeName;
        serverAdded.nodeDetails.isNew = true;
        connectAndAdjust(server, serverAdded.nodeDetails);

        if (newServerSuccessor == null && newServerPredecessor == null) {

        } else if (newServerPredecessor == newServerSuccessor) {
            server = newServerSuccessor.nodeDetails.nodeName;
            connectAndAdjust(server, newServerSuccessor.nodeDetails);
        } else {
            server = newServerPredecessor.nodeDetails.nodeName;
            connectAndAdjust(server, newServerPredecessor.nodeDetails);

            server = newServerSuccessor.nodeDetails.nodeName;
            connectAndAdjust(server, newServerSuccessor.nodeDetails);
        }
        serverAdded.nodeDetails.isNew = false;

    }

    private static void connectAndAdjust(String server, NodeDetails node) {
        try {
            Socket socket = new Socket(server, 5001);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(node);
            objectOutputStream.flush();
            socket.close();
        } catch (IOException e) {
            System.out.println("Cannot connect to " + server + " ,please check if Process is up and running...");
        }
    }

    private static void print() {
        CircularLinkedListNode temp = root;
        if (root.succ == null && root.pred == null) {
            System.out.println("------------");
            System.out.println("Name = " + temp.nodeDetails.nodeName);
            System.out.println("ID = " + temp.nodeDetails.nodeID);
            System.out.println("Min = " + temp.nodeDetails.range.minRange);
            System.out.println("Max = " + temp.nodeDetails.range.maxRange);
            System.out.println("Pred = " + temp.nodeDetails.predecessor);
            System.out.println("Succ  = " + temp.nodeDetails.successor);
            return;
        }
        boolean visitAgain = false;

        while (temp != root || (temp == root && !visitAgain)) {
            if (temp == root && !visitAgain) {
                visitAgain = true;
            }

            System.out.println("------------");
            System.out.println("Name = " + temp.nodeDetails.nodeName);
            System.out.println("ID = " + temp.nodeDetails.nodeID);
            System.out.println("Min = " + temp.nodeDetails.range.minRange);
            System.out.println("Max = " + temp.nodeDetails.range.maxRange);
            System.out.println("Pred = " + temp.nodeDetails.predecessor);
            System.out.println("Succ  = " + temp.nodeDetails.successor);
            temp = temp.succ;
        }
    }


    private static CircularLinkedListNode findPositioninChord(int maxRange) {
        CircularLinkedListNode temp = root;
        if (root.succ == null && root.pred == null) {
            return root;
        } else {
            while (!(temp.nodeDetails.range.minRange < maxRange && temp.nodeDetails.range.maxRange > maxRange)) {
                temp = temp.succ;
            }
            return temp.pred;
        }
    }
}
