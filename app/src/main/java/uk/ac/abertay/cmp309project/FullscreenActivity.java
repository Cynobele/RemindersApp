package uk.ac.abertay.cmp309project;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FullscreenActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView fullscreen_title, fullscreen_time;
    private Button dismiss;
    private Ringtone ringtone; //plays alarm sound
    private Vibrator vibrator; // vibrates device during alarm

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setShowWhenLocked(true);
        setTurnScreenOn(true); //show on locked phone / wake up
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        KeyguardManager km = (KeyguardManager) getSystemService(this.KEYGUARD_SERVICE);
        km.requestDismissKeyguard(this, null);


        fullscreen_title = findViewById(R.id.fullscreen_title); //attach ui elements
        fullscreen_time = findViewById(R.id.fullscreen_time);
        dismiss = findViewById(R.id.dismiss);

        Reminder reminder = new Reminder(); //unpack required reminder values from intent
        Intent pack = getIntent();
        reminder.setTitle(pack.getStringExtra("title"));
        reminder.setTime(pack.getStringExtra("time"));

        fullscreen_title.setText(reminder.getTitle()); //display reminder values on screen
        fullscreen_time.setText(reminder.getTime());
        dismiss.setText("Dismiss");
        dismiss.setOnClickListener(this);

        triggerRingtone(getApplicationContext());
        triggerVibrate(getApplicationContext());
    }


    //SUMMARY
    //receives intent from AlarmReceiver
    //as intent is packaged within PendingIntent this is the way to receive Extras
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }


    //SUMMARY
    //create and play a vibration effect that will last 4 seconds
    private void triggerVibrate(Context context) {
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        VibrationEffect vibe = VibrationEffect.createOneShot(4000, VibrationEffect.DEFAULT_AMPLITUDE);
        vibrator.vibrate(vibe);
    }


    //SUMMARY
    //Get, and trigger the default alarm sound: if none is set, use the notification sound instead
    private void triggerRingtone(Context context) {
        Uri alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarm == null) {
            alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        //play sound effect
        ringtone = RingtoneManager.getRingtone(context, alarm);
        ringtone.play();
    }

    //SUMMARY
    //stops any current ringtone audio / vibration effects
    private void stopAlarm(){
        ringtone.stop();
        vibrator.cancel();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.dismiss:
                //cancel the alarm when dismiss button is pressed
                stopAlarm();
                finish();
                break;
        }
    }
}
