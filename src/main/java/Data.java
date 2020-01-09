import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class Data {
    private String A;
    private String B;
    private String C;
    private String D;
    private String E;
    private String F;
    private float G;
    private boolean H;
    private boolean I;
    private String J;

    //default constructor for use in main
    public Data() {}

    /**
     * receives raw csvLine and reference to Data instance
     * returns boolean whether csvLine is valid
     * if csvLine is valid, data instance will be populated
     * @param csvLine
     * @param data
    */
    public static boolean parseDataCSV(CSVRecord record, Data data) {
        //read & validate csvLine
        if(!record.isConsistent()) {
            return false;
        }
        for(int i = 0; i < 10; i++) {
            switch(i) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 9:
                if(record.get(i).length() == 0) {
                    return false;
                }
                break;
            case 6:
                try {
                    Float.parseFloat(record.get(i).substring(1));
                } catch(Exception ex) {
                    return false;
                }
                break;
            case 7:
            case 8:
                try {
                    Boolean.parseBoolean(record.get(i));
                } catch(Exception ex) {
                    return false;
                }
                break;
            default:
                return false;
            }
        }
        
        //populate data of valid csvLine
        data.A = record.get(0);
        data.B = record.get(1);
        data.C = record.get(2);
        data.D = record.get(3);
        data.E = record.get(4);
        data.F = record.get(5);
        data.G = Float.parseFloat(record.get(6).substring(1));
        data.H = Boolean.parseBoolean(record.get(7));
        data.I = Boolean.parseBoolean(record.get(8));
        data.J = record.get(9);

        return true;
    }

    /**
     * @return the a
     */
    public String getA() {
        return A;
    }

    /**
     * @return the b
     */
    public String getB() {
        return B;
    }

    /**
     * @return the c
     */
    public String getC() {
        return C;
    }

    /**
     * @return the d
     */
    public String getD() {
        return D;
    }

    /**
     * @return the e
     */
    public String getE() {
        return E;
    }

    /**
     * @return the f
     */
    public String getF() {
        return F;
    }

    /**
     * @return the g
     */
    public float getG() {
        return G;
    }
    
    /**
     * @return the h
     */
    public boolean getH() {
        return H;
    }

    /**
     * @return the i
     */
    public boolean getI() {
        return I;
    }

    /**
     * @return the j
     */
    public String getJ() {
        return J;
    }
    
    @Override
    public String toString() {
        return String.format("Data Row >> A: %s, B: %s, C: %s, D: %s, E: %s, F: %s, G: $%.2f, H: %s, I: %s, J: %s", 
                A, B, C, D, E, F, G, Boolean.toString(H), Boolean.toString(I), J);
    }
}
