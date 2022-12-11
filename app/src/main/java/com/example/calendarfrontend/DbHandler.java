package com.example.calendarfrontend;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class DbHandler {
    public static void createTable(SQLiteDatabase db, String name)
    {
        String Table ="create table IF NOT EXISTS " + name + "(_id integer primary key autoincrement, schemeID integer, confID integer, year integer,month integer,day integer,startTime text,endTime text,title text,location text)";
        db.execSQL(Table);
    }

    public static void dropTable(SQLiteDatabase db, String tableName)
    {
        db.execSQL("drop table " + tableName);
    }

    public static void insertScheme(SQLiteDatabase db, String tableName, Scheme scheme)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put("schemeID", scheme.getId());
        contentValues.put("confID", scheme.getConf_id());
        contentValues.put("year", scheme.getYear());
        contentValues.put("month", scheme.getMonth());
        contentValues.put("day", scheme.getDay());
        contentValues.put("startTime", scheme.getStartTime());
        contentValues.put("endTime", scheme.getEndTime());
        contentValues.put("title", scheme.getTitle());
        contentValues.put("location", scheme.getLocation());
        db.insert(tableName, null, contentValues);
    }

    public static void deleteScheme(SQLiteDatabase db, String tableName, Scheme scheme)
    {
        db.delete(tableName, "schemeID=? and year=? and month=? and day=? and startTime=? and endTime=? and title=? and location=?", new String[]{String.valueOf(scheme.getId()), String.valueOf(scheme.getYear()), String.valueOf(scheme.getMonth()), String.valueOf(scheme.getDay()), scheme.getStartTime(), scheme.getEndTime(), scheme.getTitle(), scheme.getLocation()});
    }

    public static ArrayList<Scheme> queryScheme(SQLiteDatabase db, String tableName, Scheme scheme)
    {
        ArrayList<Scheme> res = new ArrayList<>();
        String task = "select * from " + tableName + " where title=?";
        Cursor cursor = db.rawQuery(task, new String[]{scheme.getTitle()});
        while (cursor.moveToNext())
        {
            Scheme temp = new Scheme();
            temp.setId(cursor.getInt(1));
            temp.setConf_id(cursor.getInt(2));
            temp.setYear(cursor.getInt(3));
            temp.setMonth(cursor.getInt(4));
            temp.setDay(cursor.getInt(5));
            temp.setStartTime(cursor.getString(6));
            temp.setEndTime(cursor.getString(7));
            temp.setTitle(cursor.getString(8));
            temp.setLocation(cursor.getString(9));
            res.add(temp);
        }
        cursor.close();
        return res;
    }

    public static Scheme queryScheme(SQLiteDatabase db, String tableName, String title)
    {
        Scheme temp = new Scheme();
        String task = "select * from " + tableName + " where title=?";
        Cursor cursor = db.rawQuery(task, new String[]{title});
        while (cursor.moveToNext())
        {
            temp.setId(cursor.getInt(1));
            temp.setConf_id(cursor.getInt(2));
            temp.setYear(cursor.getInt(3));
            temp.setMonth(cursor.getInt(4));
            temp.setDay(cursor.getInt(5));
            temp.setStartTime(cursor.getString(6));
            temp.setEndTime(cursor.getString(7));
            temp.setTitle(cursor.getString(8));
            temp.setLocation(cursor.getString(9));
            break;
        }
        cursor.close();
        return temp;
    }

    public static ArrayList<Scheme> fetchScheme(SQLiteDatabase db, String tableName, int year, int month, int day)
    {
        ArrayList<Scheme> res = new ArrayList<>();
        String task = "select * from " + tableName + " where year=? and month=? and day=?";
        Cursor cursor = db.rawQuery(task, new String[]{String.valueOf(year), String.valueOf(month), String.valueOf(day)});
        while (cursor.moveToNext())
        {
            Scheme temp = new Scheme();
            temp.setId(cursor.getInt(1));
            temp.setConf_id(cursor.getInt(2));
            temp.setYear(cursor.getInt(3));
            temp.setMonth(cursor.getInt(4));
            temp.setDay(cursor.getInt(5));
            temp.setStartTime(cursor.getString(6));
            temp.setEndTime(cursor.getString(7));
            temp.setTitle(cursor.getString(8));
            temp.setLocation(cursor.getString(9));
            res.add(temp);
        }
        cursor.close();
        return res;
    }

    public static void updateScheme(SQLiteDatabase db, String tableName, Scheme oldscheme, Scheme newScheme)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put("schemeID", newScheme.getId());
        contentValues.put("confID", newScheme.getConf_id());
        contentValues.put("year", newScheme.getYear());
        contentValues.put("month", newScheme.getMonth());
        contentValues.put("day", newScheme.getDay());
        contentValues.put("startTime", newScheme.getStartTime());
        contentValues.put("endTime", newScheme.getEndTime());
        contentValues.put("title", newScheme.getTitle());
        contentValues.put("location", newScheme.getLocation());
        db.update(tableName, contentValues, "schemeID=? and confID=? and year=? and month=? and day=? and startTime=? and endTime=? and title=? and location=?", new String[]{String.valueOf(oldscheme.getId()), String.valueOf(oldscheme.getConf_id()), String.valueOf(oldscheme.getYear()), String.valueOf(oldscheme.getMonth()), String.valueOf(oldscheme.getDay()), oldscheme.getStartTime(), oldscheme.getEndTime(), oldscheme.getTitle(), oldscheme.getLocation()});
    }
}
