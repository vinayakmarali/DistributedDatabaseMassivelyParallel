import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Process {
    static int ID;
    static String myName;
    static int minRange, maxRange;
    static int limit;
    static String predecessor, successor;
    static ArrayList<String> files = new ArrayList<String>();
    static Map<String, ArrayList<String>> fileHeaders = new HashMap<String, ArrayList<String>>();
    static Map<String, String> primaryKeys = new HashMap<String, String>();

    public static void main(String[] args) {
        Thread processUpdateThread = new Thread(new ProcessUpdate());
        processUpdateThread.start();

        Thread processReceiveFileThread = new Thread(new ProcessReceiveFile());
        processReceiveFileThread.start();

        Thread receiveAdjustedFileThread = new Thread(new ReceiveAdjustedFiles());
        receiveAdjustedFileThread.start();

        Thread loadSharingOnRemovalAddition = new Thread(new LoadSharingOnRemovalAddition());
        loadSharingOnRemovalAddition.start();

        Thread sendFilesToSuccessorOnRemoval = new Thread(new SendFilesToSuccessorOnRemoval());
        sendFilesToSuccessorOnRemoval.start();

        Thread removeNode = new Thread(new RemoveNode());
        removeNode.start();

        Thread processQuery = new Thread(new ProcessQuery());
        processQuery.start();
    }


    public static void printServerDetails() {
        System.out.println("----------------SERVER DETAILS------------------");
        System.out.println("Name : " + myName);
        System.out.println("ID : " + ID);
        System.out.println("Range : " + minRange + " - " + maxRange);
        System.out.println("Successor : " + successor);
        System.out.println("Predecessor : " + predecessor);
        System.out.println("Max Range : " + limit);
        System.out.println("-------------------------------------------------");
    }
}
