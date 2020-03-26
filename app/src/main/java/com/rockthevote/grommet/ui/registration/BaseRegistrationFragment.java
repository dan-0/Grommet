package com.rockthevote.grommet.ui.registration;

import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.databinding.FragmentRegistrationBaseBinding;

import rx.Observable;

public abstract class BaseRegistrationFragment extends Fragment {

    private @LayoutRes int contentView;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Injector.obtain(getActivity()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_registration_base, container, false);
        CardView content = (CardView) v.findViewById(R.id.content_area);
        inflater.inflate(contentView, content);
        return v;
    }

    protected View wrapBinding(
            View view,
            LayoutInflater inflater,
            ViewGroup container
    ) {

        FragmentRegistrationBaseBinding binding = FragmentRegistrationBaseBinding
                .inflate(inflater, container, false);

        binding.contentArea.addView(view);

        return binding.getRoot();
    }

    protected void setContentView(@LayoutRes int contentView) {
        this.contentView = contentView;
    }

    public Observable<Boolean> verify() {
        return Observable.just(true);
    }

    /**
     * Stores the current Fragment's state. ONLY store valid state or it will fail.
     */
    public abstract void storeState();
}
