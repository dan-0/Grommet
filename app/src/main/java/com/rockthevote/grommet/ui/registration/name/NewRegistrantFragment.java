package com.rockthevote.grommet.ui.registration.name;

import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;

import com.f2prateek.rx.preferences2.Preference;
import com.mobsandgeeks.saripaar.annotation.Checked;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.data.prefs.RegistrationDeadline;
import com.rockthevote.grommet.ui.misc.ObservableValidator;
import com.rockthevote.grommet.ui.registration.BaseRegistrationFragment;
import com.rockthevote.grommet.ui.registration.DatePickerDialogFragment;
import com.rockthevote.grommet.ui.views.NameView;
import com.rockthevote.grommet.util.Dates;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

public class NewRegistrantFragment extends BaseRegistrationFragment implements
        DatePickerDialog.OnDateSetListener {

    @BindView(R.id.name) NameView name;
    @BindView(R.id.name_changed) CheckBox nameChanged;
    @BindView(R.id.previous_name_divider) View previousNameDivider;
    @BindView(R.id.previous_name) NameView previousName;

    @NotEmpty(messageResId = R.string.required_field)
    @BindView(R.id.til_birthday) TextInputLayout tilBirthday;
    @BindView(R.id.edittext_birthday) TextInputEditText birthdayEditText;

    @Checked(messageResId = R.string.eighteen_or_older_err)
    @BindView(R.id.checkbox_is_eighteen)
    CheckBox checkBoxIsEighteen;

    @Checked(messageResId = R.string.us_citizen_err)
    @BindView(R.id.checkbox_is_us_citizen)
    CheckBox checkBoxIsUSCitizen;

    @Inject @CurrentRockyRequestId Preference<Long> rockyRequestRowId;
    @Inject @RegistrationDeadline Preference<Date> registrationDeadline;

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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Injector.obtain(getActivity()).inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        subscriptions = new CompositeSubscription();

    }

    @Override
    public void onPause() {
        super.onPause();
        subscriptions.unsubscribe();
    }

    @OnClick(R.id.edittext_birthday)
    public void onClickBirthday(View v) {
        Date startDate = Dates.parseISO8601_ShortDate(birthdayEditText.getText().toString());
        DatePickerDialogFragment.newInstance(this, startDate)
                .show(getFragmentManager(), "datepicker");
    }

    @OnCheckedChanged(R.id.name_changed)
    public void onNameChangedChecked(boolean checked) {
        previousName.setVisibility(checked ? View.VISIBLE : View.GONE);
        previousNameDivider.setVisibility(checked ? View.VISIBLE : View.GONE);

    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        GregorianCalendar date = new GregorianCalendar(year, monthOfYear, dayOfMonth);
        birthdayEditText.setText(Dates.formatAsISO8601_ShortDate(date.getTime()));

        validateBirthday();
    }

    private boolean validateBirthday() {
        boolean valid = isEighteenByDeadline();
        if (!valid) {
            tilBirthday.setError(String.format(getString(R.string.birthday_error),
                    Dates.formatAsISO8601_ShortDate(registrationDeadline.get())));
        } else {
            tilBirthday.setError(null);
        }

        return valid;
    }

    private boolean isEighteenByDeadline() {
        /*
            check if registrant will be 18 by the election date.
            Calendar uses 0 as the first month but LocalDate does not, so make sure and add 1 to it
         */

        Date birthDate = Dates.parseISO8601_ShortDate(birthdayEditText.getText().toString());
        if (birthDate == null) {
            return false;
        }

        Calendar birthCal = Calendar.getInstance();
        birthCal.setTime(birthDate);

        Calendar regCal = Calendar.getInstance();
        regCal.setTime(registrationDeadline.get());

        LocalDate regDate = LocalDate.of(
                regCal.get(Calendar.YEAR),
                regCal.get(Calendar.MONTH) + 1,
                regCal.get(Calendar.DAY_OF_MONTH));

        LocalDate birthday = LocalDate.of(
                birthCal.get(Calendar.YEAR),
                birthCal.get(Calendar.MONTH) + 1,
                birthCal.get(Calendar.DAY_OF_MONTH));

        return ChronoUnit.YEARS.between(birthday, regDate) >= 18;
    }

    @Override
    public Observable<Boolean> verify() {

        Observable<Boolean> previousNameObs = previousName.verify()
                .flatMap(val -> Observable.just(nameChanged.isChecked() ? val : true));

        return Observable.zip(
                validator.validate(),
                name.verify(),
                previousNameObs,
                Observable.just(isEighteenByDeadline()),
                (validator, name, prevName, birthday)
                        -> validator && name && prevName && birthday);
    }
}
