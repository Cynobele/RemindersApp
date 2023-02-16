package uk.ac.abertay.cmp309project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import java.time.LocalTime;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class AddReminderActivity extends AppCompatActivity implements View.OnClickListener {

    private Button submit_button, picker_button;
    private EditText title_input, body_input;
    private CheckBox alarm;
    private RadioGroup frequency_group;
    private RadioButton FG_once, FG_daily;
    private SQLiteHelper sqlite;
    private AlarmHandler alarm_handler;
    private static AddReminderActivity instance;
    private LocalTime time;
    private boolean time_set;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);
        instance = this;
        alarm_handler = alarm_handler.getInstance(getApplicationContext());
        sqlite = sqlite.getInstance(getApplicationContext());

        submit_button = findViewById(R.id.submit_button); //attach ui elements
        picker_button = findViewById(R.id.picker_button);
        title_input = findViewById(R.id.title_input);
        body_input = findViewById(R.id.body_input);
        alarm = findViewById(R.id.alarm_checkbox);
        frequency_group = findViewById(R.id.frequency_group);
        FG_once = findViewById(R.id.FG_once);
        FG_daily = findViewById(R.id.FG_daily);

        picker_button.setOnClickListener(this); //assign onclick listener to buttons
        submit_button.setOnClickListener(this);
    }


    //SUMMARY
    //opens the time picker dialog fragment
    public void showTimePicker(View v){
        DialogFragment picker_fragment = new TimePickerFragment();
        picker_fragment.show(getSupportFragmentManager(), "add");
    }


    //SUMMARY
    //adds a back (or Up) button to the action bar, allowing a user to return to MainActivity
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.activity_add_menu, menu);
        return true;
    }


    //SUMMARY
    //sets the time value to the time selected in the TimePickerDialog
    public void setTime(LocalTime selected){
        this.time = selected;
        time_set = true;
    }


    //SUMMARY
    //handles button clicks within this activity
    //picker_button - displays the time picker dialog fragment
    //submit_button - checks all required input fields are populated then transforms inputs
    //                into Reminder object and sends to SQLite helper to save
    @Override
    public void onClick(View view) {
        switch (view.getId()){


            case R.id.picker_button:
                showTimePicker(picker_button);
                break;


            case R.id.submit_button:
                boolean ready = false; //save method will not trigger until this is true
                                        // true when TITLE, BODYTEXT & TIME are set
                Reminder reminder = new Reminder();


                if(!title_input.getText().toString().isEmpty() && !body_input.getText().toString().isEmpty()){
                    reminder.setTitle(title_input.getText().toString()); //get title and body text values
                    reminder.setBodyText(body_input.getText().toString());

                    if(time_set){
                        reminder.setTime(time.toString());
                        ready = true; //allow the app to progress as both texts & time are set

                    }else{picker_button.setError("You must select a time for the reminder!"); }

                    //get the frequency selected by the user
                    int checked = frequency_group.getCheckedRadioButtonId();
                    switch(checked){
                        case R.id.FG_once:
                            reminder.setFrequency(0); // 0 = once , default checked button
                            break;
                        case R.id.FG_daily:
                            reminder.setFrequency(1); // 1 = daily
                            break;
                    }

                    if(alarm.isChecked()){ //if the box is checked, the user wants an alarm
                        reminder.setType(true);
                    } else {
                        reminder.setType(false);
                        //set frequency to Once since there should only be 1 notification
                        reminder.setFrequency(0);
                    }
                }
                else{
                    //Applies an error on the input field that is empty
                    if(title_input.getText().toString().isEmpty()){
                        title_input.setError("Reminder title cannot be empty!");
                    }
                    if(body_input.getText().toString().isEmpty()){
                        body_input.setError("Reminder note cannot be empty!");
                    }
                }

                if(ready){//export to db
                    title_input.setText(""); //empty text fields for reuse
                    body_input.setText("");


                    //use background thread to add new Reminder to database
                    new Thread(new Runnable() {
                        private Reminder thread_reminder;
                        private SQLiteHelper thread_sqlite;

                        //get a Handler object and give it a reference to the UI thread's MessageQueue
                        Handler handler = new Handler(Looper.getMainLooper());

                        //receive reminder from UI thread
                        public Runnable pass(Reminder thread_reminder, SQLiteHelper thread_sqlite){
                            this.thread_reminder = thread_reminder;
                            this.thread_sqlite = thread_sqlite;
                            return this;
                        }

                        @Override
                        public void run() {
                            thread_sqlite.saveReminder(thread_reminder); //save reminder
                            //load the last reminder entered into the database
                            //this MUST BE DONE, or the ID will be 0 and the alarm request code will be incorrect!
                            thread_reminder = thread_sqlite.loadLastReminder();

                            handler.post(new Runnable() {
                                @Override
                                public void run() { // this will run on the UI thread
                                    //cancels old alarm then registers a new alarm with updated values
                                    alarm_handler.registerIntent(thread_reminder, getBaseContext());
                                }
                            });
                        }
                    }.pass(reminder, sqlite)).start();
                }
                break;
        }
    }
}
