package com.rockthevote.grommet.ui.registration;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;

import com.f2prateek.rx.preferences.Preference;
import com.jakewharton.rxbinding.widget.RxCompoundButton;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.mobsandgeeks.saripaar.annotation.Checked;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.db.model.ContactMethod;
import com.rockthevote.grommet.data.db.model.RockyRequest;
import com.rockthevote.grommet.data.db.model.VoterClassification;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.ui.misc.ObservableValidator;
import com.rockthevote.grommet.util.Dates;
import com.squareup.sqlbrite.BriteDatabase;

import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.rockthevote.grommet.data.db.Db.DEBOUNCE;
import static com.rockthevote.grommet.data.db.model.ContactMethod.Type.EMAIL;
import static com.rockthevote.grommet.data.db.model.VoterClassification.Type.CITIZEN;
import static com.rockthevote.grommet.data.db.model.VoterClassification.Type.EIGHTEEN;

public class NewRegistrantFragment extends BaseRegistrationFragment implements
        DatePickerDialog.OnDateSetListener {


    @Email
    @BindView(R.id.text_input_layout_email)
    TextInputLayout textInputEmail;
    @BindView(R.id.edittext_email) TextInputEditText email;

    @NotEmpty
    @BindView(R.id.til_birthday)
    TextInputLayout tilBirthday;
    @BindView(R.id.edittext_birthday) TextInputEditText birthday;

    @BindView(R.id.email_opt_in) CheckBox emailOptIn;

    @Checked(messageResId = R.string.eighteen_or_older_err)
    @BindView(R.id.checkbox_is_eighteen)
    CheckBox checkBoxIsEighteen;

    @Checked(messageResId = R.string.us_citizen_err)
    @BindView(R.id.checkbox_is_us_citizen)
    CheckBox checkBoxIsUSCitizen;

    @Inject BriteDatabase db;
    @Inject @CurrentRockyRequestId Preference<Long> rockyRequestRowId;

    private ObservableValidator validator;

    private CompositeSubscription subscriptions;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setContentView(R.layout.fragment_new_registrant);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        validator = new ObservableValidator(this, getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        subscriptions = new CompositeSubscription();

        subscriptions.add(RxTextView.afterTextChangeEvents(email)
                .observeOn(Schedulers.io())
                .debounce(DEBOUNCE, TimeUnit.MILLISECONDS)
                .skip(1)
                .subscribe(event ->
                {
                    ContactMethod.insertOrUpdate(db, rockyRequestRowId.get(), EMAIL,
                            new ContactMethod.Builder()
                                    .value(event.editable().toString())
                                    .build());
                }));


        subscriptions.add(RxCompoundButton.checkedChanges(emailOptIn)
                .observeOn(Schedulers.io())
                .debounce(DEBOUNCE, TimeUnit.MILLISECONDS)
                .skip(1)
                .subscribe(checked -> {
                    db.update(
                            RockyRequest.TABLE,
                            new RockyRequest.Builder()
                                    .optInEmail(checked)
                                    .build(),
                            RockyRequest._ID + " = ? ", String.valueOf(rockyRequestRowId.get()));
                }));

        subscriptions.add(RxCompoundButton.checkedChanges(checkBoxIsUSCitizen)
                .observeOn(Schedulers.io())
                .debounce(DEBOUNCE, TimeUnit.MILLISECONDS)
                .skip(1)
                .subscribe(checked -> {
                    VoterClassification.insertOrUpdate(db, rockyRequestRowId.get(), CITIZEN,
                            new VoterClassification.Builder()
                                    .assertion(checked)
                                    .build());
                }));

        subscriptions.add(RxCompoundButton.checkedChanges(checkBoxIsEighteen)
                .observeOn(Schedulers.io())
                .debounce(DEBOUNCE, TimeUnit.MILLISECONDS)
                .skip(1)
                .subscribe(checked -> {
                    VoterClassification.insertOrUpdate(db, rockyRequestRowId.get(), EIGHTEEN,
                            new VoterClassification.Builder()
                                    .assertion(checked)
                                    .build());
                }));
    }

    @Override
    public void onPause() {
        super.onPause();
        subscriptions.unsubscribe();
    }

    @OnClick(R.id.edittext_birthday)
    public void onClickBirthday(View v) {
        DatePickerDialogFragment.newInstance(this).show(getFragmentManager(), "datepicker");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        GregorianCalendar date = new GregorianCalendar(year, monthOfYear, dayOfMonth);
        birthday.setText(Dates.formatAsISO8601_ShortDate(date.getTime()));

        db.update(
                RockyRequest.TABLE,
                new RockyRequest.Builder()
                        .dateOfBirth(date.getTime())
                        .build(),
                RockyRequest._ID + " = ? ", String.valueOf(rockyRequestRowId.get()));
    }

    @Override
    public Observable<Boolean> verify() {
        return validator.validate();
    }

}
