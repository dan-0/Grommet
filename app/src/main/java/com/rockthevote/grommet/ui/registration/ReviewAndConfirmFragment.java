package com.rockthevote.grommet.ui.registration;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.f2prateek.rx.preferences.Preference;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.db.model.Address;
import com.rockthevote.grommet.data.db.model.ContactMethod;
import com.rockthevote.grommet.data.db.model.Name;
import com.rockthevote.grommet.data.db.model.RockyRequest;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.util.Dates;
import com.squareup.sqlbrite.BriteDatabase;

import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

import static com.rockthevote.grommet.data.db.model.Address.Type.MAILING;
import static com.rockthevote.grommet.data.db.model.Address.Type.REGISTRATION;
import static com.rockthevote.grommet.data.db.model.ContactMethod.Type.EMAIL;
import static com.rockthevote.grommet.data.db.model.ContactMethod.Type.PHONE;
import static com.rockthevote.grommet.data.db.model.Name.Type.CURRENT;

public class ReviewAndConfirmFragment extends BaseRegistrationFragment implements SignaturePad.OnSignedListener {

    @BindViews({R.id.review_mail_address,
                       R.id.review_mailing_address_section_title,
                       R.id.review_mailing_address_divider}) List<View> mailingAddressViews;

    @BindView(R.id.review_name) TextView name;
    @BindView(R.id.review_birthday) TextView birthday;
    @BindView(R.id.review_email) TextView email;
    @BindView(R.id.review_phone) TextView phone;

    @BindView(R.id.review_reg_address) TextView registrationAddress;
    @BindView(R.id.review_mail_address) TextView mailingAddress;

    @BindView(R.id.review_party) TextView party;
    @BindView(R.id.review_race) TextView race;

    @BindView(R.id.signature_pad) SignaturePad signaturePad;
    @BindView(R.id.signature_pad_error) TextView signaturePadError;
    @BindView(R.id.button_register) Button buttonRegister;

    @Inject @CurrentRockyRequestId Preference<Long> rockyRequestRowId;

    @Inject BriteDatabase db;

    private CompositeSubscription subscriptions;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setContentView(R.layout.fragment_review_and_confirm);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    public void onResume() {
        super.onResume();
        subscriptions = new CompositeSubscription();

        signaturePad.setOnSignedListener(this);

        subscriptions.add(db.createQuery(Name.TABLE, Name.SELECT_BY_TYPE,
                new String[]{String.valueOf(rockyRequestRowId.get()), CURRENT.toString()})
                .mapToOne(Name.MAPPER)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(name -> {
                    this.name.setText(name.toString());
                }));

        subscriptions.add(db.createQuery(Address.TABLE, Address.SELECT_BY_TYPE,
                new String[]{String.valueOf(rockyRequestRowId.get()), REGISTRATION.toString()})
                .mapToOne(Address.MAPPER)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(address -> {
                    registrationAddress.setText(address.toString());
                }));

        subscriptions.add(db.createQuery(RockyRequest.TABLE, RockyRequest.SELECT_BY_ID,
                String.valueOf(rockyRequestRowId.get()))
                .mapToOne(RockyRequest.MAPPER)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rockyRequest -> {
                    birthday.setText(Dates.formatAsISO8601_ShortDate(rockyRequest.dateOfBirth()));
                    race.setText(rockyRequest.race().toString());
                    party.setText(rockyRequest.party().toString());

                    // show/hide registration address views
                    for (View v : mailingAddressViews) {
                        v.setVisibility(rockyRequest.regIsMail() ? View.GONE : View.VISIBLE);
                    }
                }));

        subscriptions.add(db.createQuery(Address.TABLE, Address.SELECT_BY_TYPE,
                new String[]{String.valueOf(rockyRequestRowId.get()), MAILING.toString()})
                .mapToOne(Address.MAPPER)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(address -> {
                    mailingAddress.setText(address.toString());
                }));

        subscriptions.add(db.createQuery(ContactMethod.TABLE, ContactMethod.SELECT_BY_TYPE,
                new String[]{String.valueOf(rockyRequestRowId.get()), EMAIL.toString()})
                .mapToOne(ContactMethod.MAPPER)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(contactMethod -> {
                    email.setText(contactMethod.value());
                }));

        subscriptions.add(db.createQuery(ContactMethod.TABLE, ContactMethod.SELECT_BY_TYPE,
                new String[]{String.valueOf(rockyRequestRowId.get()), PHONE.toString()})
                .mapToOne(ContactMethod.MAPPER)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(contactMethod -> {
                    phone.setText(contactMethod.value());
                }));

    }

    @Override
    public void onPause() {
        super.onPause();
        subscriptions.unsubscribe();
        signaturePad.setOnSignedListener(null);
    }

    @OnClick(R.id.clear_signature)
    public void onClearSignatureClick(View v) {
        signaturePad.clear();
    }

    @OnCheckedChanged(R.id.checkbox_agreement)
    public void onCheckChanged(boolean checked) {
        buttonRegister.setEnabled(checked);
    }

    @Override
    public void onStartSigning() {

    }

    @Override
    public void onSigned() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        signaturePad.getSignatureBitmap().compress(Bitmap.CompressFormat.JPEG, 100, baos);
        db.update(RockyRequest.TABLE,
                new RockyRequest.Builder()
                        .signature(baos.toByteArray())
                        .build(),
                RockyRequest._ID + " = ? ", String.valueOf(rockyRequestRowId.get()));
    }

    @Override
    public void onClear() {
        db.update(RockyRequest.TABLE,
                new RockyRequest.Builder()
                        .signature(new byte[0])
                        .build(),
                RockyRequest._ID + " = ? ", String.valueOf(rockyRequestRowId.get()));
    }

    @OnClick(R.id.button_register)
    public void onRegisterClick(View v) {
        if (!signaturePad.isEmpty()) {
            signaturePadError.setVisibility(View.INVISIBLE);
            db.update(RockyRequest.TABLE,
                    new RockyRequest.Builder()
                            .status(RockyRequest.Status.FORM_COMPLETE)
                            .build(),
                    RockyRequest._ID + " = ? ", String.valueOf(rockyRequestRowId.get()));

            new RegistrationCompleteDialogFragment().show(getFragmentManager(), "complete_dialog");
        } else {
            signaturePadError.setVisibility(View.VISIBLE);
        }
    }
}
