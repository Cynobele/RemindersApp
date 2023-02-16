package uk.ac.abertay.cmp309project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.time.LocalTime;

public class EditReminderActivity extends AppCompatActivity implements OnClickListener{

    private static EditReminderActivity instance;
    private SQLiteHelper sqlite;
    private Button submit, picker;
    private RadioGroup type_group, frequency_group;
    private RadioButton TG_alarm, TG_notification, FG_once, FG_daily;
    private EditText title_input, text_input;
    private LocalTime time = null;
    private boolean time_set = false;
    private AlarmHandler alarm_handler;
    private Reminder edit_reminder; //fill this reminder object with the values
                                                    // passed via intent


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_reminder);
        sqlite = sqlite.getInstance(getApplicationContext());
        alarm_handler = alarm_handler.getInstance(getApplicationContext());
        instance = this;
        Intent intent = getIntent();

        edit_reminder = new Reminder();
        edit_reminder.setId(intent.getIntExtra("id", 0));
        edit_reminder.setTitle(intent.getStringExtra("title"));
        edit_reminder.setBodyText(intent.getStringExtra("text"));
        edit_reminder.setType(intent.getBooleanExtra("type", false));
        edit_reminder.setFrequency(intent.getIntExtra("frequency", 0));
        edit_reminder.setTime(intent.getStringExtra("time"));

        submit = findViewById(R.id.edit_submit_button);
        picker = findViewById(R.id.picker_button);
        type_group = findViewById(R.id.type_group);
        TG_alarm = findViewById(R.id.TG_alarm);
        TG_notification = findViewById(R.id.TG_notification);
        frequency_group = findViewById(R.id.frequency_group);
        FG_daily = findViewById(R.id.FG_daily);
        FG_once = findViewById(R.id.FG_once);
        title_input = findViewById(R.id.edit_title_input);
        text_input = findViewById(R.id.edit_body_input);

        submit.setOnClickListener(this); //assign listeners to buttons
        picker.setOnClickListener(this);
    }


    //SUMMARY
    //adds a back (or Up) button to the action bar, allowing a user to return to the list view
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.activity_edit_menu, menu);
        return true;
    }


    //SUMMARY
    //opens the time picker dialog fragment
    public void showTimePicker(View v){
        DialogFragment picker_fragment = new TimePickerFragment();
        picker_fragment.show(getSupportFragmentManager(), "edit");
    }


    //SUMMARY
    //sets the time value to the time selected in the TimePickerDialog
    public void setTime(LocalTime selected){
        this.time = selected;
        time_set = true;
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.edit_submit_button:

                //get non-empty title / text inputs, empty ones should NOT be edited
                if(!title_input.getText().toString().isEmpty()){
                    edit_reminder.setTitle(title_input.getText().toString());
                    title_input.setText("");
                }
                if(!text_input.getText().toString().isEmpty()){
                    edit_reminder.setBodyText(text_input.getText().toString());
                    text_input.setText("");
                }
                //use a flag to check if a user has selected a new time
                if(time_set){
                    edit_reminder.setTime(time.toString());
                }

                int TG_checked = type_group.getCheckedRadioButtonId();
                switch (TG_checked){
                    case R.id.TG_alarm:
                        edit_reminder.setType(true);
                        break;
                    case R.id.TG_notification:
                        edit_reminder.setType(false); //no alarm, just a notification
                        break;
                }

                int FG_checked = frequency_group.getCheckedRadioButtonId();
                switch(FG_checked){
                    case R.id.FG_once:
                        edit_reminder.setFrequency(0);
                        break;
                    case R.id.FG_daily:
                        edit_reminder.setFrequency(1);
                        break;
                }

                //use a background thread to update the database
                new Thread(new Runnable() {
                    private Reminder thread_reminder;
                    private SQLiteHelper thread_sqlite;

                    //get a Handler object and give it a reference to the UI thread's MessageQueue
                    Handler handler = new Handler(Looper.getMainLooper());

                    //receive reminder & sqlite reference from UI thread
                    public Runnable pass(Reminder thread_reminder, SQLiteHelper thread_sqlite){
                        this.thread_reminder = thread_reminder;
                        this.thread_sqlite = thread_sqlite;
                        return this;
                    }

                    @Override
                    public void run() {
                        //update the record in db
                        thread_sqlite.editReminder(thread_reminder);

                        handler.post(new Runnable() {
                            @Override
                            public void run() { // this will run on the UI thread

                                //register new pending intent using updated values
                                alarm_handler.registerIntent(thread_reminder, getBaseContext());
                            }
                        });
                    }

                }.pass(edit_reminder, sqlite)).start();
                break;

            case R.id.picker_button:
                //open the time picker dialog
                showTimePicker(picker);
                break;
        }

    }


    //SUMMARY
    //gets reference to this instance, allowing SQLite to access public methods
    public static EditReminderActivity getInstance(){
        return instance;
    }


    //SUMMARY
    //takes a String value and displays it via Toast message
    //allows external classes to show toast messages
    public void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

}
