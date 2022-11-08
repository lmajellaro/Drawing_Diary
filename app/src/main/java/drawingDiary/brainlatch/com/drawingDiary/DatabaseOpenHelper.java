package drawingDiary.brainlatch.com.drawingDiary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static drawingDiary.brainlatch.com.drawingDiary.TaskConstants.*;

/**
 * Created by Luigi on 03/09/2017.
 */

public class DatabaseOpenHelper extends SQLiteOpenHelper {

    private static DatabaseOpenHelper mInstance = null;

    String databaseName = "";
    String tableName = "";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TASK = "task";
    public static final String COLUMN_DONE = "done";
    public static final String COLUMN_COMMENTS = "comments";
    public static final String COLUMN_IMG_LINK = "imglink";
    public static final String EXTRA_SEARCH_KEY = "inktober.brainlatch.com.inktober.MESSAGE";

    private SQLiteDatabase database;

    private final Context context;

    // database path
    private static String DATABASE_PATH;


    public String getDbName() {
        return this.databaseName;
    }

    public void setDbName(String dbName) {
        this.databaseName = dbName;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tbName) {
        this.tableName = tbName;
    }


    public static DatabaseOpenHelper getInstance(Context ctx, String dbName) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null) {
            mInstance = new DatabaseOpenHelper(ctx.getApplicationContext(), dbName);
        }
        return mInstance;
    }

    /**
     * constructor
     */
    private DatabaseOpenHelper(Context ctx, String dbName) {
        super(ctx, dbName, null, DATABASE_VERSION);
        this.context = ctx;
        DATABASE_PATH = context.getFilesDir().getParentFile().getPath()
                + "/databases/";

    }

    /**
     * Creates a empty database on the system and rewrites it with your own
     * database.
     */
    public void create() throws IOException {
        boolean check = checkDataBase();

        SQLiteDatabase db_Read = null;

        // Creates empty database default system path
        db_Read = this.getWritableDatabase();
        db_Read.close();
        try {
            if (!check) {
                copyDataBase();
            }
        } catch (IOException e) {
            throw new Error("Error copying database");
        }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each
     * time you open the application.
     *
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {
        // context.deleteDatabase(databaseName);  //TO BE REMOVED
        File dbFile = context.getDatabasePath(databaseName);
        return dbFile.exists();
    }

    /**
     * Copies your database from your local assets-folder to the just created
     * empty database in the system folder, from where it can be accessed and
     * handled. This is done by transfering bytestream.
     */
    private void copyDataBase() throws IOException {

        // Open your local db as the input stream
        InputStream myInput = context.getAssets().open(databaseName);

        // Path to the just created empty db
        String outFileName = DATABASE_PATH + databaseName;

        // Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        // transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    private void copyDataBase4Upg(String dbSource, String dbTarget) throws IOException {

        File dbFile = context.getDatabasePath(dbTarget);

        boolean check = dbFile.exists();

        SQLiteDatabase db_Read = null;

        // Creates empty database default system path
        db_Read = this.getWritableDatabase();
        db_Read.close();

        try {
            if (!check) {
                // Open your local db as the input stream
                InputStream myInput = context.getAssets().open(dbSource);

                // Path to the just created empty db
                String outFileName = DATABASE_PATH + dbTarget;

                // Open the empty db as the output stream
                OutputStream myOutput = new FileOutputStream(outFileName);

                // transfer bytes from the inputfile to the outputfile
                byte[] buffer = new byte[1024];
                int length;
                while ((length = myInput.read(buffer)) > 0) {
                    myOutput.write(buffer, 0, length);
                }

                // Close the streams
                myOutput.flush();
                myOutput.close();
                myInput.close();
            }
        } catch (IOException e) {
            throw new Error("Error copying database");
        }
    }


    /**
     * open the database
     */
    public void open() throws SQLException {
        String myPath = DATABASE_PATH + databaseName;
        database = SQLiteDatabase.openDatabase(myPath, null,
                SQLiteDatabase.OPEN_READWRITE);
    }

    /**
     * close the database
     */
    @Override
    public synchronized void close() {
        if (database != null)
            database.close();
        super.close();
    }

    // insert prompt data into the database
    public long insertPromptData(Integer id, String task, Integer done, String comments, String imglink) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(COLUMN_ID, id);
        initialValues.put(COLUMN_TASK, task);
        initialValues.put(COLUMN_DONE, done);
        initialValues.put(COLUMN_COMMENTS, comments);
        initialValues.put(COLUMN_IMG_LINK, imglink);
        return database.insert(tableName, null, initialValues);
    }

    // updates prompt Data
    public boolean updatePromptData(Integer rowId, Integer done, String comments, String imglink) {
        ContentValues args = new ContentValues();
        if (done != null)
            args.put(COLUMN_DONE, done);
        if (comments != null)
            args.put(COLUMN_COMMENTS, comments);
        if (imglink != null)
            args.put(COLUMN_IMG_LINK, imglink);

        return database.update(tableName, args, COLUMN_ID + "=" + rowId, null) > 0;
    }

    // Edit task Data
    public boolean updateTaskDB(Integer rowId, String newTask) {

        int updatedRows = 0;

        String resTable = tableName + TB_RESOURCE_TABLE_SUFFIX;

        ContentValues args = new ContentValues();
        if (newTask != null)
            args.put(COLUMN_TASK, newTask);

        updatedRows = database.update(resTable, args, COLUMN_ID + "=" + rowId, null);

        updatedRows += database.update(tableName, args, COLUMN_ID + "=" + rowId, null);

        return updatedRows > 0;
    }

    // retrieves a particular day
    public Cursor getPromptDay(long rowId) throws SQLException {
        Cursor mCursor = database.query(true, tableName, new String[]{
                        COLUMN_ID, COLUMN_TASK, COLUMN_DONE, COLUMN_COMMENTS, COLUMN_IMG_LINK},
                COLUMN_ID + " = " + rowId, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        return mCursor;
    }


    public Cursor getInktoberIdea(String task) throws SQLException {
        Cursor mCursor = database.query(true, tableName, new String[]{
                        "ID", "Task", "Idea"},
                "Task" + " = " + task, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        return mCursor;
    }

    public void dropDatabase(String dbname) throws SQLException {
        context.deleteDatabase(dbname);
    }


    // delete a particular day of the prompt Challenge
    public boolean deletePromptEntry(long rowId) {
        return database.delete(tableName, COLUMN_ID + "=" + rowId, null) > 0;
    }

    // retrieves all days
    public Cursor getAllPromptData() {
        return database.query(tableName, new String[]{COLUMN_ID,
                        COLUMN_TASK, COLUMN_DONE, COLUMN_COMMENTS}, null, null,
                null, null, COLUMN_ID + " ASC");
    }

    @Override
    public void onCreate(SQLiteDatabase arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        String oldDatabaseName = "inkToberDB_2017";

        if (oldVersion < 3 && checkDataBaseExists(DB_INKTOBER_SUGGESTIONS)) {
            context.deleteDatabase(DB_INKTOBER_SUGGESTIONS);
        }
        if (oldVersion < 4 && checkDataBaseExists(oldDatabaseName)) {
            setDbName("drawingDiary");
            setTableName("inktober2017");
            open();
            deleteAll();

            String dbPath = DATABASE_PATH + databaseName;
            db.execSQL("attach database '" + dbPath + "' as newInk2017");
            dbPath = DATABASE_PATH + oldDatabaseName;
            db.execSQL("attach database '" + dbPath + "' as oldInk2017");

/*            String qry = "ATTACH DATABASE 'inkToberDB_2017' AS ink2017;";

            database.execSQL(qry);*/

            String qry = "INSERT INTO \n" +
                    "newInk2017." + tableName + " \n" +
                    " SELECT "
                    + COLUMN_ID + ", "
                    + COLUMN_TASK + ", "
                    + COLUMN_DONE + ", "
                    + COLUMN_COMMENTS + ", "
                    + "\"\" \n" +
                    " FROM oldInk2017." + tableName + "; \n";

            db.execSQL(qry);

            context.deleteDatabase(oldDatabaseName);
            close();
        }
        if (oldVersion < 5) {
            String qry = "CREATE TABLE xmas ( id INTEGER PRIMARY KEY, task TEXT, done INTEGER, comments TEXT DEFAULT '', imglink TEXT)";
            db.execSQL(qry);
            qry = "CREATE TABLE xmas_res ( id INTEGER PRIMARY KEY, task TEXT, done INTEGER, comments TEXT DEFAULT '', imglink TEXT DEFAULT '')";
            db.execSQL(qry);
            qry = "CREATE TABLE creatury ( id INTEGER PRIMARY KEY, task TEXT, done INTEGER, comments TEXT DEFAULT '', imglink TEXT)";
            db.execSQL(qry);
            qry = "CREATE TABLE creatury_res ( id INTEGER PRIMARY KEY, task TEXT, done INTEGER, comments TEXT DEFAULT '', imglink TEXT DEFAULT '')";
            db.execSQL(qry);
        }
        if (oldVersion < 6) {
            String qry = "CREATE TABLE customprompts_list ( promptName TEXT, tableName INTEGER PRIMARY KEY AUTOINCREMENT)";
            db.execSQL(qry);
        }
        if (oldVersion < 7) {
            setDbName("drawingDiary");
            setTableName("inktober2018");
            String qry = "CREATE TABLE inktober2018 ( id INTEGER PRIMARY KEY, task TEXT, done INTEGER, comments TEXT DEFAULT '', imglink TEXT DEFAULT '')";
            db.execSQL(qry);
        }
    }

    /**
     * Check if the database exist and can be read.
     *
     * @return true if it exists and can be read, false if it doesn't
     */
    private boolean checkDataBaseExists(String dbName) {
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(DATABASE_PATH + dbName, null,
                    SQLiteDatabase.OPEN_READONLY);
            checkDB.close();
        } catch (SQLiteException e) {
            // database doesn't exist yet.
        }
        return checkDB != null;
    }

    public Cursor runQuery(String query, String[] param) {

        return database.rawQuery(query, param, null);
    }

    public void runQuery(String qry) {

        database.execSQL(qry);
    }


    public void deleteAll() {

        database.execSQL("delete from " + tableName);
    }

    public void deleteAll(String tableName) {

        database.execSQL("delete from " + tableName);
    }


    public void createPrompt(int numberOfRows) {

        String resTable = tableName + TB_RESOURCE_TABLE_SUFFIX;
        String qry = "";

        //If the resource table is empty pick up the content
        //From the Asset DB
        if (isTableEmpty(resTable))
            fillResourceTableFromAsset(resTable);

        //Clean up the present prompt table
        deleteAll();

        //Repopulate the prompt table
        //Check if you have enough records for the month
        //Otherwise we need to duplicate data - Probably is a custom prompt
        int tableRecs = getTableCount(resTable);

        if (tableRecs >= numberOfRows) {
            qry = "INSERT INTO \n"
                    + tableName +
                    " SELECT null, "
                    + COLUMN_TASK + ", "
                    + COLUMN_DONE + ", "
                    + COLUMN_COMMENTS + ", "
                    + COLUMN_IMG_LINK + " \n" +
                    " FROM " + resTable + " \n" +
                    " ORDER BY RANDOM() LIMIT " + numberOfRows;
            database.execSQL(qry);
        } else {
            //Find number of necessary repetitions
            int repetitions = Math.round(numberOfRows / tableRecs);

            for (int i = 0; i < repetitions; i++) {
                qry = "INSERT INTO \n"
                        + tableName +
                        " SELECT null, "
                        + COLUMN_TASK + ", "
                        + COLUMN_DONE + ", "
                        + COLUMN_COMMENTS + ", "
                        + COLUMN_IMG_LINK + " \n" +
                        " FROM " + resTable + " \n" +
                        " ORDER BY RANDOM() LIMIT " + tableRecs;
                database.execSQL(qry);
            }

            //Find what is left and fullfill it
            int remaining = numberOfRows % tableRecs;

            qry = "INSERT INTO \n"
                    + tableName +
                    " SELECT null, "
                    + COLUMN_TASK + ", "
                    + COLUMN_DONE + ", "
                    + COLUMN_COMMENTS + ", "
                    + COLUMN_IMG_LINK + " \n" +
                    " FROM " + resTable + " \n" +
                    " ORDER BY RANDOM() LIMIT " + remaining;
            database.execSQL(qry);

            //in this case we also update the resource table with the generated sequence
            //of tasks
            alignTables(tableName, resTable);
        }
    }


    public boolean isTableEmpty() {
        boolean isEmpty = true;

        String qry = "SELECT COUNT(*) FROM " + tableName;

        Cursor cnt = runQuery(qry, null);

        if (!isCursorEmpty(cnt)) {
            cnt.moveToFirst();
            if (cnt.getInt(0) > 0)
                isEmpty = false;
        }

        return isEmpty;
    }

    public boolean isTableEmpty(String tbName) {
        boolean isEmpty = true;

        String qry = "SELECT COUNT(*) FROM " + tbName;

        Cursor cnt = runQuery(qry, null);

        if (!isCursorEmpty(cnt)) {
            cnt.moveToFirst();
            if (cnt.getInt(0) > 0)
                isEmpty = false;
        }

        return isEmpty;
    }

    public Integer getTableCount(String tbName) {
        Integer numOfRecs = 0;

        String qry = "SELECT COUNT(*) FROM " + tbName;

        Cursor cnt = runQuery(qry, null);

        if (!isCursorEmpty(cnt)) {
            cnt.moveToFirst();
            numOfRecs = cnt.getInt(0);
        }
        return numOfRecs;
    }

    public void cleanUpChallengeTable() {

        String qry = "UPDATE \n"
                + tableName +
                " SET " + COLUMN_DONE + " = 0, \n"
                + COLUMN_COMMENTS + "= \"\", "
                + COLUMN_IMG_LINK + "= \"\"";

        database.execSQL(qry);
    }

    public void alignTables(String sourceTable, String targetTable) {

        deleteAll(targetTable);

        String insSelqry = "INSERT INTO " + targetTable + " SELECT * FROM " + sourceTable + ";";
        database.execSQL(insSelqry);

    }

    public boolean fillResourceTableFromAsset(String tableName) {
        boolean res = true;

        try {
            copyDataBase4Upg(databaseName, databaseName + "_tmp");

            String dbPath = DATABASE_PATH + databaseName + "_tmp";
            database.execSQL("attach database '" + dbPath + "'  as tempDB");

            String insSelqry = "INSERT INTO " + tableName + " SELECT * FROM tempDB." + tableName + ";";
            database.execSQL(insSelqry);

            context.deleteDatabase(databaseName + "_tmp");
        } catch (IOException e) {
            e.printStackTrace();
            res = false;
            return res;
        }

        return res;
    }

    public String createCustomPromptTableSet(String promptName) {

        String tableName = null;

        //Parametrized query to find the prompt in the list of the custom prompts
        String[] params = new String[]{promptName};
        String findPromptQry = "SELECT tablename \n" +
                " FROM " + TB_CUSTOM_PROMPTS + " \n" +
                " WHERE promptName =?";

        //Check if the prompt name already exists
        //If it already exists the name is not valid
        Cursor checkTableExists = runQuery(findPromptQry, params);
        if (!isCursorEmpty(checkTableExists))
            return tableName;

        //If it doesn't create the new record
        String qry = "INSERT INTO " + TB_CUSTOM_PROMPTS + " (promptName) VALUES(\"" + promptName + "\")";
        database.execSQL(qry);

        //Now pick up the id table number of the new created table
        Cursor findTableNumber = runQuery(findPromptQry, params);

        if (!isCursorEmpty(findTableNumber)) {
            findTableNumber.moveToFirst();

            tableName = CUSTOM_TABLE_PREFIX + findTableNumber.getString(0);

            qry = "CREATE TABLE \"" + tableName + "\" ( id INTEGER PRIMARY KEY, task TEXT, done INTEGER, comments TEXT DEFAULT '', imglink TEXT)";
            database.execSQL(qry);
            qry = "CREATE TABLE \"" + tableName + TB_RESOURCE_TABLE_SUFFIX + "\" ( id INTEGER PRIMARY KEY, task TEXT, done INTEGER, comments TEXT DEFAULT '', imglink TEXT DEFAULT '')";
            database.execSQL(qry);
        }

        return tableName;
    }

    public void deleteTable(String tableName) {
        String qry = "DROP TABLE IF EXISTS \"" + tableName + "\"";
        database.execSQL(qry);
    }

    public void deleteFromTable(String tableName, String whereClause, String[] param) {

        database.delete(tableName, whereClause, param);
    }


    public boolean isCursorEmpty(Cursor cursor) {
        return !cursor.moveToFirst() || cursor.getCount() == 0;
    }


}


