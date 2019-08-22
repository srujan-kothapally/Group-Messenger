package edu.buffalo.cse.cse486586.groupmessenger2;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static android.content.ContentValues.TAG;

public class sqlitedb extends SQLiteOpenHelper {
    private static final String
            DB_NAME="GroupMessenger";
    public sqlitedb(Context context){
        super(context,DB_NAME,null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE GroupMessenger ('key' STRING PRIMARY KEY , value STRING);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS GroupMessenger");
        onCreate(db);
    }
    public void addrow(ContentValues values){
        getWritableDatabase().insert("GroupMessenger","key",values);
    }
    public String getTableAsString(sqlitedb db, String tableName) {
        Log.d(TAG, "getTableAsString called");
        String tableString = String.format("Table %s:\n", tableName);
        String[] x = {"key0"};
        Cursor allRows  = db.getReadableDatabase().rawQuery("select * from GroupMessenger", null);
        if (allRows.moveToFirst() ){
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name: columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (allRows.moveToNext());
        }

        return tableString;
    }

}


