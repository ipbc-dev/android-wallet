package com.m2049r.xmrwallet.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.m2049r.xmrwallet.PreLoginActivity;
import com.m2049r.xmrwallet.R;
import com.m2049r.xmrwallet.widget.InputLayout;
import com.m2049r.xmrwallet.widget.ProgressDialogCV;

public class RecoveryFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = RecoveryFragment.class.getName();

    private Context mContext;
    private FirebaseAuth auth;

    private TextView signInTV;
    private Button recoveryBTN;
    private TextView headerTitleTV;
    private InputLayout emailIL;
    private ProgressDialogCV progressDialog;

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
        auth = FirebaseAuth.getInstance();
        initComponents(rootView);
        initListeners();
        return rootView;
    }

    private void initComponents(View rootView) {
        emailIL = rootView.findViewById(R.id.il_email);
        recoveryBTN = rootView.findViewById(R.id.btn_recovery);
        signInTV = rootView.findViewById(R.id.tv_sign_in);
        headerTitleTV = rootView.findViewById(R.id.tv_header_title);
        headerTitleTV.setText("Recovery");
        progressDialog = new ProgressDialogCV(getActivity(), R.string.loading_message);
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
                recovery();
                break;
            case R.id.tv_sign_in:
                ((PreLoginActivity) getActivity()).replaceFragment(SigninFragment.newInstance());
                break;
        }
    }

    private void recovery() {
        String email = emailIL.getText();

        if (TextUtils.isEmpty(email)) {
            emailIL.getTil().setError("Enter email address!");
            return;
        }

        progressDialog.show();
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                        }
                        ((PreLoginActivity) getActivity()).replaceFragment(SigninFragment.newInstance());
                        progressDialog.dismiss();
                    }
                });
    }
}
