package com.bittube.wallet.util;

import android.support.annotation.NonNull;

import com.bittube.wallet.XmrWalletApplication;
import com.bittube.wallet.network.Callback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

public class FirebaseUtil {

    static public void saveUserToken(final Callback<String> callback){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseAuth.getInstance().getCurrentUser().getIdToken(false).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    if (task.isSuccessful()) {
                        String token = task.getResult().getToken();
                        XmrWalletApplication.setUserToken(token);
                       callback.sucess(token);
                    } else {
                        callback.error(task.getException().getMessage());
                    }
                }
            });
        } else{
            callback.error("No user login");
        }
    }
}
