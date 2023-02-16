package uk.ac.abertay.cmp309project;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {

    NotificationManager manager;
    private static String channel_id = "NotificationReceiverChannel";


    //SUMMARY
    //runs when the pending intent is triggered - at the time set by the user
    //- unpack Reminder object values from intent
    //- create a NotificationManager and a NotificationChannel
    //- build the Notification using the Reminder obj values then push to display
    @Override
    public void onReceive(Context context, Intent intent) {

        Reminder reminder = new Reminder(); //unpack reminder obj from intent
        reminder.setId(intent.getIntExtra("id", 0));
        reminder.setTitle(intent.getStringExtra("title"));
        reminder.setBodyText(intent.getStringExtra("text"));
        reminder.setType(intent.getBooleanExtra("type", false));
        reminder.setFrequency(intent.getIntExtra("frequency", 0));
        reminder.setTime(intent.getStringExtra("time"));

        manager = context.getSystemService(NotificationManager.class);
        CharSequence name = "ReminderNotifications";
        String desc = "Notification Channel";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(channel_id, name, importance);
        channel.setDescription(desc);
        manager.createNotificationChannel(channel);

        buildNotification(context, reminder);
    }


    //SUMMARY
    //build notification to display reminder content
    //tapping notification will start the NotificationDismissService,
    //      cancelling the pendingintent and sending the user to the main activity
    private void buildNotification(Context context, Reminder reminder){
        Intent ontap_intent = new Intent(context, NotificationDismissService.class);
        ontap_intent.putExtra("id", reminder.getId());
        PendingIntent ontap_pending = PendingIntent.getService(context, 99, ontap_intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat
                .Builder(context, channel_id)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(reminder.getTitle())
                .setContentText(reminder.getBodyText())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(ontap_pending); //fire intent when user taps on notification

        manager.notify(reminder.getId(), builder.build());
    }


    //SUMMARY
    // inner class extending IntentService
    // -registered in manifest inside <service> tag
    // -used to clear pending intent and redirect user
    public static class NotificationDismissService extends IntentService {

        public NotificationDismissService(){super("NotificationDismissService");}

        @Override
        protected void onHandleIntent(Intent intent){

            int id = intent.getIntExtra("id", 0);
            AlarmHandler.getInstance(getApplicationContext())   //clears the old notification intent
                    .cancelNotificationIntent(getApplicationContext(), id);// so new notifications show the correct content

            Intent main_intent = new Intent(this, MainActivity.class);
            PendingIntent main_pending = PendingIntent.getActivity(getApplicationContext(), 97, main_intent, 0);

            try{
                main_pending.send();
            }catch (Exception e){
                Log.e("NotificationReceiver.DismissService.onHandleIntent | try/catch", e.getMessage().toString());
            }

        }
    }

}
