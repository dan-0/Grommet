package com.rockthevote.grommet.ui.registration.review;

import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.f2prateek.rx.preferences2.Preference;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.db.model.FormLanguage;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.data.prefs.CurrentSessionRowId;
import com.rockthevote.grommet.databinding.FragmentReviewAndConfirmBinding;
import com.rockthevote.grommet.ui.registration.BaseRegistrationFragment;
import com.rockthevote.grommet.ui.registration.DisclosureAgreementDialogFragment;
import com.rockthevote.grommet.ui.registration.RegistrationCompleteDialogFragment;
import com.rockthevote.grommet.util.Images;
import com.rockthevote.grommet.util.LocaleUtils;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import rx.subscriptions.CompositeSubscription;

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

    private CompositeSubscription subscriptions;
    private DisclosureAgreementDialogFragment dialog;

    private FragmentReviewAndConfirmBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReviewAndConfirmBinding.inflate(inflater, container, false);
        return wrapBinding(binding.getRoot(), inflater, container);
    }

    @Override
    public void storeState() {
        ReviewData data = ReviewExtKt.toReviewData(binding);
        viewModel.storeReviewData(data);
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

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Injector.obtain(getActivity()).inject(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        subscriptions.unsubscribe();
        signaturePad.setOnSignedListener(null);
        dialog.setListener(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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

    }

    @Override
    public void onClear() {
    }

    // the default value is set in the data module
    @SuppressWarnings("ConstantConditions")
    @OnClick(R.id.button_register)
    public void onRegisterClick(View v) {
        if (!signaturePad.isEmpty()) {
            signaturePadError.setVisibility(View.GONE);

            // get language the form was completed in
            FormLanguage lang = "es".equals(Locale.getDefault().getLanguage()) ?
                    FormLanguage.SPANISH : FormLanguage.ENGLISH;

            RegistrationCompleteDialogFragment dialog = new RegistrationCompleteDialogFragment();
            dialog.setCancelable(false);
            dialog.show(getFragmentManager(), "complete_dialog");

            LocaleUtils.setLocale(new Locale("en"));
            storeState();
        } else {
            signaturePadError.setVisibility(View.VISIBLE);
        }
    }

}
