package com.emanuelg.saggezza.ui.MyTimesheets;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emanuelg.saggezza.R;
import com.emanuelg.saggezza.TimesheetApi;
import com.emanuelg.saggezza.TimesheetRecyclerAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import org.jetbrains.annotations.NotNull;

public class MyTimesheetsFragment extends Fragment {

    private TextView noTimesheetEntry;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RecyclerView recyclerView;
    private TimesheetRecyclerAdapter timesheetRecyclerAdapter;
    private ListenerRegistration registration;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    LinearProgressIndicator progressIndicator;
    //endregion

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_timesheets, container, false);

        noTimesheetEntry = root.findViewById(R.id.txtNoEntries);
        recyclerView = root.findViewById(R.id.entryRecycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        progressIndicator = root.findViewById(R.id.linearProgressIndicator_Timesheets);
        timesheetRecyclerAdapter = new TimesheetRecyclerAdapter(getContext(), TimesheetApi.getInstance().getTimesheetList());
        recyclerView.setAdapter(timesheetRecyclerAdapter);
        timesheetRecyclerAdapter.notifyDataSetChanged();

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder, @NotNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(@NotNull RecyclerView.ViewHolder viewHolder, int direction) {

                if(direction==ItemTouchHelper.LEFT)
                {
                    //TODO Implement Update Timesheet on Swipe Left
                    Toast.makeText(getActivity(), "Swipe Left - Update Timesheet", Toast.LENGTH_LONG).show();
                    timesheetRecyclerAdapter.notifyDataSetChanged();
                }
                if(direction==ItemTouchHelper.RIGHT)
                {
                    new MaterialAlertDialogBuilder(getContext())
                            .setTitle("Are you sure?")
                            .setMessage("Are you sure you want to delete this entry?\nThis action can not be undone!")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                TimesheetApi.getInstance().deleteItem(timesheetRecyclerAdapter.getItemAt(viewHolder.getAdapterPosition()).getId());
                                TimesheetApi.getInstance().getTimesheetList().remove(viewHolder.getAdapterPosition());
                                timesheetRecyclerAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                                timesheetRecyclerAdapter.notifyDataSetChanged();
                                Toast.makeText(getActivity(), "Swipe Right - Delete", Toast.LENGTH_LONG).show();
                            }).setNegativeButton("Cancel", (dialog, which) -> timesheetRecyclerAdapter.notifyDataSetChanged())
                            .show();
                }
            }
        }).attachToRecyclerView(recyclerView);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        progressIndicator.setVisibility(View.GONE);
    }
}