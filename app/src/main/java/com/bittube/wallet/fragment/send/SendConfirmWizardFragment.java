/*
 * Copyright (c) 2017 m2049r
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bittube.wallet.fragment.send;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.bittube.wallet.LoginActivity;
import com.bittube.wallet.R;
import com.bittube.wallet.data.TxData;
import com.bittube.wallet.model.PendingTransaction;
import com.bittube.wallet.model.Wallet;
import com.bittube.wallet.util.DialogUtil;
import com.bittube.wallet.util.Helper;
import com.bittube.wallet.util.UserNotes;

import timber.log.Timber;

public class SendConfirmWizardFragment extends SendWizardFragment implements SendConfirm {

    public static SendConfirmWizardFragment newInstance(Listener listener) {
        SendConfirmWizardFragment instance = new SendConfirmWizardFragment();
        instance.setSendListener(listener);
        return instance;
    }

    Listener sendListener;

    public SendConfirmWizardFragment setSendListener(Listener listener) {
        this.sendListener = listener;
        return this;
    }

    interface Listener {
        SendFragment.Listener getActivityCallback();

        TxData getTxData();

        void commitTransaction();

        void disposeTransaction();

        SendFragment.Mode getMode();
    }

    private TextView tvTxAddress;
    private TextView tvTxPaymentId;
    private TextView tvTxNotes;
    private TextView tvTxAmount;
    private TextView tvTxFee;
    private TextView tvTxTotal;
    private View llProgress;
    private View bSend;
    private View llConfirmSend;
    private View pbProgressSend;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Timber.d("onCreateView() %s", (String.valueOf(savedInstanceState)));

        View view = inflater.inflate(
                R.layout.fragment_send_confirm, container, false);

        tvTxAddress = (TextView) view.findViewById(R.id.tvTxAddress);
        tvTxPaymentId = (TextView) view.findViewById(R.id.tvTxPaymentId);
        tvTxNotes = (TextView) view.findViewById(R.id.tvTxNotes);
        tvTxAmount = ((TextView) view.findViewById(R.id.tvTxAmount));
        tvTxFee = (TextView) view.findViewById(R.id.tvTxFee);
        tvTxTotal = (TextView) view.findViewById(R.id.tvTxTotal);

        llProgress = view.findViewById(R.id.llProgress);
        pbProgressSend = view.findViewById(R.id.pbProgressSend);
        llConfirmSend = view.findViewById(R.id.llConfirmSend);

        bSend = view.findViewById(R.id.bSend);
        bSend.setEnabled(false);
        bSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Timber.d("bSend.setOnClickListener");
                bSend.setEnabled(false);
                preSend();
            }
        });
        return view;
    }

    boolean inProgress = false;

    public void hideProgress() {
        llProgress.setVisibility(View.GONE);
        inProgress = false;
    }

    public void showProgress() {
        llProgress.setVisibility(View.VISIBLE);
        inProgress = true;
    }

    PendingTransaction pendingTransaction = null;

    @Override
    // callback from wallet when PendingTransaction created
    public void transactionCreated(String txTag, PendingTransaction pendingTransaction) {
        // ignore txTag - the app flow ensures this is the correct tx
        // TODO: use the txTag
        hideProgress();
        if (isResumed) {
            this.pendingTransaction = pendingTransaction;
            refreshTransactionDetails();
        } else {
            sendListener.disposeTransaction();
        }
    }

    void send() {
        sendListener.commitTransaction();
        pbProgressSend.setVisibility(View.VISIBLE);
    }

    @Override
    public void sendFailed() {
        pbProgressSend.setVisibility(View.GONE);
    }

    @Override
    public void createTransactionFailed(String errorText) {
        hideProgress();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true).
                setTitle(getString(R.string.send_create_tx_error_title)).
                setMessage(errorText).
                create().
                show();
    }

    @Override
    public boolean onValidateFields() {
        return true;
    }

    private boolean isResumed = false;

    @Override
    public void onPauseFragment() {
        isResumed = false;
        pendingTransaction = null;
        sendListener.disposeTransaction();
        refreshTransactionDetails();
        super.onPauseFragment();
    }

    @Override
    public void onResumeFragment() {
        super.onResumeFragment();
        Timber.d("onResumeFragment()");
        Helper.hideKeyboard(getActivity());
        isResumed = true;

        final TxData txData = sendListener.getTxData();
        tvTxAddress.setText(txData.getDestinationAddress());
        String paymentId = txData.getPaymentId();
        if ((paymentId != null) && (!paymentId.isEmpty())) {
            tvTxPaymentId.setText(txData.getPaymentId());
        } else {
            tvTxPaymentId.setText("-");
        }
        UserNotes notes = sendListener.getTxData().getUserNotes();
        if ((notes != null) && (!notes.note.isEmpty())) {
            tvTxNotes.setText(notes.note);
        } else {
            tvTxNotes.setText("-");
        }
        refreshTransactionDetails();
        if ((pendingTransaction == null) && (!inProgress)) {
            showProgress();
            prepareSend(txData);
        }
    }

    void refreshTransactionDetails() {
        Timber.d("refreshTransactionDetails()");
        if (pendingTransaction != null) {
            llConfirmSend.setVisibility(View.VISIBLE);
            bSend.setEnabled(true);
            tvTxAmount.setText(Wallet.getDisplayAmount(pendingTransaction.getAmount()));
            tvTxFee.setText(Wallet.getDisplayAmount(pendingTransaction.getFee()));
            //tvTxDust.setText(Wallet.getDisplayAmount(pendingTransaction.getDust()));
            tvTxTotal.setText(Wallet.getDisplayAmount(
                    pendingTransaction.getFee() + pendingTransaction.getAmount()));
        } else {
            llConfirmSend.setVisibility(View.GONE);
            bSend.setEnabled(false);
        }
    }

    public void preSend() {
        final Activity activity = getActivity();
        Dialog passwordDialog = DialogUtil.showInputDialog(activity, getString(R.string.prompt_send_password),
                true, new DialogUtil.InputClickListener() {
                    @Override
                    public void okClick(Dialog dialog, TextInputLayout textInputLayout) {
                        String pass = textInputLayout.getEditText().getText().toString();
                        if (getActivityCallback().verifyWalletPassword(pass)) {
                            dialog.dismiss();
                            Helper.hideKeyboardAlways(activity);
                            send();
                        } else {
                            textInputLayout.setError(getString(R.string.bad_password));
                        }
                    }

                    @Override
                    public void cancelClick() {
                        Helper.hideKeyboardAlways(activity);
                        bSend.setEnabled(true);
                    }
                });

        Helper.showKeyboard(passwordDialog);

    }

    // creates a pending transaction and calls us back with transactionCreated()
    // or createTransactionFailed()
    void prepareSend(TxData txData) {
        getActivityCallback().onPrepareSend(null, txData);
    }

    SendFragment.Listener getActivityCallback() {
        return sendListener.getActivityCallback();
    }
}
