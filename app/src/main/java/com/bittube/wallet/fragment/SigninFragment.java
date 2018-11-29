package com.bittube.wallet.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.bittube.wallet.network.Callback;
import com.bittube.wallet.util.FirebaseUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.bittube.wallet.LoginActivity;
import com.bittube.wallet.PreLoginActivity;
import com.bittube.wallet.R;
import com.bittube.wallet.widget.InputLayout;
import com.bittube.wallet.widget.ProgressDialogCV;
import com.bittube.wallet.widget.control.EmailValidator;
import com.bittube.wallet.widget.control.MinSizeValidator;
import com.bittube.wallet.widget.control.NoEmptyValidator;
import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.Arrays;

public class SigninFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = SigninFragment.class.getName();
    private static final int RC_SIGN_IN = 9001;

    private Context mContext;
    private FirebaseAuth auth;
    private GoogleSignInClient mGoogleSignInClient;
    private TwitterAuthClient mTwitterAuthClient;
    private CallbackManager mCallbackManager;

    private TextView signUpTV;
    private Button signInBTN;
    private TextView headerTitleTV;
    private TextView recoveryTV;
    private InputLayout emailIL, passwordIL;
    private ProgressDialogCV progressDialog;
    private FrameLayout googleFL, twitterFL, facebookFL;

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

        configureGoogleSDK();
        configureTwitterSDK();

        auth = FirebaseAuth.getInstance();
        initComponents(rootView);
        initListeners();
        return rootView;
    }

    private void configureGoogleSDK() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso);
    }

    private void configureTwitterSDK() {
        // Configure Twitter SDK
        TwitterAuthConfig authConfig =  new TwitterAuthConfig(
                getString(R.string.twitter_consumer_key),
                getString(R.string.twitter_consumer_secret));

        TwitterConfig twitterConfig = new TwitterConfig.Builder(mContext)
                .twitterAuthConfig(authConfig)
                .build();

        Twitter.initialize(twitterConfig);
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
        twitterFL = rootView.findViewById(R.id.fl_twitter);
        facebookFL = rootView.findViewById(R.id.fl_facebook);

        emailIL.initValidators(new NoEmptyValidator("E-mail"),
                new EmailValidator());
        passwordIL.initValidators(new NoEmptyValidator("Password"),
                new MinSizeValidator("Password", 6));
    }

    private void initListeners() {
        signUpTV.setOnClickListener(this);
        signInBTN.setOnClickListener(this);
        recoveryTV.setOnClickListener(this);
        googleFL.setOnClickListener(this);
        twitterFL.setOnClickListener(this);
        facebookFL.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        Intent intent;
        switch (id) {
            case R.id.tv_sign_up:
                ((PreLoginActivity) getActivity()).replaceFragment(SignupFragment.newInstance(), SignupFragment.TAG);
                break;
            case R.id.btn_sign_in:
                signIn();
                break;
            case R.id.tv_recovery:
                ((PreLoginActivity) getActivity()).replaceFragment(RecoveryFragment.newInstance(), RecoveryFragment.TAG);
                break;
            case R.id.fl_google:
                signInGoogle();
                break;
            case R.id.fl_twitter:
                signInTwitter();
                break;
            case R.id.fl_facebook:
                signInFacebook();
                break;
        }
    }

    private void signIn() {
        if (!emailIL.isValid() | !passwordIL.isValid()) {
            return;
        }

        String email = emailIL.getText();
        String password = passwordIL.getText();

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
                            FirebaseUtil.saveUserToken(new Callback<String>() {
                                @Override
                                public void success(String token) {
                                    Intent intent = new Intent(mContext, LoginActivity.class);
                                    startActivity(intent);
                                }

                                @Override
                                public void error(String errMsg) {
                                    Toast.makeText(mContext, "Authentication error: " + errMsg, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
    }

    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signInTwitter() {
        mTwitterAuthClient= new TwitterAuthClient();
        mTwitterAuthClient.authorize(getActivity(), new com.twitter.sdk.android.core.Callback<TwitterSession>() {

            @Override
            public void success(Result<TwitterSession> result) {
                // Success
                Log.d(TAG, "twitterLogin:success" + result);
                handleTwitterSession(result.data);
            }

            @Override
            public void failure(TwitterException e) {
                Log.d(TAG, "twitterLogin:failure" + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void signInFacebook() {
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(
                mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // Handle success
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "FacebookException: onCancel");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.d(TAG, "FacebookException:" + exception.getMessage());
                    }
                }
        );

        LoginManager.getInstance().logInWithReadPermissions(
                this,
                Arrays.asList("email", "public_profile")
        );
    }

    private void handleTwitterSession(TwitterSession session) {
        Log.d(TAG, "handleTwitterSession:" + session);
        progressDialog.show();

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        auth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            FirebaseUtil.saveUserToken(new Callback<String>() {
                                @Override
                                public void success(String token) {
                                    Intent intent = new Intent(mContext, LoginActivity.class);
                                    startActivity(intent);
                                }

                                @Override
                                public void error(String errMsg) {
                                    Toast.makeText(mContext, "Authentication error: " + errMsg, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed." + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        progressDialog.show();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();

                            FirebaseUtil.saveUserToken(new Callback<String>() {
                                @Override
                                public void success(String token) {
                                    Intent intent = new Intent(mContext, LoginActivity.class);
                                    startActivity(intent);
                                }

                                @Override
                                public void error(String errMsg) {
                                    Toast.makeText(mContext, "Authentication error: " + errMsg, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed." + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("onActivityResult", "" + requestCode);

        // Result from Google
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(getActivity(), "Authentication failed." + task.getException(),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Result from Twitter
        if (mTwitterAuthClient != null) {
            mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);
        }

        // Result from Facebook
        if (mCallbackManager != null) {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
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
                            FirebaseUtil.saveUserToken(new Callback<String>() {
                                @Override
                                public void success(String token) {
                                    Intent intent = new Intent(mContext, LoginActivity.class);
                                    startActivity(intent);
                                }

                                @Override
                                public void error(String errMsg) {
                                    Toast.makeText(mContext, "Authentication error: " + errMsg, Toast.LENGTH_SHORT).show();
                                }
                            });
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
