package com.intelligentrescueagent.Framework.DataBase;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Angel Buzany on 04/07/2016.
 */
public class DataBaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "CitizenAlarm.db";
    public static final String TABLE_NAME = "Users";
    public static final String COL_Id = "id";
    public static final String COL_FB_ID = "fb_id";
    public static final String COL_ALIAS = "alias";
    public static final String COL_EMAIL = "email";

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME +" (id INTEGER PRIMARY KEY AUTOINCREMENT,fb_id TEXT,alias TEXT,email text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    public void insertUser(String fbId, String alias, String email){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO Users VALUES(NULL,'"+ fbId +"','"+ alias +"', '"+ email +"')");
    }
    
    public Cursor selectUserByFbId(String fbId){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor rs =  db.rawQuery("select * from users", null);

        return  rs;
    }
}
