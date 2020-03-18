package com.rockthevote.grommet.ui.eventFlow;

import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.f2prateek.rx.preferences2.Preference;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.db.model.Session;
import com.rockthevote.grommet.data.prefs.CurrentSessionRowId;
import com.rockthevote.grommet.util.Strings;
import com.squareup.sqlbrite.BriteDatabase;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

/**
 * Created by Mechanical Man, LLC on 8/16/17. Grommet
 */

public class SessionProgressDialogFragment extends DialogFragment {

    @Inject BriteDatabase db;
    @Inject @CurrentSessionRowId Preference<Long> currentSessionRowId;

    // Total Counts
    @BindView(R.id.summary_total_registrations) TextView totalRegistrations;
    @BindView(R.id.summary_total_abandoned) TextView totalAbandoned;
    @BindView(R.id.summary_total_dln) TextView totalDLN;
    @BindView(R.id.summary_total_ssn) TextView totalSSN;
    @BindView(R.id.summary_total_email_opt_in) TextView totalEmailOptIn;
    @BindView(R.id.summary_total_sms_opt_in) TextView totalSMSOptIn;

    // Percentage Counts
    @BindView(R.id.summary_dln_percentage) TextView percentDLN;
    @BindView(R.id.summary_ssn_percentage) TextView percentSSN;
    @BindView(R.id.summary_email_opt_in_percentage) TextView percentEmailOptIn;
    @BindView(R.id.summary_sms_opt_in_percentage) TextView percentSMSOptIn;

    static SessionProgressDialogFragment newInstance() {
        return new SessionProgressDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.obtain(getContext()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        if (getShowsDialog()) {
            view = inflater.inflate(R.layout.session_progress_dialog_fragment, container, false);
        } else {
            view = inflater.inflate(R.layout.summary_totals, container, false);
        }

        ButterKnife.bind(this, view);
        updateView();

        return view;
    }

    public void updateView() {
        // update count totals
        Cursor cursor = db.query(Session.SELECT_CURRENT_SESSION);
        if (cursor.moveToNext()) {
            Session session = Session.MAPPER.call(cursor);

            totalRegistrations.setText(String.valueOf(session.totalRegistrations()));
            totalAbandoned.setText(String.valueOf(session.totalAbandoned()));
            totalDLN.setText(String.valueOf(session.totalIncludeDLN()));
            totalSSN.setText(String.valueOf(session.totalIncludeSSN()));
            totalEmailOptIn.setText(String.valueOf(session.totalEmailOptIn()));
            totalSMSOptIn.setText(String.valueOf(session.totalSMSOptIn()));

            // update count totals
            totalRegistrations.setText(String.valueOf(session.totalRegistrations()));
            totalAbandoned.setText(String.valueOf(session.totalAbandoned()));
            totalDLN.setText(String.valueOf(session.totalIncludeDLN()));
            totalSSN.setText(String.valueOf(session.totalIncludeSSN()));
            totalEmailOptIn.setText(String.valueOf(session.totalEmailOptIn()));
            totalSMSOptIn.setText(String.valueOf(session.totalSMSOptIn()));

            // update percentages
            double totalReg = session.totalRegistrations() * 1.0;

            if (totalReg > 0) {
                percentDLN.setText(
                        Strings.formatNumberAsPercentage(session.totalIncludeDLN() / totalReg));
                percentSSN.setText(
                        Strings.formatNumberAsPercentage(session.totalIncludeSSN() / totalReg));
                percentEmailOptIn.setText(
                        Strings.formatNumberAsPercentage(session.totalEmailOptIn() / totalReg));
                percentSMSOptIn.setText(
                        Strings.formatNumberAsPercentage(session.totalSMSOptIn() / totalReg));
            }
        }
        cursor.close();
    }

    @Optional
    @OnClick(R.id.session_progress_dismiss)
    public void onDismissClick(View v) {
        getDialog().dismiss();
    }
}
