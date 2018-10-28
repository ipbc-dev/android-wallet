package com.m2049r.xmrwallet.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.m2049r.xmrwallet.PreLoginActivity;
import com.m2049r.xmrwallet.R;

public class RecoveryFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = RecoveryFragment.class.getName();

    private Context mContext;
    private TextView signInTV;
    private Button recoveryBTN;
    private TextView headerTitleTV;

    public static RecoveryFragment newInstance() {

        Bundle args = new Bundle();

        RecoveryFragment fragment = new RecoveryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recovery, container, false);
        mContext = getActivity();
        initComponents(rootView);
        initListeners();
        return rootView;
    }

    private void initComponents(View rootView) {
        recoveryBTN = rootView.findViewById(R.id.btn_recovery);
        signInTV = rootView.findViewById(R.id.tv_sign_in);
        headerTitleTV = rootView.findViewById(R.id.tv_header_title);
        headerTitleTV.setText("Recovery");
    }

    private void initListeners() {
        recoveryBTN.setOnClickListener(this);
        signInTV.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btn_recovery:
                Toast.makeText(mContext, "Recovery action", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_sign_in:
                ((PreLoginActivity) getActivity()).replaceFragment(SigninFragment.newInstance());
                break;
        }
    }
}
