package com.emanuelg.saggezza;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.emanuelg.saggezza.model.Employee;

import java.util.List;

public class LeaderboardRecyclerAdapter extends RecyclerView.Adapter<LeaderboardRecyclerAdapter.ViewHolder> {

    private Context context;
    private final List<Employee> employeeList;

    public LeaderboardRecyclerAdapter(Context context, List<Employee> employeeList) {
        this.context = context;
        this.employeeList = employeeList;
    }

    @NonNull
    @Override
    public LeaderboardRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view, context);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull LeaderboardRecyclerAdapter.ViewHolder holder, int position) {

        Employee item = employeeList.get(position);
        if(item.getEmail().equals(Employee.getInstance().getEmail()))
        {
            holder.txtIndex.setTextColor(ContextCompat.getColor(context, R.color.SaggezzaGreen));
            holder.txtName.setTextColor(ContextCompat.getColor(context, R.color.SaggezzaGreen));
            holder.txtScore.setTextColor(ContextCompat.getColor(context, R.color.SaggezzaGreen));
        }
        holder.txtIndex.setText((position + 1) + ".");
        holder.txtName.setText(item.getName());
        holder.txtScore.setText(Integer.toString(item.getScore()));

    }

    @Override
    public int getItemCount() {
        return employeeList.size();
    }

    public Employee getItemAt(int position){
        return employeeList.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView txtIndex,txtName,txtScore;
        public ImageView imgEmployee;

        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            context = ctx;
            imgEmployee = itemView.findViewById(R.id.imgRank);
            txtIndex=itemView.findViewById(R.id.txtIndex);
            txtName=itemView.findViewById(R.id.txtName);
            txtScore=itemView.findViewById(R.id.txtScore);


        }
    }
}