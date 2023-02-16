package uk.ac.abertay.cmp309project;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.fragment.app.ListFragment;
import java.util.List;


public class ResultListFragment extends ListFragment{

    private SQLiteHelper sqlite;
    private List<Reminder> reminders;

    public ResultListFragment() {
        // Required empty constructor
        setRetainInstance(true);
    }

    //SUMMARY
    //Inflates the layout for this fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflated_view = inflater.inflate(R.layout.fragment_result_list, container,false);
        return inflated_view;
    }

    //SUMMARY
    //loads the data from the database into the custom ArrayAdapter class 'ReminderAdapter'
    public void onViewCreated(View view, Bundle savedInstanceState){

        sqlite = SQLiteHelper.getInstance(getContext());

        if(sqlite.loadReminders() == null){
            // DO NOT attempt to load the list as this will cause NullPointerException
        }else {
            reminders = sqlite.loadReminders(); //load reminders
        }

        if(reminders.size()>0){ //if there is data to display, display it
            //custom adapter to display Reminder objects
            ReminderAdapter adapter = new ReminderAdapter(getContext(), reminders);
            ListView list_view = (ListView) view.findViewById(R.id.reminder_list);
            list_view.setAdapter(adapter);
        }
    }
}
