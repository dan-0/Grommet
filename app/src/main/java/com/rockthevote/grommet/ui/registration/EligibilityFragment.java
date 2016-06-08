package com.rockthevote.grommet.ui.registration;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Checked;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.rockthevote.grommet.R;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class EligibilityFragment extends Fragment implements DatePickerDialog.OnDateSetListener {


    @Email
    @BindView(R.id.edittext_email)
    EditText editTextEmail;

    @NotEmpty
    @BindView(R.id.edittext_birthday)
    EditText editTextBirthday;

    @Checked(messageResId = R.string.eighteen_or_older_err)
    @BindView(R.id.checkbox_is_eighteen)
    CheckBox checkBoxIsEighteen;

    @Checked(messageResId = R.string.us_citizen_err)
    @BindView(R.id.checkbox_is_us_citizen)
    CheckBox checkBoxIsUSCitizen;

    @BindView(R.id.checkbox_first_time) CheckBox checkBoxIsFirstTime;
    @BindView(R.id.checkbox_has_id) CheckBox checkBoxHasID;


    private Validator validator;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_eligibility, container, false);
        ButterKnife.bind(this, v);

        validator = new Validator(this);
        return v;
    }

    @OnClick(R.id.edittext_birthday)
    public void onClickBirthday(View v) {
        DatePickerDialogFragment.newInstance(this).show(getFragmentManager(), "datepicker");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        String dateFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);

        GregorianCalendar birthday = new GregorianCalendar(year, monthOfYear, dayOfMonth);
        editTextBirthday.setText(sdf.format(birthday.getTime()));
    }

    //TODO remove
    @OnCheckedChanged(R.id.checkbox_first_time)
    public void onCheckChangedFirstTime(boolean checked) {
        validate()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isValid -> {
                    Toast.makeText(getActivity(), String.valueOf(isValid), Toast.LENGTH_SHORT).show();
                });
    }

    public Observable<Boolean> validate() {

        return Observable.create(subscriber -> {
            validator.setValidationListener(new Validator.ValidationListener() {
                @Override
                public void onValidationSucceeded() {
                    subscriber.onNext(true);
                    subscriber.onCompleted();
                }

                @Override
                public void onValidationFailed(List<ValidationError> errors) {
                    for (ValidationError error : errors) {
                        View view = error.getView();
                        String message = error.getCollatedErrorMessage(getActivity());
                        if (view instanceof EditText) {
                            ((EditText) view).setError(message);
                        } else if (view instanceof CheckBox) {
                            ((CheckBox) view).setError(message);
                        }
                    }
                    subscriber.onNext(false);
                    subscriber.onCompleted();
                }
            });
            validator.validate();
        });
    }
}
