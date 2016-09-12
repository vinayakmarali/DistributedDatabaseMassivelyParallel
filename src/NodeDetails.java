import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class NodeDetails implements Serializable {
    String nodeName;
    int nodeID;
    Range range;
    String predecessor, successor;
    boolean isNew;
    boolean isRemoving;
    int limit;
    static Map<String, ArrayList<String>> fileHeaders = new HashMap<String, ArrayList<String>>();
}
