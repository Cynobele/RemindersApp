package uk.ac.abertay.cmp309project;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.ALARM_SERVICE;

public class AlarmHandler { //handles the setting and triggering of alarms for the app

    private static AlarmHandler instance = null;
    AlarmManager manager;

    private AlarmHandler(){} //empty constructor

    public static AlarmHandler getInstance(Context context){
        if(instance == null){
            instance = new AlarmHandler();
        }
        return instance;
    }

    //SUMMARY
    //receive a reminder object
    //if the reminder type is alarm, register an alarm PendingIntent
    //otherwise, register a notification PendingIntent
    public void registerIntent(Reminder reminder, Context context){

        manager = (AlarmManager)context.getSystemService(ALARM_SERVICE);
        if(reminder.isAlarm()){
            setAlarm(reminder, context, manager);
        }else{
            setNotification(reminder, context, manager);
        }
    }

    //SUMMARY
    //should be called when MainActivity is launched via tapping a notification
    //clears the pending intent that creates the alarm
    public void cancelAlarmIntent(Context context, int id){
        manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent cancel_intent = new Intent(context, AlarmReceiver.class);
        PendingIntent cancel_pending = PendingIntent.getBroadcast(context, id, cancel_intent, 0);

        manager.cancel(cancel_pending); //cancel the alarm fired by the intent
        cancel_pending.cancel();        //cancel the intent
    }


    //SUMMARY
    //sets an exact alarm using a pending intent
    // -pending intent has FLAG_CANCEL_CURRENT : any duplicate pending intent found will be overwritten
    //      this allows us to change what time the alarm is triggered, if the user edits the reminder
    // -pending intent uses reminder.getId() as the request code, so we can edit and remove the correct
    //      alarm by referencing its id
    // - setExactAndAllowWhileIdle() and RTC_WAKEUP allow this alarm to bypass Doze/idle modes
    private void setAlarm(Reminder reminder, Context context, AlarmManager manager){
        Intent intent = new Intent(context, AlarmReceiver.class); //explicit intent for AlarmReceiver
        intent.putExtra("id", reminder.getId());        //pack reminder vals into intent
        intent.putExtra("title", reminder.getTitle());
        intent.putExtra("text", reminder.getBodyText());
        intent.putExtra("type", reminder.isAlarm());
        intent.putExtra("frequency", reminder.getFrequency());
        intent.putExtra("time", reminder.getTime());

        PendingIntent pending = PendingIntent.getBroadcast(context, reminder.getId(), intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        //Convert string time value to useable formats
        String str_time = reminder.getTime();
        long time = convertTimeToLong(str_time);
        Date date = new Date(time);
        Format format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        MainActivity.getInstance().showToast("Alarm set for: "+format.format(date));
        manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pending);

    }

    //SUMMARY
    //should be called when MainActivity is launched via tapping a notification
    //clears the pending intent that created the notification
    public void cancelNotificationIntent(Context context, int id){
        manager = (AlarmManager)context.getSystemService(ALARM_SERVICE);
        Intent cancel_intent = new Intent(context, NotificationReceiver.class);
        PendingIntent cancel_pending = PendingIntent.getBroadcast(context, id, cancel_intent, 0);

        manager.cancel(cancel_pending);
        cancel_pending.cancel();
    }


    //SUMMARY
    //- pack the Reminder object's values into an intent, which will start the NotificationReceiver
    //- register a PendingIntent to trigger at Reminder's time val
    private void setNotification(Reminder reminder, Context context, AlarmManager manager){
        Intent intent = new Intent(context, NotificationReceiver.class); //explicit intent for NotificationReceiver

        intent.putExtra("id", reminder.getId());        //pack reminder vals into intent
        intent.putExtra("title", reminder.getTitle());
        intent.putExtra("text", reminder.getBodyText());
        intent.putExtra("type", reminder.isAlarm());
        intent.putExtra("frequency", reminder.getFrequency());
        intent.putExtra("time", reminder.getTime());

        PendingIntent pending = PendingIntent.getBroadcast(context, reminder.getId(), intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        //Convert string time value to useable formats
        String str_time = reminder.getTime();
        long time = convertTimeToLong(str_time);
        Date date = new Date(time);
        Format format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        Toast.makeText(context,
                "Notification set for: "+format.format(date), Toast.LENGTH_LONG).show();

        //RTC_WAKEUP allows the alarm to trigger even if phone is asleep
        manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pending);
    }


    //SUMMARY
    //convert the time from String HH:MM to a usable long value
    private long convertTimeToLong(String time){
        int hour, min;
        hour = Integer.parseInt(time.substring(0,2)); //first 2 chars are hour HH:MM
        min = Integer.parseInt(time.substring(3,5));  //last 2 are mins, ignore colon

        Calendar calendar = Calendar.getInstance(); //get the trigger time as a long

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if(hour < Calendar.getInstance().get(Calendar.HOUR_OF_DAY)||    //if the chosen hour is earlier than now OR
                (hour == Calendar.getInstance().get(Calendar.HOUR_OF_DAY)  //the hour is the same, but mins is earlier
                        && min <= Calendar.getInstance().get(Calendar.MINUTE))){

            calendar.add(Calendar.DAY_OF_YEAR,1); //add 1 day to the time so it doesn't fire immediately

            //adds 10 minutes, for testing
            //calendar.add(Calendar.MINUTE, 10);
        }
        return calendar.getTimeInMillis();
    }
}
