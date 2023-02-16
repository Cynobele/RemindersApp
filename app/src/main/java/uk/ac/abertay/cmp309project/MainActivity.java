package uk.ac.abertay.cmp309project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SQLiteHelper sqlite;
    private FragmentManager fm = getSupportFragmentManager();
    private Fragment nr_fragment = new NoResultsFragment();
    private Fragment rl_fragment = new ResultListFragment();
    private static String nr_tag = "no_results"; //tags for fragment manager
    private static String rl_tag = "results_list";
    private static MainActivity instance;
    private List<Reminder> reminders;

    //SUMMARY
    //runs before onCreate when the activity comes into focus
    @Override
    public void onStart(){
        super.onStart();
        instance = this; //create reference for this activity
    }


    //SUMMARY
    // assign activity_main.xml to view
    // checks that required permissions are granted
    // requests permissions if they are not
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sqlite = SQLiteHelper.getInstance(getApplicationContext()); //get sqlite class reference

        String[] permissions = {Manifest.permission.VIBRATE, Manifest.permission.USE_FULL_SCREEN_INTENT,
            Manifest.permission.SCHEDULE_EXACT_ALARM, Manifest.permission.DISABLE_KEYGUARD,
                Manifest.permission.SYSTEM_ALERT_WINDOW};
        int vibrate_permission = checkSelfPermission(Manifest.permission.VIBRATE); //check permissions
        int fullscreen_permission = checkSelfPermission(Manifest.permission.USE_FULL_SCREEN_INTENT);
        int exact_permission = checkSelfPermission(Manifest.permission.SCHEDULE_EXACT_ALARM);
        int keyguard_permission = checkSelfPermission(Manifest.permission.DISABLE_KEYGUARD);
        int alert_permission = checkSelfPermission(Manifest.permission.SYSTEM_ALERT_WINDOW);

        //request permissions if any are missing
        if(vibrate_permission != PackageManager.PERMISSION_GRANTED || fullscreen_permission  != PackageManager.PERMISSION_GRANTED
         || exact_permission  != PackageManager.PERMISSION_GRANTED || keyguard_permission  != PackageManager.PERMISSION_GRANTED
         || alert_permission  != PackageManager.PERMISSION_GRANTED){
            requestPermissions(permissions, 1);
        }


        reminders = sqlite.loadReminders();
        if(savedInstanceState == null){//if the state is not being restored, use .add

            if(reminders.size() > 0){ //display ResultListFragment if reminders contains items
                fm.beginTransaction()
                        .add(R.id.fragment, rl_fragment, rl_tag)
                        .setReorderingAllowed(true)
                        .addToBackStack("results")
                        .commit();
            }else{                  //display NoResultsFragment if reminders is empty
                fm.beginTransaction()
                        .add(R.id.fragment, nr_fragment, nr_tag)
                        .setReorderingAllowed(true)
                        .addToBackStack("no_results")
                        .commit();
            }

        }else { //on orientation change, use .replace instead as FragmentManager has an item already
            if (reminders.size() > 0) {
                fm.beginTransaction()
                        .replace(R.id.fragment, rl_fragment, rl_tag)
                        .setReorderingAllowed(true)
                        .addToBackStack("results")
                        .commit();
            } else {
                fm.beginTransaction()
                        .replace(R.id.fragment, nr_fragment, nr_tag)
                        .setReorderingAllowed(true)
                        .addToBackStack("no_results")
                        .commit();
            }
        }
    }


    //SUMMARY
    //adds a value to a bundle that is created before the view is destroyed
    //this will flag the onCreate method to use .replace instead of .add for the fragment transaction
    @Override
    protected void onSaveInstanceState(Bundle bundle){

        Fragment frag = this.getSupportFragmentManager().findFragmentById(R.id.fragment);
        if(frag instanceof ResultListFragment){
            bundle.putString(rl_tag, rl_tag);
        }
        super.onSaveInstanceState(bundle);
    }


    //SUMMARY
    //inflates the main_menu.xml layout onto the action bar, adding the Add & Refresh buttons
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    //SUMMARY
    //triggers when the Add or Refresh buttons on the action bar are clicked
    //Add : creates an intent and then starts 'AddReminderActivity'
    //Refresh : removes the fragment from view, then recreate it.
    //          this causes it to update as onCreate() runs and retrieves the updated database
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.add:
                //move to the 'add' activity, allows user to create a new reminder
                Intent intent = new Intent(this, AddReminderActivity.class);
                startActivity(intent);
                return true;

            case R.id.refresh:
                reminders = sqlite.loadReminders();
                if(reminders.size() > 0) {  //refresh the ResultsListFragment
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.remove(fm.findFragmentByTag(rl_tag))
                            .add(rl_fragment, rl_tag)
                            .commit();
                }else{ //if the last reminder was deleted, replace ResultsListFragment with NoResultsFragment
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.fragment, nr_fragment, nr_tag)
                            .setReorderingAllowed(true)
                            .addToBackStack("no_results")
                            .commit();
                }
                return true;

                default: //should be unreachable, but required for error handling
                    return super.onOptionsItemSelected(item);
        }
    }


    //SUMMARY
    //gets reference to this instance, allowing other classes to access MainActivity
    public static MainActivity getInstance(){
        return instance;
    }


    //SUMMARY
    //takes a String value and displays it via Toast message
    //allows external classes to show toast messages
    public void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}
