package com.m2049r.xmrwallet.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.m2049r.xmrwallet.LoginActivity;
import com.m2049r.xmrwallet.PreLoginActivity;
import com.m2049r.xmrwallet.R;

public class SigninFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = SigninFragment.class.getName();

    private Context mContext;
    private TextView signUpTV;
    private Button signInBTN;
    private TextView headerTitleTV;
    private TextView recoveryTV;

    public static SigninFragment newInstance() {

        Bundle args = new Bundle();

        SigninFragment fragment = new SigninFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_signin, container, false);
        mContext = getActivity();
        initComponents(rootView);
        initListeners();
        return rootView;
    }

    private void initComponents(View rootView) {
        signUpTV = rootView.findViewById(R.id.tv_sign_up);
        signInBTN = rootView.findViewById(R.id.btn_sign_in);
        headerTitleTV = rootView.findViewById(R.id.tv_header_title);
        headerTitleTV.setText("Sign in");
        recoveryTV = rootView.findViewById(R.id.tv_recovery);
    }

    private void initListeners() {
        signUpTV.setOnClickListener(this);
        signInBTN.setOnClickListener(this);
        recoveryTV.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        Intent intent;
        switch (id) {
            case R.id.tv_sign_up:
                ((PreLoginActivity) getActivity()).replaceFragment(SignupFragment.newInstance());
                break;
            case R.id.btn_sign_in:
                intent = new Intent(mContext, LoginActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_recovery:
                ((PreLoginActivity) getActivity()).replaceFragment(RecoveryFragment.newInstance());
                break;
        }
    }
}
