# Java CSV to SQLite DB

## Overview



This project prompts the user for a file name relative to the current directory (ending in `.csv`) and then establishes a connection to an SQLite DB of the same name (ending in `.db` rather than `.csv`).  

Finally, once the db is created, a `.sql` script [createSchema.sql](./sql/createSchema.sql) is ran, creating a single table.  The `.csv` file specified is then read, parsed, and inserted into the DB within this table.  If any records are invalid or lacking any of the required values, those records are written to a new `.csv` file (ending in `-bad.csv`).

Finally, a `.log` file is also written, summarizing the stats of the read/write process:
```
Records received: ####
Records successful: ####
Records failed: ####
```

## Table of Contents

  - [Overview](#overview)
  - [How to Run](#how-to-run)
  - [Code Files](#code-files)
      - [Main.java](#mainjava)
      - [Data.java](#datajava)
      - [DataDAO.java](#datadaojava)
      - [createSchema.sql](#createschemasql)

## How to Run

Use Maven:
```
Go to where the files are stored and run below command in the terminal. Do not go to the src file just the Folder where all of the files are

in the command line type

mvn clean && mvn package && mvn exec:java -Dexec.mainClass="Main"
```
Might need to install maven if you don't have it

there are 3 outside dependencies managed by Maven: commons-csv, sqlite-jdbc, ibatis2-common. The last one there provides some convenience methods for running an SQL file as a script. This is used to create the DB table on connection.

## Code Files

#### Main.java

The logic for the actual application isn't too long and so it all happens inside the `main` method.  The user is first prompted for the input file name (must end in `.csv`).  Then the DB connection is initialized, creating the SQLite DB file.  Next, the original input CSV file is read to have the original lines on hand in case of errors.  

Each `CSVRecord` is iterated over using the Apache Commons CSV Parse library.  This library assists with things like quotes or comma's within quotes as we have here for the `data:image` column.  This prevents us from simply splitting by commas and justifies the use of this library.  

If any fields are blank or missing, the original row from the input field is referenced and written to a new CSV output file.  Finally, the `.log` file is written with stats kept by 3 different counters during iteration over all CSVRecords.

#### Data.java

This is the model class for the input CSV.  The fields all share the column names.  The [parseDataCSV()](./src/main/java/Data.java#L32) method accepts a `CSVRecord` and a `Data` object.  Some basic validation is done on all the fields.  The method will return true if the record is valid and in that case, the `data` object will be populated and available to the caller on line 82 since it is an object, which in Java is passed by reference.  So, populating the `data` object inside `parseDataCSV()` will modify it back in [Main.java](./src/main/java/Main.java).  This also means that the default constructor is the only way to create a `Data` object but then you can populate the fields with the `parseDataCSV()` method.

#### DataDAO.java

This is the *Data Access Object* for our app.  It contains the method [initDB()](./src/main/java/DataDAO.java#L20), which establishes the DB connection.  It also runs the `.sql` script upon making the connection so that the DB table is available.  Finally, the [create()](./src/main/java/DataDAO.java#L65) method accepts a `Data` object and then writes this object to the database using JDBC.

#### createSchema.sql

This script creates the only DB table needed, if it doesn't already exist.  This script is executed by the [initDB()](./src/main/java/DataDAO.java#L20) method within [DataDAO.java](./src/main/java/DataDAO.java).
