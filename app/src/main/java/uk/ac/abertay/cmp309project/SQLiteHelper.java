package uk.ac.abertay.cmp309project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class SQLiteHelper extends SQLiteOpenHelper {

    //database components
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "RemindersDatabase";
    private static final String TABLE_NAME = "Reminders";

            //title -the notification title
            //text -the body text for the notification
            //type -should the notification set off an alarm?
            //time -when to fire the notification
    //ignore this error if one appears, the IDE is lying and WILL NOT break due to this...
    private static final String[] COLUMN_NAMES = {"ID", "Title", "Text", "Type", "Frequency", "Time"};
    private static SQLiteHelper sqlite_instance = null;
    private String create_string =
                    "CREATE TABLE " +TABLE_NAME+ " ("+
                    COLUMN_NAMES[0] + " INTEGER PRIMARY KEY, " + //ID value so rows can be accessed
                    COLUMN_NAMES[1] + " TEXT, " +
                    COLUMN_NAMES[2] + " TEXT, " +
                    COLUMN_NAMES[3] + " INTEGER, " +// 1 = alarm, 0 = notification
                    COLUMN_NAMES[4] + " INTEGER, " +// 1 = daily, 0 = once
                    COLUMN_NAMES[5] + " TEXT);";//Time should be stored as String in ISO-8601 format
                                                // for example: hh:mm / 22:52 is 10:52PM

    private SQLiteHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public static SQLiteHelper getInstance(Context context){
        if(sqlite_instance == null){
            sqlite_instance = new SQLiteHelper(context); //create a new instance if none exists
        }
        return sqlite_instance;
    }


    //SUMMARY
    //takes an integer value and attempts to remove the corresponding db row
    //returns true if any rows were affected
    public boolean deleteReminder(int id){ //removes row from database
        SQLiteDatabase database = this.getWritableDatabase();

        //return based on # of rows affected, 0 means deletion failed so return false
        return database.delete(TABLE_NAME, "ID=?", new String[]{String.valueOf(id)}) > 0;
    }

    //SUMMARY
    //receives a reminder object that has been edited
    //overwrites reminder in db with matching ID column
    //returns the number of rows affected by the edit
    public int editReminder(Reminder reminder){

        int affected =0; //# of rows affected by edit -> should be 1 on success
        SQLiteDatabase database = getWritableDatabase();
        try{

            ContentValues row = new ContentValues();    //populate new row with edited data
            row.put(COLUMN_NAMES[1], reminder.getTitle());
            row.put(COLUMN_NAMES[2], reminder.getBodyText());
            row.put(COLUMN_NAMES[3], reminder.isAlarm());
            row.put(COLUMN_NAMES[4], reminder.getFrequency());
            row.put(COLUMN_NAMES[5], reminder.getTime());

            //update the database with the edited row
            affected = database.update(TABLE_NAME, row, "ID = ?",
                                        new String[]{String.valueOf(reminder.getId())});
            database.close();

            //since only 1 Reminder is being updated, affected should be 1
            if(affected == 1){EditReminderActivity.getInstance().showToast("Updated successfully!");}

        }catch(Exception e){
            //should be unreachable but prevents crash
        }
        return affected;
    }


    //SUMMARY
    //takes a Reminder object -created from user input- and inserts it as a new row in database
    public void saveReminder(Reminder reminder){
        SQLiteDatabase database = getWritableDatabase();
        try{

            ContentValues row = new ContentValues();
            row.put(COLUMN_NAMES[1], reminder.getTitle());   //add object values to row, COLUMN_NAMES[0]
            row.put(COLUMN_NAMES[2], reminder.getBodyText()); // is excluded as it will autoincrement
            row.put(COLUMN_NAMES[3], reminder.isAlarm());
            row.put(COLUMN_NAMES[4], reminder.getFrequency());
            row.put(COLUMN_NAMES[5], reminder.getTime());

            database.insert(TABLE_NAME, null, row); //insert row and close database
            database.close();
        }catch(Exception e){
            //should be unreachable but handles unexpected exceptions
        }
    }

    //SUMMARY
    //loads only the final row in the database
    //used for retrieving an incremented ID value from the database before creating alarm
    public Reminder loadLastReminder(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor result = db.query(TABLE_NAME,COLUMN_NAMES, null, null, null, null, null, null);

        Reminder reminder = new Reminder();         //create a reminder object
        if(result.getCount() > 0){
            result.moveToLast(); //go the the last entry


            reminder.setId(result.getInt(0));        //populate reminder with table data
            reminder.setTitle(result.getString(1));
            reminder.setBodyText(result.getString(2));

            if(result.getInt(3) == 0){     //if type is false, no alarm should be set
                reminder.setType(false);
            }else{ reminder.setType(true);}

            reminder.setFrequency(result.getInt(4));
            reminder.setTime(result.getString(5)); //time as string in hh:MM 24 hour format
        }
        return reminder;
    }


    //SUMMARY
    //check for entries in the database and return any found as list of Reminder objects
    public List<Reminder> loadReminders(){

        List<Reminder> reminders = new ArrayList<>();//create a list of Reminder objects
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor result = db.query(TABLE_NAME, COLUMN_NAMES, null, null, null, null, null, null);

        if(result.getCount() > 0){ //if there are entries

            result.moveToPosition(0);
            for(int i=0; i<result.getCount(); i++){

                Reminder reminder = new Reminder();         //create a reminder object
                reminder.setId(result.getInt(0));        //then populate it with table data
                reminder.setTitle(result.getString(1));
                reminder.setBodyText(result.getString(2));

                if(result.getInt(3) == 0){     //if type is false, no alarm should be set
                    reminder.setType(false);
                }else{ reminder.setType(true);}

                reminder.setFrequency(result.getInt(4));
                reminder.setTime(result.getString(5)); //time as string in hh:MM 24 hour format

                reminders.add(reminder); //once object is full, add it to the list
                result.moveToNext();     //then move onto the next item in result list
            }
        }else{
            //MainActivity.getInstance().showToast("No records in database!");
        }
        return reminders;
    }


    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(create_string); //create the database if it doesn't exist
    }


    @Override
    public void onUpgrade(SQLiteDatabase database, int i, int i1) {

    }
}
