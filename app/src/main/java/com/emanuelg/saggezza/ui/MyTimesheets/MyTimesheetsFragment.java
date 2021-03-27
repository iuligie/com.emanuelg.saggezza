package com.emanuelg.saggezza.ui.MyTimesheets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emanuelg.saggezza.R;
import com.emanuelg.saggezza.TimesheetApi;
import com.emanuelg.saggezza.TimesheetRecyclerAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class MyTimesheetsFragment extends Fragment {

    private TextView noTimesheetEntry;
    //private CollectionReference collectionReference;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RecyclerView recyclerView;
    private TimesheetRecyclerAdapter timesheetRecyclerAdapter;
    private ListenerRegistration registration;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    ProgressBar progressBar;
    //endregion

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_timesheets, container, false);

        noTimesheetEntry = root.findViewById(R.id.txtNoEntries);
        recyclerView = root.findViewById(R.id.entryRecycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //progressBar.setVisibility(View.VISIBLE);
//        TimesheetApi.getInstance().MyTimesheetListener();
        timesheetRecyclerAdapter = new TimesheetRecyclerAdapter(getContext(), TimesheetApi.getInstance().getTimesheetList());
        recyclerView.setAdapter(timesheetRecyclerAdapter);
        timesheetRecyclerAdapter.notifyDataSetChanged();


        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();

    }
}