package com.example.calendarfrontend;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class DbHandler {
    public static void createTable(SQLiteDatabase db, String name)
    {
        String Table ="create table IF NOT EXISTS " + name + "(_id integer primary key autoincrement, schemeID integer, confID integer, year integer,month integer,day integer,startTime text,endTime text,title text,location text, rawtext text)";
        db.execSQL(Table);
    }

    public static void dropTable(SQLiteDatabase db, String tableName)
    {
        db.execSQL("drop table " + tableName);
    }

    public static void insertScheme(SQLiteDatabase sQLiteDatabase, String str, Scheme scheme) {
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
        contentValues.put("rawtext", scheme.getRaw_text());
        sQLiteDatabase.insert(str, (String) null, contentValues);
    }

    public static void deleteScheme(SQLiteDatabase sQLiteDatabase, String str, Scheme scheme) {
        sQLiteDatabase.delete(str, "schemeID=? and year=? and month=? and day=? and startTime=? and endTime=? and title=? and location=? and rawtext=?", new String[]{String.valueOf(scheme.getId()), String.valueOf(scheme.getYear()), String.valueOf(scheme.getMonth()), String.valueOf(scheme.getDay()), scheme.getStartTime(), scheme.getEndTime(), scheme.getTitle(), scheme.getLocation(), scheme.getRaw_text()});
    }

    public static ArrayList<Scheme> queryScheme(SQLiteDatabase sQLiteDatabase, String str, Scheme scheme) {
        ArrayList<Scheme> arrayList = new ArrayList<>();
        Cursor rawQuery = sQLiteDatabase.rawQuery("select * from " + str + " where title=?", new String[]{scheme.getTitle()});
        while (rawQuery.moveToNext()) {
            Scheme scheme2 = new Scheme();
            scheme2.setId(rawQuery.getInt(1));
            scheme2.setConf_id(rawQuery.getInt(2));
            scheme2.setYear(rawQuery.getInt(3));
            scheme2.setMonth(rawQuery.getInt(4));
            scheme2.setDay(rawQuery.getInt(5));
            scheme2.setStartTime(rawQuery.getString(6));
            scheme2.setEndTime(rawQuery.getString(7));
            scheme2.setTitle(rawQuery.getString(8));
            scheme2.setLocation(rawQuery.getString(9));
            arrayList.add(scheme2);
        }
        rawQuery.close();
        return arrayList;
    }

    public static Scheme queryScheme(SQLiteDatabase sQLiteDatabase, String str, String str2) {
        Scheme scheme = new Scheme();
        Cursor rawQuery = sQLiteDatabase.rawQuery("select * from " + str + " where title=?", new String[]{str2});
        if (rawQuery.moveToNext()) {
            scheme.setId(rawQuery.getInt(1));
            scheme.setConf_id(rawQuery.getInt(2));
            scheme.setYear(rawQuery.getInt(3));
            scheme.setMonth(rawQuery.getInt(4));
            scheme.setDay(rawQuery.getInt(5));
            scheme.setStartTime(rawQuery.getString(6));
            scheme.setEndTime(rawQuery.getString(7));
            scheme.setTitle(rawQuery.getString(8));
            scheme.setLocation(rawQuery.getString(9));
            scheme.setRaw_text(rawQuery.getString(10));
        }
        rawQuery.close();
        return scheme;
    }

    public static ArrayList<Scheme> fetchScheme(SQLiteDatabase sQLiteDatabase, String str, int i, int i2, int i3) {
        ArrayList<Scheme> arrayList = new ArrayList<>();
        Cursor rawQuery = sQLiteDatabase.rawQuery("select * from " + str + " where year=? and month=? and day=?", new String[]{String.valueOf(i), String.valueOf(i2), String.valueOf(i3)});
        while (rawQuery.moveToNext()) {
            Scheme scheme = new Scheme();
            scheme.setId(rawQuery.getInt(1));
            scheme.setConf_id(rawQuery.getInt(2));
            scheme.setYear(rawQuery.getInt(3));
            scheme.setMonth(rawQuery.getInt(4));
            scheme.setDay(rawQuery.getInt(5));
            scheme.setStartTime(rawQuery.getString(6));
            scheme.setEndTime(rawQuery.getString(7));
            scheme.setTitle(rawQuery.getString(8));
            scheme.setLocation(rawQuery.getString(9));
            scheme.setRaw_text(rawQuery.getString(10));
            arrayList.add(scheme);
        }
        rawQuery.close();
        return arrayList;
    }

    public static void updateScheme(SQLiteDatabase sQLiteDatabase, String str, Scheme scheme, Scheme scheme2) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("schemeID", scheme2.getId());
        contentValues.put("confID", scheme2.getConf_id());
        contentValues.put("year", scheme2.getYear());
        contentValues.put("month", scheme2.getMonth());
        contentValues.put("day", scheme2.getDay());
        contentValues.put("startTime", scheme2.getStartTime());
        contentValues.put("endTime", scheme2.getEndTime());
        contentValues.put("title", scheme2.getTitle());
        contentValues.put("location", scheme2.getLocation());
        contentValues.put("rawtext", scheme2.getRaw_text());
        sQLiteDatabase.update(str, contentValues, "schemeID=? and confID=? and year=? and month=? and day=? and startTime=? and endTime=? and title=? and location=? and rawtext=?", new String[]{String.valueOf(scheme.getId()), String.valueOf(scheme.getConf_id()), String.valueOf(scheme.getYear()), String.valueOf(scheme.getMonth()), String.valueOf(scheme.getDay()), scheme.getStartTime(), scheme.getEndTime(), scheme.getTitle(), scheme.getLocation(), scheme.getRaw_text()});
    }
}
