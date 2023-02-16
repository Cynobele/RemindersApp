package uk.ac.abertay.cmp309project;


import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.widget.TimePicker;
import java.time.LocalTime;


public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {


    public TimePickerFragment() {
        // Required empty public constructor
    }

    //SUMMARY
    //creates a time picker dialog box, allowing the user to select a time using
    //an analogue or digital clock face input
    public Dialog onCreateDialog(Bundle savedInstanceState){

        //get the current time as the default for the picker
        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();

        return new TimePickerDialog(getActivity(), this, hour, minute,true);
    }

    //SUMMARY
    //takes the values selected selected within TimePicker
    //passes the selected time to the parent activity
    @Override
    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

        String picker_type = this.getTag(); //either 'add' or 'edit'
        LocalTime time = LocalTime.of(selectedHour, selectedMinute); //get selected values

        if(picker_type.contains("add")){
            AddReminderActivity parent = (AddReminderActivity)getActivity();
            parent.setTime(time);
        }else{
           EditReminderActivity parent =(EditReminderActivity)getActivity();
           parent.setTime(time);
        }
    }
}
