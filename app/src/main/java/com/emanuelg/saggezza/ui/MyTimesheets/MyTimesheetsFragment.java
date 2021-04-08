package com.emanuelg.saggezza.ui.MyTimesheets;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.emanuelg.saggezza.R;
import com.emanuelg.saggezza.TimesheetApi;
import com.emanuelg.saggezza.TimesheetRecyclerAdapter;
import com.emanuelg.saggezza.ui.dialog.UpdateTimesheet;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MyTimesheetsFragment extends Fragment {

    private TimesheetRecyclerAdapter timesheetRecyclerAdapter;
    LinearProgressIndicator progressIndicator;
    TextView noTimesheetEntry;
    SwipeRefreshLayout swipeRefresh;
    //endregion

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_timesheets, container, false);

        swipeRefresh = root.findViewById(R.id.swipeLayout);
        swipeRefresh.setOnRefreshListener(() -> {
            timesheetRecyclerAdapter.notifyDataSetChanged();
            new Handler().postDelayed(() -> {
                // Stop animation (This will be after 3 seconds)
                swipeRefresh.setRefreshing(false);
            }, 3000);
        });
        noTimesheetEntry = root.findViewById(R.id.txtNoEntries);
        RecyclerView recyclerView = root.findViewById(R.id.entryRecycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        progressIndicator = root.findViewById(R.id.linearProgressIndicator_Timesheets);
        timesheetRecyclerAdapter = new TimesheetRecyclerAdapter(getContext(), TimesheetApi.getInstance().getTimesheetList());
        recyclerView.setAdapter(timesheetRecyclerAdapter);
        timesheetRecyclerAdapter.notifyDataSetChanged();
        if(timesheetRecyclerAdapter.getItemCount() == 0)
            noTimesheetEntry.setVisibility(View.VISIBLE);
        else noTimesheetEntry.setVisibility(View.GONE);
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
                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    UpdateTimesheet updateTimesheet = new UpdateTimesheet();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("Timesheet", TimesheetApi.getInstance().getTimesheetList().get(viewHolder.getAdapterPosition()));
                    bundle.putInt("index",viewHolder.getAdapterPosition());
                    updateTimesheet.setArguments(bundle);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction.add(android.R.id.content, updateTimesheet).addToBackStack(null).commit();
                    Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).hide();
                    //TODO Implement Update Timesheet on Swipe Left
                    //Toast.makeText(getActivity(), "Swipe Left - Update Timesheet", Toast.LENGTH_LONG).show();
                    timesheetRecyclerAdapter.notifyDataSetChanged();
                }
                if(direction==ItemTouchHelper.RIGHT)
                {
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Are you sure?")
                            .setMessage("Are you sure you want to delete this entry?\nThis action can not be undone!")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                TimesheetApi.getInstance().deleteItem(timesheetRecyclerAdapter.getItemAt(viewHolder.getAdapterPosition()).getId());
                                TimesheetApi.getInstance().getTimesheetList().remove(viewHolder.getAdapterPosition());
                                timesheetRecyclerAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                                timesheetRecyclerAdapter.notifyDataSetChanged();
                                Toast.makeText(getActivity(), "Swipe Right - Delete", Toast.LENGTH_LONG).show();
                                if(timesheetRecyclerAdapter.getItemCount() == 0)noTimesheetEntry.setVisibility(View.VISIBLE);
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
        timesheetRecyclerAdapter.notifyDataSetChanged();
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     */
    @Override
    public void onResume() {
        super.onResume();
        timesheetRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(timesheetRecyclerAdapter.getItemCount() == 0)
            noTimesheetEntry.setVisibility(View.VISIBLE);
        else noTimesheetEntry.setVisibility(View.GONE);
        timesheetRecyclerAdapter.notifyDataSetChanged();
        progressIndicator.setVisibility(View.GONE);
    }
}