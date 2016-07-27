package com.rockthevote.grommet.ui.registration;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rockthevote.grommet.R;

import java.util.Timer;
import java.util.TimerTask;

public class RegistrationCompleteDialogFragment extends DialogFragment {

    private long delay = 1500;

    private Timer timer;
    private TimerTask task;

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if(null != dialog){
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setCancelable(false);

        return inflater.inflate(R.layout.dialog_registration_complete, container);
    }

    @Override
    public void onResume() {
        super.onResume();
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                getActivity().finish();
            }
        };
        timer.schedule(task, delay);

    }

    @Override
    public void onPause() {
        super.onPause();
        task.cancel();
    }
}
