package com.emanuelg.saggezza.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.emanuelg.saggezza.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class AddTimesheet extends DialogFragment {

    private static final String TAG = "AddTimesheet";
    private Toolbar toolbar;
    LinearProgressIndicator progressIndicator;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL,R.style.Theme_MaterialComponents_DayNight_Dialog);

    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setWindowAnimations(R.style.SaggezzaTimesheets_Slide);
        }
        progressIndicator.setVisibility(View.GONE);
    }

    @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_add_timesheet, container, false);

            toolbar = view.findViewById(R.id.toolbar);

            progressIndicator = view.findViewById(R.id.linearProgressIndicator_dialog);

            progressIndicator.setVisibility(View.VISIBLE);

            return view;
        }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar.setNavigationOnClickListener(v -> dismiss());

        toolbar.setTitle("Submit your timesheet");

        toolbar.inflateMenu(R.menu.dialog_menu);

        toolbar.setOnMenuItemClickListener(item -> {

            if(item.getItemId() == R.id.action_save)
            {
                SaveTimesheet();
            }
            dismiss();
            return true;
        });
    }

    private void SaveTimesheet() {
        //TODO implement actual save method
        Toast.makeText(getContext(), "Something went wrong! Please try again!", Toast.LENGTH_LONG).show();
    }
}

