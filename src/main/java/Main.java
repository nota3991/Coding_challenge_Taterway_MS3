import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class Main {
    //constants - in java they should be static final
    
    //first line (headers) of output bad csv file
    private static final String CSV_HEADERS = "A,B,C,D,E,F,G,H,I,J";

    //end of bad csv file name
    private static final String ERR_FILE_NAME_SUFFIX = "-bad.csv";

    public static void main(String[] args) {
        //prompt user for input file path/name (relative to current directory)
        System.out.printf("\nInput file path/name relative to %s: ", Paths.get("").toAbsolutePath().toString());
        //scanner for reading user console input
        Scanner scanner = new Scanner(System.in);
        //variable to hold input file name (declared outside of try...catch to maintain scope after try...catch)
        String csvFileName = null;
        try {
            //read user input from console and store
            csvFileName = scanner.nextLine();
            //not storing this, just creating path to ensure input forms a valid path (does not check if file exists just string validity to create path)
            Paths.get(csvFileName.trim()).toAbsolutePath().toString();
        } catch(InvalidPathException ex) {
            //close scanner resource
            scanner.close();

            ex.printStackTrace();
            System.exit(1);
        }
        
        //close scanner resource
        scanner.close();

        //init db connection, using the part of the input file name before the .csv as the db name
        DataDAO.initDB(csvFileName.split(".csv")[0]);

        //store badFileName as the part of the input file name before the .csv plus the constant ERR_FILE_NAME_SUFFIX
        String badFileName = csvFileName.split(".csv")[0] + ERR_FILE_NAME_SUFFIX;

        //read input file to have original file lines on hand for reference in case of bad records
        Path inputPath = Paths.get(csvFileName);
        //required for Files.readAllLines
        Charset charset = Charset.forName("UTF-8");
        //variable to store all fileLines as ArrayList (declared outside try..catch to maintain scope afterwards)
        List<String> fileLines = new ArrayList<>(0);
        try {
            //read all lines from file 
            fileLines = Files.readAllLines(inputPath, charset);
        } catch(IOException ex) {
            System.err.println("Error reading file: " + csvFileName);
            System.exit(1);
        }
        //we read all lines into a list above because the parsed CSVRecord type used below does not write into the error file in the exact way as it appears in the input
        //so, this way we have the original input lines and can just copy them in by index to the bad output csv file if necessary, rather than trying to write from the CSVRecord type

        /*
         * write each line to db or to err output csv
         */

        //used to create parser
        final CSVFormat csvFileFormat = CSVFormat.DEFAULT;

        //tracks if bad input has been read yet
        boolean hasBadOutput = false;

        //counters to track for output file
        int totalRecords = 0;
        int successRecords = 0;
        int failedRecords = 0;
        try (
            //create new reader from file input by user
            Reader reader = Files.newBufferedReader(Paths.get(csvFileName));
            //parse CSV using file reader
            CSVParser csvParser = csvFileFormat.parse(reader);
        ) {
            //allows for iterating over csv content to track current line number during iteration
            Iterator<CSVRecord> iterator = csvParser.iterator();

            //extra iterator.next() up front to iterate past header row so we skip it in below loop
            iterator.next();

            //loop over all input records
            while(iterator.hasNext()) {
                //increment counter
                totalRecords++;

                //store next record as "record"
                CSVRecord record = iterator.next();

                //create new instance of our Data object from Data.java, which represents a row of CSV data
                Data lineData = new Data();

                //pass record for this loop iteration to static parseDataCSV method, which converts CSVRecord object to Data object; lineData is passed by reference and populated in parseDataCSV
                if(Data.parseDataCSV(record, lineData)) {
                    //increment success counter
                    successRecords++;

                    //write to db
                    DataDAO.create(lineData);
                    continue;
                }

                //invalid row -- write to err file

                //increment fail counter
                failedRecords++;
                try {
                    //if we haven't had bad records yet, we'll branch here and flip the boolean and then write the new file
                    if(!hasBadOutput) {
                        //flip boolean flag to indicate from now on that we HAVE had bad output
                        hasBadOutput = true;

                        //write a new file - the 2nd argument is a list of strings (lines), starting with headers constant and then the line of the input file that corresponds to where the csvParser is right now
                        //the CREATE and TRUNCATE_EXISTING options at the end indicate that we should create this file if it does not exist already and that if it does exist, we should overwrite the contents from scratch
                        Files.write(Paths.get(badFileName), List.of(CSV_HEADERS, fileLines.get((int)csvParser.getCurrentLineNumber() - 1)), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    } else {
                        //we have already had bad output so APPEND to existing file - 2nd argument has to be a list type but it's just one line...the line from the original input file that corresponds to where the csvParser is
                        Files.write(Paths.get(badFileName), List.of(fileLines.get((int)csvParser.getCurrentLineNumber() - 1)), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }
            }

            //write .log file - using same method as above where 2nd argument is list of lines (strings)
            Files.write(Paths.get(csvFileName.split(".csv")[0] + ".log"), List.of(
                String.format("Records received: %d", totalRecords),
                String.format("Records successful: %d", successRecords),
                String.format("Records failed: %d", failedRecords)
            ), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            //again, CREATE and TRUNCATE_EXISTING either create the file if it doesn't exist or overwrites from scratch if it does
        } catch(Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
