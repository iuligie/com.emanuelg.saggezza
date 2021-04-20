package com.emanuelg.saggezza.ui.dashboard;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DashboardFragment extends Fragment {
    private final List<Employee> employeeList =new ArrayList<>();
    private RecyclerView recyclerView;
    private LeaderboardRecyclerAdapter leaderboardRecyclerAdapter;
    private LinearProgressIndicator progressIndicator;
    private ProgressBar progressBar_Avatar;
    private ProgressBar progressBar_Leaderboard;
    private TextView txtEmployeeName;
    private TextView txtLevel;
    private ImageView imgRank;
    private List<Uri> uriAchievementsList = new ArrayList<>();
    private final List<ImageView> imgAchievementsList = new ArrayList<>();
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        progressIndicator =root.findViewById(R.id.linearProgressIndicator);
        progressBar_Avatar = root.findViewById(R.id.progressBar_Avatar);

        progressBar_Leaderboard = root.findViewById(R.id.progressBar_Leaderboard);
        recyclerView = root.findViewById(R.id.leaderboard);

        txtEmployeeName = root.findViewById(R.id.txtEmployeeName);
        txtLevel= root.findViewById(R.id.txtLevel);
        imgRank = root.findViewById(R.id.imgRank);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        progressBar_Avatar.setVisibility(View.VISIBLE);
        progressIndicator.setVisibility(View.VISIBLE);
        progressBar_Leaderboard.setVisibility(View.VISIBLE);

        Pair<String, Integer> rankAndAvatar = getRankAndAvatar(Employee.getInstance().getScore());
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
        initializeData();

        ImageView imgAchievement1 = view.findViewById(R.id.imgAchiement1);
        ImageView imgAchievement2 = view.findViewById(R.id.imgAchiement2);
        ImageView imgAchievement3 = view.findViewById(R.id.imgAchiement3);
        ImageView imgAchievement4 = view.findViewById(R.id.imgAchiement4);
        ImageView imgAchievement5 = view.findViewById(R.id.imgAchiement5);

        int achievementsTotal = Employee.getInstance().getAchievementsTotal();

        //region imgAchievements
        if(achievementsTotal == 2)
        {
            imgAchievementsList.add(imgAchievement1);
            imgAchievementsList.add(imgAchievement2);
            imgAchievement3.setVisibility(View.GONE);
            imgAchievement4.setVisibility(View.GONE);
            imgAchievement5.setVisibility(View.GONE);

        }else if(achievementsTotal==3)
        {
            imgAchievementsList.add(imgAchievement1);
            imgAchievementsList.add(imgAchievement2);
            imgAchievementsList.add(imgAchievement3);
            imgAchievement4.setVisibility(View.GONE);
            imgAchievement5.setVisibility(View.GONE);
        }else if(achievementsTotal == 4)
        {
            imgAchievementsList.add(imgAchievement1);
            imgAchievementsList.add(imgAchievement2);
            imgAchievementsList.add(imgAchievement3);
            imgAchievementsList.add(imgAchievement4);
            imgAchievement5.setVisibility(View.GONE);
        }else if(achievementsTotal == 5){
            imgAchievementsList.add(imgAchievement1);
            imgAchievementsList.add(imgAchievement2);
            imgAchievementsList.add(imgAchievement3);
            imgAchievementsList.add(imgAchievement4);
            imgAchievementsList.add(imgAchievement5);
        }
        else{
            imgAchievementsList.add(imgAchievement1);
            imgAchievement2.setVisibility(View.GONE);
            imgAchievement3.setVisibility(View.GONE);
            imgAchievement4.setVisibility(View.GONE);
            imgAchievement5.setVisibility(View.GONE);
        }
        attachAchievements(imgAchievementsList);
        //endregion


    }

    @Override
    public void onStart() {
        super.onStart();
        progressIndicator.setVisibility(View.VISIBLE);
        progressBar_Leaderboard.setVisibility(View.VISIBLE);
        leaderboardRecyclerAdapter.notifyDataSetChanged();
        new Handler().postDelayed(() -> {
            progressIndicator.setVisibility(View.GONE);
            progressBar_Avatar.setVisibility(View.GONE);
            progressBar_Leaderboard.setVisibility(View.GONE);
        }, 1000);
    }

    private void attachAchievements(List<ImageView> imgs) {

       // List<Uri> res= uriAchievementsList//loadAchievementFromDB(imgs.size()+1);
        if(uriAchievementsList.size() == 0)
        loadAchievementFromDB(imgs.size()+1);
        Collections.reverse(imgs);
        for (int i=0;i<imgs.size();i++) {
            Picasso.get()
                    .load(uriAchievementsList.get(i).toString())
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

    private void initializeData() {
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
                        initializeAdapter();
                    }
                });

        initializeAdapter();

    }

    private void initializeAdapter() {
        leaderboardRecyclerAdapter = new LeaderboardRecyclerAdapter(getContext(), employeeList);
        recyclerView.setAdapter(leaderboardRecyclerAdapter);
        leaderboardRecyclerAdapter.notifyDataSetChanged();
    }

    /*public List<Uri> loadAchievementFromDB(int total) {
        List<Uri> result = new ArrayList<>();
        if(TimesheetApi.getInstance().loadAchievementFromDB(total).size() != 0)
        {
            result = TimesheetApi.getInstance().loadAchievementFromDB(total);
        }

        else if (result.size() == 0) {
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
    }*/
    public Pair<String, Integer> getRankAndAvatar(int score)
    {
        Pair<String, Integer> result;

        if(score >= 100 && score <200)
        {
            result = new Pair<>("Level 2 - Visionary ", R.drawable.rank2);
        }else if(score >= 200&& score <300)
        {
            result = new Pair<>("Level 3 - Aspiring Explorer ", R.drawable.rank3);
        }
        else if(score >= 300&& score <400)
        {
            result = new Pair<>("Level 4 - Established Explorer ", R.drawable.rank4);
        }
        else if(score >= 500)
        {
            result = new Pair<>("Guru", R.drawable.rank5);
        }else {
            result = new Pair<>("Level 1 - Engager ", R.drawable.rank1);
        }


        return result;
    }

    public void loadAchievementFromDB(int total) {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        //StorageReference storageRef = storage.getReference();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://emanuel-dissertation.appspot.com");
        for (int i = 1; i <= total; i++)
            {
                String strImg = "achievement" + i + ".png";
                com.google.android.gms.tasks.Task<Uri> task = storageRef.child(strImg).getDownloadUrl();
                while (!task.isComplete()) {
                    progressIndicator.setVisibility(View.VISIBLE);
                    progressBar_Leaderboard.setVisibility(View.VISIBLE);
                }
                if (task.isSuccessful()) {
                    uriAchievementsList.add(task.getResult());
                    progressIndicator.setVisibility(View.GONE);
                    progressBar_Leaderboard.setVisibility(View.GONE);
                }
            }


        if (uriAchievementsList.size() == 0) {
            uriAchievementsList.add(Uri.parse("gs://emanuel-dissertation.appspot.com/achievement3.png"));
            //throw new AssertionError("Unable to load achievements");
        }

       // return uriAchievementsList;
    }

}