package com.m2049r.xmrwallet.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.m2049r.xmrwallet.LoginActivity;
import com.m2049r.xmrwallet.PreLoginActivity;
import com.m2049r.xmrwallet.R;
import com.m2049r.xmrwallet.widget.InputLayout;
import com.m2049r.xmrwallet.widget.ProgressDialogCV;

public class SigninFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = SigninFragment.class.getName();
    private static final int RC_SIGN_IN = 9001;

    private Context mContext;
    private FirebaseAuth auth;
    private GoogleSignInClient mGoogleSignInClient;

    private TextView signUpTV;
    private Button signInBTN;
    private TextView headerTitleTV;
    private TextView recoveryTV;
    private InputLayout emailIL, passwordIL;
    private ProgressDialogCV progressDialog;
    private FrameLayout googleFL;

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

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso);

        auth = FirebaseAuth.getInstance();
        initComponents(rootView);
        initListeners();
        return rootView;
    }

    private void initComponents(View rootView) {
        emailIL = rootView.findViewById(R.id.il_email);
        passwordIL = rootView.findViewById(R.id.il_password);
        signUpTV = rootView.findViewById(R.id.tv_sign_up);
        signInBTN = rootView.findViewById(R.id.btn_sign_in);
        headerTitleTV = rootView.findViewById(R.id.tv_header_title);
        headerTitleTV.setText("Sign in");
        recoveryTV = rootView.findViewById(R.id.tv_recovery);
        progressDialog = new ProgressDialogCV(getActivity(), R.string.loading_message);
        googleFL = rootView.findViewById(R.id.fl_google);
    }

    private void initListeners() {
        signUpTV.setOnClickListener(this);
        signInBTN.setOnClickListener(this);
        recoveryTV.setOnClickListener(this);
        googleFL.setOnClickListener(this);
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
                signIn();
                break;
            case R.id.tv_recovery:
                ((PreLoginActivity) getActivity()).replaceFragment(RecoveryFragment.newInstance());
                break;
            case R.id.fl_google:
                signInGoogle();
                break;
        }
    }

    private void signIn() {

        String email = emailIL.getText();
        String password = passwordIL.getText();

        if (TextUtils.isEmpty(email)) {
            emailIL.getTil().setError("Enter email address!");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordIL.getTil().setError("Enter password!");
            return;
        }

        if (password.length() < 6) {
            passwordIL.getTil().setError("Password too short, enter minimum 6 characters!");
            return;
        }

        //authenticate user
        progressDialog.show();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        progressDialog.dismiss();
                        if (!task.isSuccessful()) {
                            // there was an error
                            Toast.makeText(getActivity(), "Authentication failed." + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intent = new Intent(mContext, LoginActivity.class);
                            startActivity(intent);
                        }
                    }
                });
    }


    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                //updateUI(null);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        progressDialog.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            //updateUI(user);
                            Intent intent = new Intent(mContext, LoginActivity.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed." + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                            //Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
    }
}
