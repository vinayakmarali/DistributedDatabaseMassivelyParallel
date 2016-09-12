import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        int count = 0;
        //Input file which needs to be parsed
        String fileToParse = "/Users/YASH/IdeaProjects/DS-TermProject/Salaries.csv";
        BufferedReader fileReader = null;

        //Delimiter used in CSV file/Users/YASH/IdeaProjects/DS-TermProject/Salaries.csv
        final String DELIMITER = "\n";
        try {
            String line = "";
            //Create the file reader
            fileReader = new BufferedReader(new FileReader(fileToParse));
            //Read the file line by line
            while ((line = fileReader.readLine()) != null) {
                //Get all tokens available in line
                count++;
                /*String[] tokens = line.split(DELIMITER);
                for (String token : tokens) {
                    //Print all tokens
                    System.out.println(token);
                }*/
            }
            System.out.println(count);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
