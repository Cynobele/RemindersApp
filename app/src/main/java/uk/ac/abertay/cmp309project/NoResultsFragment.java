package uk.ac.abertay.cmp309project;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


//SUMMARY
//This fragment is only shown if the database has no rows.
//Uses the fragment_no_results.xml layout file, only has a TextView + button to add a reminder
public class NoResultsFragment extends Fragment implements View.OnClickListener {


    private ImageButton add_button;

    public NoResultsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root_view = inflater.inflate(R.layout.fragment_no_results, container, false);
        add_button = root_view.findViewById(R.id.add_reminder_button);
        add_button.setOnClickListener(this);

        return root_view;
    }

    //SUMMARY
    //creates an intent and starts the 'AddReminderActivity'
    public void goToActivity(View view){
        Intent intent = new Intent(getActivity(), AddReminderActivity.class);
        startActivity(intent);
    }

    //SUMMARY
    //handles button clicks within this fragment
    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.add_reminder_button:
                goToActivity(view); //redirect to the AddReminderActivity
                break;
        }
    }
}
