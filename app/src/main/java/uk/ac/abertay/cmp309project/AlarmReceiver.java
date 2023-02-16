package uk.ac.abertay.cmp309project;

import android.app.IntentService;
import android.app.KeyguardManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    private static Ringtone ringtone;
    private static Vibrator vibrator;
    private static String channel_id = "AlarmReceiverChannel";
    static NotificationManager manager;

    //SUMMARY
    // runs when the AlarmReceiver receives an intent
    // -unpack the intent extras into a reminder object
    // -create a notification channel
    // -call the buildNotification method using context and the reminder object
    @Override
    public void onReceive(Context context, Intent intent) {

        Reminder reminder = new Reminder();         //unpack reminder obj from intent

        reminder.setId(intent.getIntExtra("id", 0));
        reminder.setTitle(intent.getStringExtra("title"));
        reminder.setBodyText(intent.getStringExtra("text"));
        reminder.setType(intent.getBooleanExtra("type", false));
        reminder.setFrequency(intent.getIntExtra("frequency", 0));
        reminder.setTime(intent.getStringExtra("time"));


        manager = context.getSystemService(NotificationManager.class);
        CharSequence name = "ReminderAlarms";
        String desc = "Alarm-Notification Channel";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(channel_id, name, importance);
        channel.setDescription(desc);
        manager.createNotificationChannel(channel);

        buildNotification(context, reminder);
    }

    //SUMMARY
    // fullscreen_intent - this intent starts the FullscreenActivity, which displays the alarm over the lock screen
    // ontap_intent = this intent calls the inner class AlarmDismissService, which then stops all alarm functions and opens MainActivity
    // dismiss_intent - this intent calls the inner class AlarmDismissService, which then stops all alarm functions
    private void buildNotification(Context context,Reminder reminder){

        // fullscreen_intent is for displaying an alarm when the phone is asleep / locked
        Intent fullscreen_intent = new Intent(context, FullscreenActivity.class);
        fullscreen_intent.putExtra("id", reminder.getId());        //pack reminder vals into intent
        fullscreen_intent.putExtra("title", reminder.getTitle());
        fullscreen_intent.putExtra("text", reminder.getBodyText());
        fullscreen_intent.putExtra("type", reminder.isAlarm());
        fullscreen_intent.putExtra("frequency", reminder.getFrequency());
        fullscreen_intent.putExtra("time", reminder.getTime());
        fullscreen_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent fullscreen_pending = PendingIntent.getActivity(context, 6, fullscreen_intent,
                PendingIntent.FLAG_CANCEL_CURRENT); // have to assign FLAG_CANCEL_CURRENT here or
                                                    // FullscreenActivity.onNewIntent will run multiple times and
                                                    // overwrite the intent extras...


        //on_tap intent is for when the user taps ON THE NOTIFICATION
        // - cancels notification and then opens main activity
        Intent ontap_intent = new Intent(context, AlarmDismissService.class);
        ontap_intent.putExtra("action", 0);
        ontap_intent.putExtra("id", reminder.getId());
        ontap_intent.putExtra("title", reminder.getTitle());
        ontap_intent.putExtra("text", reminder.getBodyText());
        ontap_intent.putExtra("type", reminder.isAlarm());
        ontap_intent.putExtra("frequency", reminder.getFrequency());
        ontap_intent.putExtra("time", reminder.getTime());
        PendingIntent ontap_pending = PendingIntent.getService(context, 99, //arbitrary unique code
                ontap_intent, PendingIntent.FLAG_UPDATE_CURRENT);


        //dismiss intent is for when the user taps ON THE DISMISS ACTION BUTTON
        // - cancels notification but does not open any activity
        Intent dismiss_intent = new Intent(context, AlarmDismissService.class);
        dismiss_intent.putExtra("action", 1);
        dismiss_intent.putExtra("id", reminder.getId());
        dismiss_intent.putExtra("title", reminder.getTitle());
        dismiss_intent.putExtra("text", reminder.getBodyText());
        dismiss_intent.putExtra("type", reminder.isAlarm());
        dismiss_intent.putExtra("frequency", reminder.getFrequency());
        dismiss_intent.putExtra("time", reminder.getTime());
        PendingIntent dismiss_pending = PendingIntent.getService(context, 98, //arbitrary unique code
                dismiss_intent, PendingIntent.FLAG_UPDATE_CURRENT);


        //build the alarm notification
        NotificationCompat.Builder builder = new NotificationCompat
                .Builder(context, channel_id)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(reminder.getTitle())
                .setContentText(reminder.getBodyText())
                .setAutoCancel(true)
                .setContentIntent(ontap_pending)                // <- tapping the notification will send user to MainActivity
                .setPriority(NotificationCompat.PRIORITY_MAX)   // <- higher priority means the alarm is more likely to appear on the lock screen
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .addAction(R.drawable.deleteicon, "Dismiss", dismiss_pending)
                .setFullScreenIntent(fullscreen_pending, true); //fullscreen activity will appear if phone is locked

        KeyguardManager km = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);

        if(!km.isKeyguardLocked()){
            //trigger alarm audio and vibrate if phone is awake
            triggerRingtone(context);
            triggerVibrate(context);
        }

        manager.notify(reminder.getId(), builder.build()); //send the notification
    }


    //SUMMARY
    // triggers a vibration effect when an alarm goes off
    // REQUIRES VIBRATE PERMISSIONS!
    private void triggerVibrate(Context context) {
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        //vibrate for 4 seconds at default intensity
        VibrationEffect vibe = VibrationEffect.createOneShot(4000, VibrationEffect.DEFAULT_AMPLITUDE);
        vibrator.vibrate(vibe);
    }


    //SUMMARY
    // plays the default alarm ringtone when an alarm goes off
    private void triggerRingtone(Context context) {
        //Get the default alarm sound: if none is set, use the notification sound instead
        Uri alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarm == null) {
            alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        //play sound effect
        ringtone = RingtoneManager.getRingtone(context, alarm);
        ringtone.play();
    }


    //SUMMARY
    //this method is called by the AlarmDismissService when a user taps on the Dismiss action button
    // -stops ringtone audio, cancels any vibrations and dismisses the notification
    public static void stopAlarm(int id){
        try {
            ringtone.stop();
            vibrator.cancel();
        }catch (Exception e){
            Log.e("AlarmReceiver.stopAlarm", e.getMessage().toString());
        }
        manager.cancel(id);
    }


    //SUMMARY
    // inner class extending IntentService
    // -registered in manifest inside <service> tag
    // -used to trigger action when 'Dismiss' action button on notification is pressed
    public static class AlarmDismissService extends IntentService {

        public AlarmDismissService() { //empty constructor
            super("AlarmDismissService");
        }

        //SUMMARY
        //called when a user taps on the Dismiss action button OR on the notification
        // - tapping on the notification will stop the alarm, clear notification then open the MainActivity
        // - tapping on 'Dismiss' will stop alarm and clear notification, but the activity will not begin
        @Override
        protected void onHandleIntent(Intent intent) {
            int action = intent.getIntExtra("action", 0);

            switch(action){
                case 0:
                    //tapped on notification
                    rescheduleOrCancel(intent);

                    Intent main_intent = new Intent(this, MainActivity.class);
                    PendingIntent main_pending = PendingIntent.getActivity(getApplicationContext(), 97, main_intent, 0);

                    try {
                        main_pending.send(); //attempt to start MainActivity
                    }
                    catch (Exception e){
                        Log.e("AlarmReceiver.DismissService.onHandleIntent | try/catch", e.getMessage().toString());
                    }
                    break;

                case 1:
                    //tapped on Dismiss
                    rescheduleOrCancel(intent);
                    break;
            }

        }

        //SUMMARY
        //reschedule DAILY alarms for the same time but +1 day
        private void rescheduleOrCancel(Intent intent){

            //unpack reminder
            Reminder reminder = new Reminder();
            reminder.setId(intent.getIntExtra("id", 0));
            reminder.setTitle(intent.getStringExtra("title"));
            reminder.setBodyText(intent.getStringExtra("text"));
            reminder.setType(intent.getBooleanExtra("type", false));
            reminder.setFrequency(intent.getIntExtra("frequency", 0));
            reminder.setTime(intent.getStringExtra("time"));
            int id = intent.getIntExtra("id", 0);
            int freq = intent.getIntExtra("frequency", 0);

            if (freq > 0){ //daily alarm, need to cancel then set new alarm for 1 day later
                AlarmReceiver.stopAlarm(id);
                AlarmHandler.getInstance(getApplicationContext()).cancelAlarmIntent(getApplicationContext(), reminder.getId());

                //reschedules the alarm for the same time tomorrow
                AlarmHandler.getInstance(getApplicationContext()).registerIntent(reminder, getApplicationContext());
            }
            else{ //single time alarm, just cancel the alarm intent
                AlarmReceiver.stopAlarm(id);
                AlarmHandler.getInstance(getApplicationContext()).cancelAlarmIntent(getApplicationContext(), reminder.getId());
            }

        }
    }
}
