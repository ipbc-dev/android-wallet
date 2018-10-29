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

public class SignupFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = SignupFragment.class.getName();

    private Context mContext;
    private TextView signInTV;
    private Button signUpBTN;
    private TextView headerTitleTV;

    public static SignupFragment newInstance() {

        Bundle args = new Bundle();

        SignupFragment fragment = new SignupFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_signup, container, false);
        mContext = getActivity();
        initComponents(rootView);
        initListeners();
        return rootView;
    }

    private void initComponents(View rootView) {
        signUpBTN = rootView.findViewById(R.id.btn_sign_up);
        signInTV = rootView.findViewById(R.id.tv_sign_in);
        headerTitleTV = rootView.findViewById(R.id.tv_header_title);
        headerTitleTV.setText("Sign up");
    }

    private void initListeners() {
        signUpBTN.setOnClickListener(this);
        signInTV.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        Intent intent;
        switch (id) {
            case R.id.btn_sign_up:
                intent = new Intent(mContext, LoginActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_sign_in:
                ((PreLoginActivity) getActivity()).replaceFragment(SigninFragment.newInstance());
                break;
        }
    }
}
