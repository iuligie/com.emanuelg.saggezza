package com.emanuelg.saggezza.ui.dashboard;

import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DashboardFragment extends Fragment {
    private List<Employee> employeeList =new ArrayList<>();
    private RecyclerView recyclerView;
    private LeaderboardRecyclerAdapter leaderboardRecyclerAdapter;
    //ProgressBar progressBar;
    LinearProgressIndicator progressIndicator;
    ProgressBar progressBar_Avatar;
    ProgressBar progressBar_Leaderboard;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        progressIndicator =root.findViewById(R.id.linearProgressIndicator);
        progressBar_Avatar = root.findViewById(R.id.progressBar_Avatar);
        progressBar_Avatar.setVisibility(View.VISIBLE);
        progressBar_Leaderboard = root.findViewById(R.id.progressBar_Leaderboard);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.leaderboard);
        TextView txtEmployeeName= view.findViewById(R.id.txtEmployeeName);
        TextView txtLevel = view.findViewById(R.id.txtLevel);
        ImageView imgRank = view.findViewById(R.id.imgRank);

        Pair<String, Integer> rankAndAvatar = TimesheetApi.getInstance().getRankAndAvatar(Employee.getInstance().getScore());
        txtLevel.setText(rankAndAvatar.first);
        Picasso.get()
                .load(rankAndAvatar.second)
                .resize(300, 300)
                .into(imgRank, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar_Avatar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });

        txtEmployeeName.setText(Employee.getInstance().getAccount().getDisplayName());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        InitializeData();
        progressBar_Leaderboard.setVisibility(View.GONE);
        progressBar_Avatar.setVisibility(View.GONE);
        ImageView imgAchievement1 = view.findViewById(R.id.imgAchiement1);
        ImageView imgAchievement2 = view.findViewById(R.id.imgAchiement2);
        ImageView imgAchievement3 = view.findViewById(R.id.imgAchiement3);
        ImageView imgAchievement4 = view.findViewById(R.id.imgAchiement4);
        ImageView imgAchievement5 = view.findViewById(R.id.imgAchiement5);

        int achievementsTotal = Employee.getInstance().getAchievementsTotal();
        List<ImageView> imgs = new ArrayList<>();
        //region Achievements
        if(achievementsTotal == 2)
        {
            imgs.add(imgAchievement1);
            imgs.add(imgAchievement2);
            imgAchievement3.setVisibility(View.GONE);
            imgAchievement4.setVisibility(View.GONE);
            imgAchievement5.setVisibility(View.GONE);

        }else if(achievementsTotal==3)
        {
            imgs.add(imgAchievement1);
            imgs.add(imgAchievement2);
            imgs.add(imgAchievement3);
            imgAchievement4.setVisibility(View.GONE);
            imgAchievement5.setVisibility(View.GONE);
        }else if(achievementsTotal == 4)
        {
            imgs.add(imgAchievement1);
            imgs.add(imgAchievement2);
            imgs.add(imgAchievement3);
            imgs.add(imgAchievement4);
            imgAchievement5.setVisibility(View.GONE);
        }else if(achievementsTotal == 5){
            imgs.add(imgAchievement1);
            imgs.add(imgAchievement2);
            imgs.add(imgAchievement3);
            imgs.add(imgAchievement4);
            imgs.add(imgAchievement5);
        }
        else{
            imgs.add(imgAchievement1);
            imgAchievement2.setVisibility(View.GONE);
            imgAchievement3.setVisibility(View.GONE);
            imgAchievement4.setVisibility(View.GONE);
            imgAchievement5.setVisibility(View.GONE);
        }
        LoadAchievements(imgs);
        //endregion
    }

    @Override
    public void onStart() {
        super.onStart();
        progressIndicator.setVisibility(View.GONE);
    }

    private void LoadAchievements(List<ImageView> imgs) {

        List<Uri> res=getAchievements(imgs.size()+1);
        for (int i=0;i<imgs.size();i++) {
            Picasso.get()
                    .load(res.get(i).toString())
                    .resize(300, 300)
                    .into(imgs.get(i), new Callback(){

                        @Override
                        public void onSuccess() {
                            progressIndicator.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
        }

    }

    private void InitializeData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Employees")
                .orderBy("score", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot items : Objects.requireNonNull(task.getResult())) {
                            Employee temp = items.toObject(Employee.class);
                            employeeList.add(temp);
                        }
                        TimesheetApi.getInstance().setEmployeeList(employeeList);
                        InitializeAdapter();
                    }
                });

        InitializeAdapter();

    }

    private void InitializeAdapter() {
        leaderboardRecyclerAdapter = new LeaderboardRecyclerAdapter(getContext(), employeeList);
        recyclerView.setAdapter(leaderboardRecyclerAdapter);
        leaderboardRecyclerAdapter.notifyDataSetChanged();
    }

    public List<Uri> getAchievements(int total) {
        List<Uri> result = new ArrayList<>();
        // Create a Cloud Storage reference from the app
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageRef = storage.getReferenceFromUrl("gs://emanuel-dissertation.appspot.com");
        for (int i = 1; i <= total; i++) {
            String strImg = "achievement" + i + ".png";
            storageRef.child("achievement1.png")
                    .getDownloadUrl()
                    .addOnSuccessListener(result::add);

        }
        if (result.size() == 0) {
            result.add(Uri.parse("https://firebasestorage.googleapis.com/v0/b/emanuel-dissertation.appspot.com/o/achievement1.png?alt=media&token=ad9b5aba-00de-4177-9bba-8c0b459b9973"));
            if (total >= 2)
                result.add(Uri.parse("https://firebasestorage.googleapis.com/v0/b/emanuel-dissertation.appspot.com/o/achievement2.png?alt=media&token=ce2a5a65-f459-4bdf-ba9e-4f36daac42d2"));
            if (total >= 3)
                result.add(Uri.parse("https://firebasestorage.googleapis.com/v0/b/emanuel-dissertation.appspot.com/o/achievement3.png?alt=media&token=6d9112a7-5e46-452b-a8ea-c223793dc057"));
            if (total >= 4)
                result.add(Uri.parse("https://firebasestorage.googleapis.com/v0/b/emanuel-dissertation.appspot.com/o/achievement4.png?alt=media&token=549d484a-5d14-4b05-8473-1420356812ca"));
            if (total >= 5)
                result.add(Uri.parse("https://firebasestorage.googleapis.com/v0/b/emanuel-dissertation.appspot.com/o/achievement5.png?alt=media&token=1f56fd4b-32cb-46a6-be4e-842614f59240"));
            //throw new AssertionError("Unable to load achievements");
        }
        return result;
    }

}