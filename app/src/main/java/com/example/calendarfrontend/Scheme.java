package com.example.calendarfrontend;

public class Scheme {
    private int id;
    private int conf_id;
    private int year;
    private int month;
    private int day;
    private String title;
    private String location;
    private String raw_text;
    private String startTime;
    private String endTime;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getConf_id() {
        return conf_id;
    }
    public void setConf_id(int conf_id) {
        this.conf_id = conf_id;
    }
    public int getYear() {
        return year;
    }
    public void setYear(int year) {
        this.year = year;
    }
    public int getMonth() {
        return month;
    }
    public void setMonth(int month) {
        this.month = month;
    }
    public int getDay() {
        return day;
    }
    public void setDay(int day) {
        this.day = day;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public String getStartTime() {
        return startTime;
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    public String getEndTime() {
        return endTime;
    }
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    public String getRaw_text() {
        return raw_text;
    }
    public void setRaw_text(String raw_text) {
        this.raw_text = raw_text;
    }
}
