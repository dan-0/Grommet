package com.rockthevote.grommet.ui.registration.name;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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
import com.rockthevote.grommet.databinding.FragmentNewRegistrantBinding;
import com.rockthevote.grommet.ui.misc.ObservableValidator;
import com.rockthevote.grommet.ui.registration.BaseRegistrationFragment;
import com.rockthevote.grommet.ui.registration.DatePickerDialogFragment;
import com.rockthevote.grommet.ui.registration.RegistrationData;
import com.rockthevote.grommet.ui.registration.RegistrationViewModel;
import com.rockthevote.grommet.util.Dates;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class NewRegistrantFragment extends BaseRegistrationFragment {

    @NotEmpty(messageResId = R.string.required_field)
    @BindView(R.id.til_birthday)
    TextInputLayout tilBirthday;

    @Checked(messageResId = R.string.eighteen_or_older_err)
    @BindView(R.id.checkbox_is_eighteen)
    CheckBox checkBoxIsEighteen;

    @Checked(messageResId = R.string.us_citizen_err)
    @BindView(R.id.checkbox_is_us_citizen)
    CheckBox checkBoxIsUSCitizen;

    @Inject
    @CurrentRockyRequestId
    Preference<Long> rockyRequestRowId;
    @Inject
    @RegistrationDeadline
    Preference<Date> registrationDeadline;

    private ObservableValidator validator;

    private CompositeSubscription subscriptions;

    private FragmentNewRegistrantBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNewRegistrantBinding.inflate(inflater, container, false);
        return wrapBinding(binding.getRoot(), inflater, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        validator = new ObservableValidator(this, getActivity());
        binding.edittextBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date startDate = Dates.parseISO8601_ShortDate(binding.edittextBirthday.getText().toString());
                DatePickerDialogFragment.newInstance(NewRegistrantFragment.this::onDateSet, startDate)
                        .show(getFragmentManager(), "datepicker");
            }
        });

        binding.nameChanged.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.previousName.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            binding.previousNameDivider.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
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

    private void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        GregorianCalendar date = new GregorianCalendar(year, monthOfYear, dayOfMonth);
        binding.edittextBirthday.setText(Dates.formatAsISO8601_ShortDate(date.getTime()));

        validateBirthday();
    }

    private boolean validateBirthday() {
        boolean valid = isEighteenByDeadline();
        if (!valid) {
            binding.tilBirthday.setError(String.format(getString(R.string.birthday_error),
                    Dates.formatAsISO8601_ShortDate(registrationDeadline.get())));
        } else {
            binding.tilBirthday.setError(null);
        }

        return valid;
    }

    private boolean isEighteenByDeadline() {
        /*
            check if registrant will be 18 by the election date.
            Calendar uses 0 as the first month but LocalDate does not, so make sure and add 1 to it
         */

        Date birthDate = Dates.parseISO8601_ShortDate(binding.edittextBirthday.getText().toString());
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

        Observable<Boolean> previousNameObs = binding.previousName.verify()
                .flatMap(val -> Observable.just(binding.nameChanged.isChecked() ? val : true));

        return Observable.zip(
                validator.validate(),
                binding.name.verify(),
                previousNameObs,
                Observable.just(isEighteenByDeadline()),
                (validator, name, prevName, birthday)
                        -> validator && name && prevName && birthday);
    }

    @Override
    public void storeState() {
        NewRegistrantData data = NewRegistrantExtKt.toNameRegistrationData(binding);
        viewModel.storeNewRegistrantData(data);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
