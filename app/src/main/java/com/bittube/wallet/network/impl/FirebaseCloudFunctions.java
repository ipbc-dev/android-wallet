package com.bittube.wallet.network.impl;

import android.support.annotation.NonNull;
import android.util.Log;

import com.bittube.wallet.network.Callback;
import com.bittube.wallet.network.models.OnlineWallet;
import com.bittube.wallet.util.OkHttpClientSingleton;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FirebaseCloudFunctions implements com.bittube.wallet.network.interfaces.FirebaseCloudFunctions {

    OkHttpClient client;

    private String APP_BASE_URL = "https://us-central1-bittube-airtime-extension.cloudfunctions.net/app";
    private String GET_USER_WALLETS_ENDPOINT = APP_BASE_URL + "/getUserWallets";

    public FirebaseCloudFunctions() {
        this.client = OkHttpClientSingleton.getOkHttpClient();
    }


    @Override
    public void getUserWallets(@NonNull String userToken, final Callback<List<OnlineWallet>> callback) {

        final HttpUrl url = HttpUrl.parse(GET_USER_WALLETS_ENDPOINT).newBuilder()
                .build();
        final Request httpRequest = createHttpRequest(url, userToken);


        client.newCall(httpRequest).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(final Call call, final IOException ex) {
                callback.error("Error: " + ex.getMessage());
            }

            @Override
            public void onResponse(final Call call, final Response response) {
                if (response.isSuccessful()) {

                    String responseData = null;
                    try {
                        responseData = response.body().string();
                        Log.d("DYMTEK", "getUserWallets response: " + responseData);

                        JSONObject json = new JSONObject(responseData);
                        String message = json.getString("message");

                        if (message != null && message.equals("success")) {
                            JSONObject data = json.getJSONObject("data");
                            Iterator<String> keys = data.keys();

                            List<OnlineWallet> onlineWallets = new ArrayList<>();

                            while (keys.hasNext()) {
                                String name = keys.next();
                                onlineWallets.add(new OnlineWallet(name, data.getJSONObject(name)));
                            }

                            callback.success(onlineWallets);

                        } else {
                            callback.error("Error " + response.code() + ": " + message);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.error("Error " + e.getMessage());
                    }


                } else {
                    callback.error("Error " + response.code() + ": " + response.message());
                }
            }
        });

    }


    private Request createHttpRequest(final HttpUrl url, String token) {


        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url).get();
        if (token != null && !token.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }

        return requestBuilder.build();
    }
}
