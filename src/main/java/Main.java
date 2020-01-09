import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;

public class Main {
    private static final String CSV_HEADERS = "A,B,C,D,E,F,G,H,I,J";
    private static final String ERR_FILE_NAME_SUFFIX = "-bad.csv";

    public static void main(String[] args) {
        System.out.printf("\nInput file path/name relative to %s: ", Paths.get("").toAbsolutePath().toString());
        Scanner scanner = new Scanner(System.in);
        String csvFileName = null;
        try {
            csvFileName = scanner.nextLine();
            Paths.get(csvFileName.trim()).toAbsolutePath().toString();
        } catch(InvalidPathException ex) {
            //close scanner resource
            scanner.close();

            ex.printStackTrace();
            System.exit(1);
        }
        
        //close scanner resource
        scanner.close();

        //init db connection
        DataDAO.initDB(csvFileName.split(".csv")[0]);

        //write file line
        String badFileName = csvFileName.split(".csv")[0] + ERR_FILE_NAME_SUFFIX;

        //read input file to have original file lines on hand
        Path inputPath = Paths.get(csvFileName);
        Charset charset = Charset.forName("UTF-8");
        List<String> fileLines = new ArrayList<>(0);
        try {
            fileLines = Files.readAllLines(inputPath, charset);
        } catch(IOException ex) {
            System.err.println("Error reading file: " + csvFileName);
            System.exit(1);
        }

        //write each line to db or to err output csv
        final CSVFormat csvFileFormat = CSVFormat.DEFAULT;
        boolean hasBadOutput = false;
        int totalRecords = 0;
        int successRecords = 0;
        int failedRecords = 0;
        try (
            Reader reader = Files.newBufferedReader(Paths.get(csvFileName));
            CSVParser csvParser = csvFileFormat.parse(reader);
        ) {
            Iterator<CSVRecord> iterator = csvParser.iterator();

            //iterate to header row so we skip it in below loop
            iterator.next();

            while(iterator.hasNext()) { 
                totalRecords++;

                CSVRecord record = iterator.next();

                Data lineData = new Data();
                if(Data.parseDataCSV(record, lineData)) {
                    //increment counter
                    successRecords++;

                    //write to db
                    DataDAO.create(lineData);
                    continue;
                }

                //invalid row -- write to err file
                failedRecords++;
                try {
                    if(!hasBadOutput) {
                        hasBadOutput = true;
                        Files.write(Paths.get(badFileName), List.of(CSV_HEADERS, fileLines.get((int)csvParser.getCurrentLineNumber() - 1)), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    } else {
                        Files.write(Paths.get(badFileName), List.of(fileLines.get((int)csvParser.getCurrentLineNumber() - 1)), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }
            }

            //write .log file
            Files.write(Paths.get(csvFileName.split(".csv")[0] + ".log"), List.of(
                String.format("Records received: %d", totalRecords),
                String.format("Records successful: %d", successRecords),
                String.format("Records failed: %d", failedRecords)
            ), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch(Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}