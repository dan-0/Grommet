package com.rockthevote.grommet.ui.registration;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.f2prateek.rx.preferences2.Preference;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.api.RegistrationService;
import com.rockthevote.grommet.data.db.model.Address;
import com.rockthevote.grommet.data.db.model.ContactMethod;
import com.rockthevote.grommet.data.db.model.Name;
import com.rockthevote.grommet.data.db.model.RockyRequest;
import com.rockthevote.grommet.data.db.model.RockyRequest.Language;
import com.rockthevote.grommet.data.db.model.Session;
import com.rockthevote.grommet.data.db.model.VoterId;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.data.prefs.CurrentSessionRowId;
import com.rockthevote.grommet.util.Dates;
import com.rockthevote.grommet.util.Images;
import com.rockthevote.grommet.util.LocaleUtils;
import com.squareup.sqlbrite.BriteDatabase;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

import static com.rockthevote.grommet.data.db.model.Address.Type.MAILING_ADDRESS;
import static com.rockthevote.grommet.data.db.model.Address.Type.REGISTRATION_ADDRESS;
import static com.rockthevote.grommet.data.db.model.ContactMethod.Type.EMAIL;
import static com.rockthevote.grommet.data.db.model.ContactMethod.Type.PHONE;
import static com.rockthevote.grommet.data.db.model.Name.Type.CURRENT_NAME;

public class ReviewAndConfirmFragment extends BaseRegistrationFragment implements SignaturePad.OnSignedListener {

    @BindViews({R.id.review_mail_address,
                       R.id.review_mailing_address_section_title,
                       R.id.review_mailing_address_divider}) List<View> mailingAddressViews;

    @BindView(R.id.review_name) TextView name;
    @BindView(R.id.review_birthday) TextView birthday;
    @BindView(R.id.review_email) TextView email;
    @BindView(R.id.review_phone) TextView phone;
    @BindView(R.id.review_phone_label) TextView phoneLabel;

    @BindView(R.id.review_reg_address) TextView registrationAddress;
    @BindView(R.id.review_mail_address) TextView mailingAddress;

    @BindView(R.id.review_party) TextView party;
    @BindView(R.id.review_race) TextView race;

    @BindView(R.id.signature_pad) SignaturePad signaturePad;
    @BindView(R.id.signature_pad_error) TextView signaturePadError;
    @BindView(R.id.button_register) Button buttonRegister;

    @BindView(R.id.checkbox_agreement) CheckBox confirmCheckbox;

    @Inject @CurrentRockyRequestId Preference<Long> rockyRequestRowId;
    @Inject @CurrentSessionRowId Preference<Long> currentSessionRowId;

    @Inject BriteDatabase db;

    private CompositeSubscription subscriptions;
    private DisclosureAgreementDialogFragment dialog;

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
        dialog = new DisclosureAgreementDialogFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        dialog.setListener(new DisclosureAgreementDialogFragment.DisclosureListener() {
            @Override
            public void onDeclineClick() {
                confirmCheckbox.setChecked(false);
                dialog.dismiss();
            }

            @Override
            public void onAcceptClick() {
                buttonRegister.setEnabled(true);
                dialog.dismiss();
            }
        });

        subscriptions = new CompositeSubscription();

        signaturePad.setOnSignedListener(this);

        subscriptions.add(db.createQuery(Name.TABLE, Name.SELECT_BY_TYPE,
                new String[]{String.valueOf(rockyRequestRowId.get()), CURRENT_NAME.toString()})
                .mapToOne(Name.MAPPER)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(name -> {
                    this.name.setText(name.toString());
                }));

        subscriptions.add(db.createQuery(Address.TABLE, Address.SELECT_BY_TYPE,
                new String[]{String.valueOf(rockyRequestRowId.get()), REGISTRATION_ADDRESS.toString()})
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

                    String polParty;
                    if (RockyRequest.Party.OTHER_PARTY == rockyRequest.party()) {
                        polParty = rockyRequest.otherParty();
                    } else {
                        polParty = rockyRequest.party().toString();
                    }

                    party.setText(polParty);
                    phoneLabel.setText(rockyRequest.phoneType().toString());
                    // show/hide registration address views
                    for (View v : mailingAddressViews) {
                        v.setVisibility(rockyRequest.hasMailingAddress() ? View.VISIBLE : View.GONE);
                    }
                }));

        subscriptions.add(db.createQuery(Address.TABLE, Address.SELECT_BY_TYPE,
                new String[]{String.valueOf(rockyRequestRowId.get()), MAILING_ADDRESS.toString()})
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
        dialog.setListener(null);
    }

    @OnClick(R.id.clear_signature)
    public void onClearSignatureClick(View v) {
        signaturePad.clear();
    }

    @OnCheckedChanged(R.id.checkbox_agreement)
    public void onCheckChanged(boolean checked) {
        if (checked) {
            dialog.show(getFragmentManager(), "disclosure_dialog");
        } else {
            buttonRegister.setEnabled(false);
        }
    }

    @Override
    public void onStartSigning() {

    }

    @Override
    public void onSigned() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap image = Images.transformAspectRatio(signaturePad.getSignatureBitmap(), 3, 1);
        image = Images.aspectSafeScale(image, 180, 60);
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);

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

    // the default value is set in the data module
    @SuppressWarnings("ConstantConditions")
    @OnClick(R.id.button_register)
    public void onRegisterClick(View v) {
        if (!signaturePad.isEmpty()) {
            signaturePadError.setVisibility(View.GONE);

            // get language the form was completed in
            Language lang = "es".equals(Locale.getDefault().getLanguage()) ?
                    Language.SPANISH : Language.ENGLISH;

            db.update(RockyRequest.TABLE,
                    new RockyRequest.Builder()
                            .language(lang)
                            .status(RockyRequest.Status.FORM_COMPLETE)
                            .build(),
                    RockyRequest._ID + " = ? ", String.valueOf(rockyRequestRowId.get()));

            updateSessionTotals();

            Intent regService = new Intent(getContext(), RegistrationService.class);
            getActivity().startService(regService);

            RegistrationCompleteDialogFragment dialog = new RegistrationCompleteDialogFragment();
            dialog.setCancelable(false);
            dialog.show(getFragmentManager(), "complete_dialog");

            LocaleUtils.setLocale(new Locale("en"));
        } else {
            signaturePadError.setVisibility(View.VISIBLE);
        }
    }

    private void updateSessionTotals() {
        Cursor rockyCursor =
                db.query(RockyRequest.SELECT_BY_ID, String.valueOf(rockyRequestRowId.get()));
        Cursor sessionCursor =
                db.query(Session.SELECT_CURRENT_SESSION);

        if (rockyCursor.moveToNext() && sessionCursor.moveToNext()) {
            RockyRequest rockyRequest = RockyRequest.MAPPER.call(rockyCursor);
            Session session = Session.MAPPER.call(sessionCursor);

            boolean hasDLN = false;
            boolean hasSSN = false;

            Cursor voterIdCursor = db.query(VoterId.SELECT_BY_ROCKY_REQUEST_ID,
                    String.valueOf(rockyRequestRowId.get()));
            while (voterIdCursor.moveToNext()) {
                VoterId voterId = VoterId.MAPPER.call(voterIdCursor);
                if (VoterId.Type.DRIVERS_LICENSE == voterId.type()) {
                    hasDLN = !voterId.attestNoSuchId();
                }

                if (VoterId.Type.SSN_LAST_FOUR == voterId.type()) {
                    hasSSN = !voterId.attestNoSuchId();
                }
            }

            voterIdCursor.close();

            Session.Builder sessionValues = new Session.Builder()
                    .totalRegistrations(session.totalRegistrations()
                            + 1)
                    .totalEmailOptIn(session.totalEmailOptIn()
                            + (rockyRequest.partnerOptInEmail() ? 1 : 0))
                    .totalSMSOptIn(session.totalSMSOptIn()
                            + (rockyRequest.partnerOptInSMS() ? 1 : 0))
                    .totalIncludeSSN(session.totalIncludeSSN()
                            + (hasSSN ? 1 : 0))
                    .totalIncludeDLN(session.totalIncludeDLN()
                            + (hasDLN ? 1 : 0));

            db.update(Session.TABLE,
                    sessionValues.build(), Session._ID + " = ? ",
                    String.valueOf(currentSessionRowId.get()));
        }

        rockyCursor.close();
        sessionCursor.close();
    }
}
