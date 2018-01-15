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

import com.f2prateek.rx.preferences2.Preference;
import com.jakewharton.rxbinding.widget.RxCompoundButton;
import com.mobsandgeeks.saripaar.annotation.Checked;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.db.model.RockyRequest;
import com.rockthevote.grommet.data.db.model.VoterClassification;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.ui.misc.ObservableValidator;
import com.rockthevote.grommet.ui.views.NameView;
import com.rockthevote.grommet.util.Dates;
import com.squareup.sqlbrite.BriteDatabase;

import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.rockthevote.grommet.data.db.Db.DEBOUNCE;
import static com.rockthevote.grommet.data.db.model.VoterClassification.Type.CITIZEN;
import static com.rockthevote.grommet.data.db.model.VoterClassification.Type.EIGHTEEN;

public class NewRegistrantFragment extends BaseRegistrationFragment implements
        DatePickerDialog.OnDateSetListener {

    @BindView(R.id.name) NameView name;
    @BindView(R.id.name_changed) CheckBox nameChanged;
    @BindView(R.id.previous_name_divider) View previousNameDivider;
    @BindView(R.id.previous_name) NameView previousName;

    @NotEmpty
    @BindView(R.id.til_birthday) TextInputLayout tilBirthday;
    @BindView(R.id.edittext_birthday) TextInputEditText birthday;

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

    @OnCheckedChanged(R.id.name_changed)
    public void onNameChangedChecked(boolean checked) {
        previousName.setVisibility(checked ? View.VISIBLE : View.GONE);
        previousNameDivider.setVisibility(checked ? View.VISIBLE : View.GONE);

        db.update(RockyRequest.TABLE,
                new RockyRequest.Builder()
                        .hasPreviousName(checked)
                        .build(),
                RockyRequest._ID + " = ? ", String.valueOf(rockyRequestRowId.get()));
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

        Observable<Boolean> previousNameObs = previousName.verify()
                .flatMap(val -> Observable.just(nameChanged.isChecked() ? val : true));

        return validator.validate()
                .concatWith(name.verify())
                .concatWith(previousNameObs)
                .toList()
                .flatMap(list -> {
                    Boolean ret = true;
                    for (Boolean val : list) {
                        ret = ret && val;
                    }

                    return Observable.just(ret);
                });
    }
}
