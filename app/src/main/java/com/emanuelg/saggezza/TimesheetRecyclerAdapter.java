package com.emanuelg.saggezza;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emanuelg.saggezza.model.Timesheet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimesheetRecyclerAdapter extends RecyclerView.Adapter<TimesheetRecyclerAdapter.ViewHolder> {

    private Context context;
    private final List<Timesheet> timesheetList;
    private final TimesheetApi api = TimesheetApi.getInstance();

    public TimesheetRecyclerAdapter(Context context, List<Timesheet> timesheetList) {
        this.context = context;
        this.timesheetList = timesheetList;
    }

    @NonNull
    @Override
    public TimesheetRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.timesheet_card, parent, false);
        //api.setTimesheetList(timesheetList);
        //view.findViewById(R.id.img_progressBar).setVisibility(View.INVISIBLE);
        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull TimesheetRecyclerAdapter.ViewHolder holder, int position) {

        Timesheet item = timesheetList.get(position);
        String imageUrl;
       // item.setProject(TimesheetApi.getInstance().getProjectByRef(item.getProjectRef()));
        holder.txtTaskName.setText(item.getTask().getName());
        holder.txtProjectName.setText(item.getProject().getName());
        //holder.txtDate.setText(MessageFormat.format("{0} - {1}",item.getBeginDate().substring(0,5), item.getEndDate().substring(0,5)));
        holder.txtDate.setText(item.getTxtDateRange());
        holder.txtHours.setText(item.getHours());

        //imageUrl = item.getImageUrl();
        long miliseconds = item.getSubmittedOn().getSeconds();
        Date date = new Date(Long.parseLong(Long.toString(miliseconds)) *  1000L);
        String strDate = new SimpleDateFormat("dd/MM/yyyy", Locale.UK).format(date);
        holder.txtEntryDate.setText(strDate);
        if(item.isOnTime())
        {
            holder.imageView.setImageResource(R.drawable.badge);
        }else holder.imageView.setImageResource(R.drawable.ic_error_24);
//        holder.img_progressBar.setVisibility(View.VISIBLE);

    }

    @Override
    public int getItemCount() {
        return timesheetList.size();
    }

    public Timesheet getItemAt(int position){
        return timesheetList.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView txtTaskName, txtProjectName,txtDate,txtHours,txtEntryDate;
        public ImageView imageView;
        //public CheckBox owned;
        public ProgressBar img_progressBar;
        // String userId;
        //String username;

        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            context = ctx;
            imageView = itemView.findViewById(R.id.imgBadge);
            txtTaskName = itemView.findViewById(R.id.txtTaskName);
            txtProjectName=itemView.findViewById(R.id.txtProject);
            txtDate=itemView.findViewById(R.id.txtDate);
            txtHours=itemView.findViewById(R.id.txtHours);
            txtEntryDate=itemView.findViewById(R.id.txtEntryDate);

            itemView.setOnLongClickListener(v -> {
                Toast.makeText(v.getContext(), "Long Press Detected - Edit Item", Toast.LENGTH_LONG).show();
                TimesheetApi api=TimesheetApi.getInstance();
                api.setCurrentTimesheetPos(Integer.toString(getAdapterPosition()));
                //ctx.startActivity(new Intent(v.getContext(), PostProductActivity.class));
                //TO DO implement edit function for timesheets
                return false;
            });


        }
    }
}