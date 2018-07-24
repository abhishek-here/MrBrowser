package sb.mrbrowser;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ABHISHEK on 7/9/2017.
 */

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "abhis.db";
    private static final String TABLE_NAME = "abhis";
    private static final String NAME="name";
    private static final String URL="url";
    private static final String CREATE_TABLE = " CREATE TABLE " + TABLE_NAME + "(" + NAME + " TEXT PRIMARY KEY, " +
            URL +  " TEXT NOT NULL "+")";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_TABLE);
    }
    public void onUpgrade(SQLiteDatabase db,int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXIST " + TABLE_NAME);
        onCreate(db);
    }
    public boolean insertData(String name,String url){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put("NAME",name);
        contentValues.put("URL",url);
        long result= db.insert(TABLE_NAME,null,contentValues);
        if (result==-1)
            return false;
        else
            return true;
    }
    public Cursor getAllData() {
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor data=db.rawQuery("SELECT * FROM " + TABLE_NAME,null);
        return data;
    }
    public void deleteRow(String value){
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + URL + "= '" +value+"'");
        db.close();
    }
}
