package uk.ac.abertay.cmp309project;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class ReminderAdapter extends ArrayAdapter<Reminder> implements View.OnClickListener {

    private AlertDialog.Builder builder;

    //SUMMARY
    //reminders - list containing Reminder objects retrieved from sqlite
    //public constructor for creating a scrollable ListView from a List<Reminder>
    public ReminderAdapter(Context context, List<Reminder> reminders) {
        super(context, 0, reminders);
    }



    //SUMMARY
    //position - the current item in the List<Reminder>
    //convert - a reusable layout from a previous row, if there isnt one, create one
    //parent - the ListView
    //this method will display the Reminder objects in the list view using row_layout template
    @Override
    public View getView(int position, View convert, ViewGroup parent){

        Reminder reminder = getItem(position); //get the object at this pos

        if(convert == null){ //if there is not an existing view to reuse, create one
            convert = LayoutInflater.from(getContext()).inflate(R.layout.row_layout, parent, false);
        }

        builder = new AlertDialog.Builder(getContext());

        TextView title_view = convert.findViewById(R.id.title_view); //attach TextViews
        TextView body_view = convert.findViewById(R.id.bodytext_view);
        TextView time_view = convert.findViewById(R.id.time_view);
        TextView alarm_view = convert.findViewById(R.id.alarm_view);
        TextView frequency_view = convert.findViewById(R.id.frequency_view);

        title_view.setText(reminder.getTitle()); //get values to display from Reminder object
        body_view.setText(reminder.getBodyText());
        time_view.setText(reminder.getTime());
        if(reminder.isAlarm()){
            alarm_view.setText("Alarm at : ");
        }else{
            alarm_view.setText("Notification at : ");
        }
        if(reminder.getFrequency() == 0){
            frequency_view.setText(" | Only once");
        } else {
            frequency_view.setText(" | Repeating daily");
        }

        //assign click listeners to the buttons on this ListItem
        ImageButton delete = convert.findViewById(R.id.delete);
        ImageButton edit = convert.findViewById(R.id.edit);
        delete.setOnClickListener(this);
        edit.setOnClickListener(this);

        delete.setTag(reminder); //save this object as Tag value, lets us access the id for delete/edit
        edit.setTag(reminder);
        return convert;
    }

    //SUMMARY
    //each of the items in the ListView has a Delete and an Edit button.
    //the reminder obj that was used to create the list items is used as a tag value, so that the ID can be accessed for SQLite queries
    //Delete opens a dialog box when clicked, to confirm if the user wants to delete the reminder
    @Override
    public void onClick(View view) {

        final SQLiteHelper sqlite = SQLiteHelper.getInstance(getContext());
        final Reminder reminder = (Reminder)view.getTag();
        final int id = reminder.getId();

        switch (view.getId()){

            case R.id.delete:

                //confirm the user wants to delete using an AlertDialog
                builder.setMessage("Are you sure you want to delete this reminder?")
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                sqlite.deleteReminder(id); //delete the reminder
                                MainActivity.getInstance().showToast("\tDeleted!\nRefresh to see changes");
                                AlarmHandler.getInstance(getContext()).cancelAlarmIntent(getContext(),
                                        reminder.getId());
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.cancel(); //close the dialog box
                            }
                        });

                AlertDialog alert = builder.create(); //create the pop-up to display the confirmation
                alert.setTitle("Delete?");
                alert.show();
                break;

            case R.id.edit: //edit the reminder

                //bundle the old values from the reminder that is being edited
                //old reminder value is sent to the database for fields that should not be updated
                Intent intent = new Intent(getContext(), EditReminderActivity.class);
                intent.putExtra("id", reminder.getId());
                intent.putExtra("title", reminder.getTitle());
                intent.putExtra("text", reminder.getBodyText());
                intent.putExtra("type", reminder.isAlarm());
                intent.putExtra("frequency", reminder.getFrequency());
                intent.putExtra("time", reminder.getTime());
                getContext().startActivity(intent);

                break;
        }
    }
}
