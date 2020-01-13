import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.ibatis.common.jdbc.ScriptRunner;

public class DataDAO {
    //constant to store name of sql file that sets up db table
    private static final String CREATE_SQL_FILE_NAME = "sql/createSchema.sql";

    //static db Connection object used by this class
    private static Connection dbConn;

    //establishes db connection in static dbConn object
    public static void initDB(String dbName) {
        //--connection
        try {
            // get connection
            dbConn = DriverManager.getConnection(String.format("jdbc:sqlite:%s.db", Paths.get(dbName).toAbsolutePath()));
            if (dbConn != null) {
                System.out.println("DB connection successful!");
            } else {
                System.out.println("DB connection NOT successful!");
                System.exit(1);
            }
        } catch (SQLException e) {
            System.out.println("DB connection NOT successful!");
            e.printStackTrace();
            System.exit(1);
        }

        // --run create schema script
        Reader reader = null;
        try {
            // Initialize object for ScripRunner
            ScriptRunner sr = new ScriptRunner(dbConn, false, false);

            // get script by path/name
            reader = new BufferedReader(new FileReader(CREATE_SQL_FILE_NAME));

            // Excecute script
            sr.runScript(reader);

            // close this reader
            reader.close();
        } catch (Exception e) {
            System.err.println("Failed to Execute" + CREATE_SQL_FILE_NAME + " The error is " + e.getMessage());
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }        
    }

    //prevent making instances of this class, use static methods instead
    private DataDAO() {}

    //inserts new Data record into DB as a new row using JDBC
	public static void create(Data data) {
        //create new
        try {
            PreparedStatement createStatement = dbConn.prepareStatement("INSERT INTO csvdata (A, B, C, D, E, F, G, H, I, J) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
            createStatement.setString(1, data.getA());
            createStatement.setString(2, data.getB());
            createStatement.setString(3, data.getC());
            createStatement.setString(4, data.getD());
            createStatement.setString(5, data.getE());
            createStatement.setString(6, data.getF());
            createStatement.setFloat(7, data.getG());
            createStatement.setBoolean(8, data.getH());
            createStatement.setBoolean(9, data.getI());
            createStatement.setString(10, data.getJ());
            createStatement.execute();
        } catch(SQLException ex) {
            ex.printStackTrace();
            return;
        }
        return;
    }
}
