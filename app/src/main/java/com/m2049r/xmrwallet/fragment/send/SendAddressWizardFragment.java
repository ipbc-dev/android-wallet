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

package com.m2049r.xmrwallet.fragment.send;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.m2049r.xmrwallet.R;
import com.m2049r.xmrwallet.data.BarcodeData;
import com.m2049r.xmrwallet.data.TxData;
import com.m2049r.xmrwallet.data.TxDataBtc;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.util.BitcoinAddressValidator;
import com.m2049r.xmrwallet.util.Helper;
import com.m2049r.xmrwallet.widget.InputLayout;

import timber.log.Timber;

public class SendAddressWizardFragment extends SendWizardFragment {

    static final int INTEGRATED_ADDRESS_LENGTH = 109;

    public static SendAddressWizardFragment newInstance(Listener listener) {
        SendAddressWizardFragment instance = new SendAddressWizardFragment();
        instance.setSendListener(listener);
        return instance;
    }

    Listener sendListener;

    public SendAddressWizardFragment setSendListener(Listener listener) {
        this.sendListener = listener;
        return this;
    }

    public interface Listener {
        void setBarcodeData(BarcodeData data);

        void setMode(SendFragment.Mode mode);

        TxData getTxData();
    }

    private EditText etDummy;
    private InputLayout etAddress;
    private InputLayout etPaymentId;
    private View bPaymentId;
    private View cvScan;
    private View tvPaymentIdIntegrated;
    private View rlPaymentId;
    private TextView tvXmrTo;
    private View llXmrTo;

    OnScanListener onScanListener;

    public interface OnScanListener {
        void onScan();

        BarcodeData popScannedData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Timber.d("onCreateView() %s", (String.valueOf(savedInstanceState)));

        View view = inflater.inflate(R.layout.fragment_send_address, container, false);

        tvPaymentIdIntegrated = view.findViewById(R.id.tvPaymentIdIntegrated);
        rlPaymentId = view.findViewById(R.id.rlPaymentId);
        llXmrTo = view.findViewById(R.id.llXmrTo);
        tvXmrTo = (TextView) view.findViewById(R.id.tvXmrTo);
        tvXmrTo.setText(Html.fromHtml(getString(R.string.info_xmrto)));

        etAddress = (InputLayout) view.findViewById(R.id.etAddress);
        etAddress.getEditText().setRawInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        etAddress.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
                    if (checkAddress()) {
                        if (rlPaymentId.getVisibility() == View.VISIBLE) {
                            etPaymentId.requestFocus();
                        } else {
                            etDummy.requestFocus();
                            Helper.hideKeyboard(getActivity());
                        }
                    }
                    return true;
                }
                return false;
            }
        });
        etAddress.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                etAddress.getTil().setError(null);
                if (isIntegratedAddress()) {
                    Timber.d("isIntegratedAddress");
                    etPaymentId.getEditText().getText().clear();
                    rlPaymentId.setVisibility(View.GONE);
                    tvPaymentIdIntegrated.setVisibility(View.VISIBLE);
                    llXmrTo.setVisibility(View.GONE);
                    sendListener.setMode(SendFragment.Mode.XMR);
                } else if (isBitcoinAddress()) {
                    Timber.d("isBitcoinAddress");
                    etPaymentId.getEditText().getText().clear();
                    rlPaymentId.setVisibility(View.GONE);
                    tvPaymentIdIntegrated.setVisibility(View.GONE);
                    llXmrTo.setVisibility(View.VISIBLE);
                    sendListener.setMode(SendFragment.Mode.BTC);
                } else {
                    Timber.d("isStandardAddress");
                    rlPaymentId.setVisibility(View.VISIBLE);
                    tvPaymentIdIntegrated.setVisibility(View.GONE);
                    llXmrTo.setVisibility(View.GONE);
                    sendListener.setMode(SendFragment.Mode.XMR);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });


        etPaymentId = (InputLayout) view.findViewById(R.id.etPaymentId);
        etPaymentId.getEditText().setRawInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        etPaymentId.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    if (checkPaymentId()) {
                        etDummy.requestFocus();
                        Helper.hideKeyboard(getActivity());
                    }
                    return true;
                }
                return false;
            }
        });
        etPaymentId.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                etPaymentId.getTil().setError(null);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        bPaymentId = view.findViewById(R.id.bPaymentId);
        bPaymentId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etPaymentId.getEditText().setText((Wallet.generatePaymentId()));
            }
        });

        cvScan = view.findViewById(R.id.bScan);
        cvScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onScanListener.onScan();
            }
        });


        etDummy = (EditText) view.findViewById(R.id.etDummy);
        etDummy.setRawInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        etDummy.requestFocus();
        Helper.hideKeyboard(getActivity());

        return view;
    }

    private boolean checkAddressNoError() {
        String address = etAddress.getText();
        return Wallet.isAddressValid(address)
                || BitcoinAddressValidator.validate(address);
    }

    private boolean checkAddress() {
        boolean ok = checkAddressNoError();
        if (!ok) {
            etAddress.getTil().setError(getString(R.string.send_address_invalid));
        } else {
            etAddress.getTil().setError(null);
        }
        return ok;
    }

    private boolean isIntegratedAddress() {
        String address = etAddress.getText();
        return (address.length() == INTEGRATED_ADDRESS_LENGTH)
                && Wallet.isAddressValid(address);
    }

    private boolean isBitcoinAddress() {
        String address = etAddress.getText();
        if ((address.length() >= 27) && (address.length() <= 34))
            return BitcoinAddressValidator.validate(address);
        else
            return false;
    }

    private boolean checkPaymentId() {
        String paymentId = etPaymentId.getText();
        boolean ok = paymentId.isEmpty() || Wallet.isPaymentIdValid(paymentId);
        if (!ok) {
            etPaymentId.getTil().setError(getString(R.string.receive_paymentid_invalid));
        } else {
            if (!paymentId.isEmpty() && isIntegratedAddress()) {
                ok = false;
                etPaymentId.getTil().setError(getString(R.string.receive_integrated_paymentid_invalid));
            } else {
                etPaymentId.getTil().setError(null);
            }
        }
        return ok;
    }

    @Override
    public boolean onValidateFields() {
        boolean ok = true;
        if (!checkAddressNoError()) {
            etAddress.startAnimation(Helper.getShakeAnimation(getContext()));
            ok = false;
        }
        if (!checkPaymentId()) {
            etPaymentId.startAnimation(Helper.getShakeAnimation(getContext()));
            ok = false;
        }
        if (!ok) return false;
        if (sendListener != null) {
            TxData txData = sendListener.getTxData();
            if (isBitcoinAddress()) {
                ((TxDataBtc) txData).setBtcAddress(etAddress.getText());
                txData.setDestinationAddress(null);
                txData.setPaymentId("");
            } else {
                txData.setDestinationAddress(etAddress.getText());
                txData.setPaymentId(etPaymentId.getText());
            }
        }
        return true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnScanListener) {
            onScanListener = (OnScanListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement ScanListener");
        }
    }

    // QR Scan Stuff

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume");
        BarcodeData data = onScanListener.popScannedData();
        sendListener.setBarcodeData(data);
        if (data != null) {
            Timber.d("GOT DATA");
            String scannedAddress = data.address;
            if (scannedAddress != null) {
                etAddress.getEditText().setText(scannedAddress);
                checkAddress();
            } else {
                etAddress.getEditText().getText().clear();
                etAddress.getTil().setError(null);
            }
            String scannedPaymenId = data.paymentId;
            if (scannedPaymenId != null) {
                etPaymentId.getEditText().setText(scannedPaymenId);
                checkPaymentId();
            } else {
                etPaymentId.getEditText().getText().clear();
                etPaymentId.getTil().setError(null);
            }
        }
    }

    @Override
    public void onResumeFragment() {
        super.onResumeFragment();
        Timber.d("onResumeFragment()");
        Helper.hideKeyboard(getActivity());
        etDummy.requestFocus();
    }
}
