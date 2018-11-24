package com.m2049r.xmrwallet.fragment;

import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.m2049r.xmrwallet.LoginActivity;
import com.m2049r.xmrwallet.PreLoginActivity;
import com.m2049r.xmrwallet.R;
import com.m2049r.xmrwallet.util.DialogUtil;
import com.m2049r.xmrwallet.widget.InputLayout;
import com.m2049r.xmrwallet.widget.ProgressDialogCV;
import com.m2049r.xmrwallet.widget.control.EmailValidator;
import com.m2049r.xmrwallet.widget.control.EqualValidator;
import com.m2049r.xmrwallet.widget.control.MinSizeValidator;
import com.m2049r.xmrwallet.widget.control.NoEmptyValidator;

public class SignupFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = SignupFragment.class.getName();

    private Context mContext;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private TextView signInTV;
    private Button signUpBTN;
    private TextView headerTitleTV;
    private InputLayout usernameIL, emailIL, passwordIL, confirmPassIL;
    private ProgressDialogCV progressDialog;

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

        auth = FirebaseAuth.getInstance();
        FirebaseFirestore.setLoggingEnabled(true);
        firestore = FirebaseFirestore.getInstance();

        initComponents(rootView);
        initListeners();
        return rootView;
    }

    private void initComponents(View rootView) {
        usernameIL = rootView.findViewById(R.id.il_username);
        emailIL = rootView.findViewById(R.id.il_email);
        passwordIL = rootView.findViewById(R.id.il_password);
        confirmPassIL = rootView.findViewById(R.id.il_confirm_password);
        signUpBTN = rootView.findViewById(R.id.btn_sign_up);
        signInTV = rootView.findViewById(R.id.tv_sign_in);
        headerTitleTV = rootView.findViewById(R.id.tv_header_title);
        headerTitleTV.setText("Sign up");
        progressDialog = new ProgressDialogCV(getActivity(), R.string.loading_message);

        usernameIL.initValidators(new NoEmptyValidator("Username"));
        emailIL.initValidators(new NoEmptyValidator("E-mail"),
                new EmailValidator());
        passwordIL.initValidators(new NoEmptyValidator("Password"),
                new MinSizeValidator("Password", 6),
                new EqualValidator(confirmPassIL));
        confirmPassIL.initValidators(new NoEmptyValidator("Password"),
                new MinSizeValidator("Password", 6),
                new EqualValidator(passwordIL));
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
                signUp();
                break;
            case R.id.tv_sign_in:
                ((PreLoginActivity) getActivity()).replaceFragment(SigninFragment.newInstance());
                break;
        }
    }

    private void signUp() {
        if (!usernameIL.isValid() | !emailIL.isValid() | !passwordIL.isValid() | !confirmPassIL.isValid()) {
            return;
        }

        String email = emailIL.getText();
        String password = passwordIL.getText();

        //create user
        progressDialog.show();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        progressDialog.dismiss();
                        if (!task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Authentication failed." + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intent = new Intent(mContext, LoginActivity.class);
                            startActivity(intent);
                        }
                    }
                });
    }
}
