package com.bittube.wallet.service;

import android.os.AsyncTask;

import com.bittube.wallet.model.Wallet;
import com.bittube.wallet.model.WalletManager;
import com.bittube.wallet.network.Callback;
import com.bittube.wallet.network.models.OnlineWallet;
import com.bittube.wallet.util.MoneroThreadPoolExecutor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class WalletRecovery {

    // RECOVERY MODES
    private static final int NEW_WALLET = 000;
    private static final int RECOVER_BY_SEED = 111;
    private static final int RECOVER_BY_KEYS = 222;


    // Multiple wallets

    public void recoverWalletsBySeed(List<OnlineWallet> onlineWallets, File storage, Callback<Boolean> callback) {


        WalletRetriever walletRetriever = buildWalletRetriever(RECOVER_BY_SEED);


        new AsyncRecoverWallets(onlineWallets, walletRetriever, storage, callback)
                .executeOnExecutor(MoneroThreadPoolExecutor.MONERO_THREAD_POOL_EXECUTOR);


    }

    public void recoverWalletsByKeys(List<OnlineWallet> onlineWallets, File storage, Callback<Boolean> callback) {

        WalletRetriever walletRetriever = buildWalletRetriever(RECOVER_BY_KEYS);


        new AsyncRecoverWallets(onlineWallets, walletRetriever, storage, callback)
                .executeOnExecutor(MoneroThreadPoolExecutor.MONERO_THREAD_POOL_EXECUTOR);
    }


    // Single wallet

    public void newWallet(String name, String password, File storage, Callback<Boolean> callback) {
        List<OnlineWallet> onlineWallets = new ArrayList<>();
        onlineWallets.add(new OnlineWallet(name, password));

        WalletRetriever walletRetriever = buildWalletRetriever(NEW_WALLET);

        new AsyncRecoverWallets(onlineWallets, walletRetriever, storage, callback)
                .executeOnExecutor(MoneroThreadPoolExecutor.MONERO_THREAD_POOL_EXECUTOR);
    }

    public void recoverSingleWalletBySeed(OnlineWallet onlineWallet, File storage, Callback<Boolean> callback) {
        List<OnlineWallet> onlineWallets = new ArrayList<>();
        onlineWallets.add(onlineWallet);

        WalletRetriever walletRetriever = buildWalletRetriever(RECOVER_BY_SEED);

        new AsyncRecoverWallets(onlineWallets, walletRetriever, storage, callback)
                .executeOnExecutor(MoneroThreadPoolExecutor.MONERO_THREAD_POOL_EXECUTOR);
    }

    public void recoverSingleWalletByKeys(OnlineWallet onlineWallet, File storage, Callback<Boolean> callback) {
        List<OnlineWallet> onlineWallets = new ArrayList<>();
        onlineWallets.add(onlineWallet);

        WalletRetriever walletRetriever = buildWalletRetriever(RECOVER_BY_KEYS);

        new AsyncRecoverWallets(onlineWallets, walletRetriever, storage, callback)
                .executeOnExecutor(MoneroThreadPoolExecutor.MONERO_THREAD_POOL_EXECUTOR);
    }


    private class AsyncRecoverWallets extends AsyncTask<Void, Void, Boolean> {
        final List<OnlineWallet> onlineWallets;
        final WalletRetriever walletRetriever;
        final File storage;
        final Callback<Boolean> callback;


        File newWalletFile;

        AsyncRecoverWallets(List<OnlineWallet> onlineWallets,
                            final WalletRetriever walletRetriever, File storage, Callback<Boolean> callback) {
            super();
            this.onlineWallets = onlineWallets;
            this.walletRetriever = walletRetriever;
            this.storage = storage;
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (!storage.isDirectory()) {
                Timber.e("Wallet dir " + storage.getAbsolutePath() + "is not a directory");
                return false;
            }

            for (OnlineWallet onlineWallet : onlineWallets) {
                String walletName = onlineWallet.getName();
                File cacheFile = new File(storage, walletName);
                File keysFile = new File(storage, walletName + ".keys");
                File addressFile = new File(storage, walletName + ".address.txt");

                if (cacheFile.exists() || keysFile.exists() || addressFile.exists()) {
                    Timber.e("Some wallet files already exist for %s", cacheFile.getAbsolutePath());
                    return false;
                }

                newWalletFile = new File(storage, walletName);
                String password = onlineWallet.getPassword() != null ? onlineWallet.getPassword() : "";
                boolean success = walletRetriever.recoverWallet(newWalletFile, password, onlineWallet);

                if (!success) {
                    Timber.e("Could not create new wallet in %s", newWalletFile.getAbsolutePath());
                } else {
                    Timber.d("New Wallet %s", storage.getAbsolutePath());
                    cacheFile.delete(); // when recovering wallets, the cache seems corrupt
                }

            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                callback.success(true);
            } else {
                callback.error("Error recovering online wallets");
            }
        }
    }


    private WalletRetriever buildWalletRetriever(final int recoveryMode) {

        return new WalletRetriever() {
            @Override
            public boolean recoverWallet(File aFile, String password, OnlineWallet onlineWallet) {
                Wallet newWallet;

                switch (recoveryMode) {
                    case NEW_WALLET:
                        newWallet = WalletManager.getInstance()
                                .createWallet(aFile, password, WalletManager.MNEMONIC_LANGUAGE_ENGLISH);
                        break;
                    case RECOVER_BY_KEYS:
                        newWallet = WalletManager.getInstance()
                                .createWalletWithKeys(aFile, password, WalletManager.MNEMONIC_LANGUAGE_ENGLISH, onlineWallet.getCreation_date(),
                                        onlineWallet.getAddress(), onlineWallet.getViewKey(), onlineWallet.getSpendKey());
                        break;
                    case RECOVER_BY_SEED:
                        newWallet = WalletManager.getInstance().
                                recoveryWallet(aFile, password, onlineWallet.getSeed(), onlineWallet.getCreation_date());
                        break;
                    default:
                        Timber.e("Invalid wallet generation mode");
                        return false;
                }


                boolean success = (newWallet.getStatus() == Wallet.Status.Status_Ok);


                if (!success) {
                    Timber.e(newWallet.getErrorString());
                }
                newWallet.close();
                return success;
            }

        };

    }

    interface WalletRetriever {
        boolean recoverWallet(File aFile, String password, OnlineWallet onlineWallet);

    }


}
