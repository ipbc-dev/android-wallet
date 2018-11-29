package com.bittube.wallet.network.interfaces;


import android.support.annotation.NonNull;

import com.bittube.wallet.network.Callback;
import com.bittube.wallet.network.models.OnlineWallet;

import java.util.List;


public interface FirebaseCloudFunctions {

    void getUserWallets(@NonNull final String userToken, final Callback<List<OnlineWallet>> callback);

}

