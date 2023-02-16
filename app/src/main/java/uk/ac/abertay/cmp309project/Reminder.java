package uk.ac.abertay.cmp309project;

public class Reminder { //Reminder objects represent records in the sqlite db

    private int id; //autoincrementing integer | primary key
    private String title;
    private String body_text;
    private boolean alarm;  // stored as int in sqlite: 0 is converted to false - meaning NO ALARM
    private String time;    //time stored as hh:mm:ss
    private int frequency;  //how often the reminder should be sent (once , daily )

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getBodyText() {
        return body_text;
    }
    public void setBodyText(String body_text) {
        this.body_text = body_text;
    }

    public boolean isAlarm() { //if true, an alarm should be set off
        return alarm;
    }
    public void setType(boolean alarm) {
        this.alarm = alarm;
    }

    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }

    public int getFrequency() {return frequency;}
    public void setFrequency(int frequency) {this.frequency = frequency;}
}
