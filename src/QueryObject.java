import java.io.Serializable;
import java.util.ArrayList;


public class QueryObject implements Serializable {
    String fileName;
    ArrayList<String> columns;
    Integer IdVal;
    String serachByColumnName;
    String valueOfColumnName;
    Integer greaterThan;
    Integer lessThan;
}
