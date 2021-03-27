package com.emanuelg.saggezza.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emanuelg.saggezza.LeaderboardRecyclerAdapter;
import com.emanuelg.saggezza.R;
import com.emanuelg.saggezza.TimesheetApi;
import com.emanuelg.saggezza.model.Employee;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.OrderBy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DashboardFragment extends Fragment {
    private List<Employee> employeeList =new ArrayList<>();
    private RecyclerView recyclerView;
    private LeaderboardRecyclerAdapter leaderboardRecyclerAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        final TextView textView = root.findViewById(R.id.txtAchievements);
//        noTimesheetEntry = root.findViewById(R.id.txtNoEntries);


        //progressBar.setVisibility(View.VISIBLE);
//        TimesheetApi.getInstance().MyTimesheetListener();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.leaderboard);
        TextView txtEmployeeName= view.findViewById(R.id.txtEmployeeName);
        ImageView imgRank = view.findViewById(R.id.imgRank);
        Picasso.get()
                .load(R.drawable.rank1)
                .placeholder(R.drawable.rank1)
                .resize(300, 300)
                .into(imgRank);
        txtEmployeeName.setText(Employee.getInstance().getAccount().getDisplayName());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        InitializeData();

    }

    private void InitializeData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map <String,Integer> itemLeaderboard = new HashMap<String,Integer>();
        db.collection("Employees")
                .orderBy("score", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot items : Objects.requireNonNull(task.getResult())) {
                        Employee temp = items.toObject(Employee.class);
                        itemLeaderboard.put(temp.getEmail(),temp.getScore());
                        employeeList.add(temp);
                    }
                    TimesheetApi.getInstance().setEmployeeList(employeeList);
                    InitializeAdapter();
                }
            }});
        //for (int i=0;i<=5;i++)
        //employeeList.add(Employee.getInstance());
        //if (employeeList.size()!=0)
        InitializeAdapter();
        /*else{
            employeeList.add(Employee.getInstance());
            InitializeAdapter();
            //throw new RuntimeException("Something went wrong!");
        }*/
    }

    private void InitializeAdapter() {
        leaderboardRecyclerAdapter = new LeaderboardRecyclerAdapter(getContext(), employeeList);
        recyclerView.setAdapter(leaderboardRecyclerAdapter);
        leaderboardRecyclerAdapter.notifyDataSetChanged();
    }
}