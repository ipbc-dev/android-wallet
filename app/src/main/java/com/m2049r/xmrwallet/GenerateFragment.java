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

package com.m2049r.xmrwallet;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.m2049r.xmrwallet.util.RestoreHeight;
import com.m2049r.xmrwallet.widget.InputLayout;
import com.m2049r.xmrwallet.widget.Toolbar;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.model.WalletManager;
import com.m2049r.xmrwallet.util.Helper;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import timber.log.Timber;

public class GenerateFragment extends Fragment {

    static final String TYPE = "type";
    static final String TYPE_NEW = "new";
    static final String TYPE_KEY = "key";
    static final String TYPE_SEED = "seed";
    static final String TYPE_VIEWONLY = "view";

    private InputLayout ilWalletName;
    private InputLayout ilWalletPassword;
    private InputLayout ilWalletAddress;
    private InputLayout ilWalletMnemonic;
    private InputLayout ilWalletViewKey;
    private InputLayout ilWalletSpendKey;
    private InputLayout ilWalletRestoreHeight;
    private Button bGenerate;

    private String type = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle args = getArguments();
        this.type = args.getString(TYPE);

        View view = inflater.inflate(R.layout.fragment_generate, container, false);

        ilWalletName = view.findViewById(R.id.il_wallet_name);
        ilWalletPassword = view.findViewById(R.id.il_wallet_password);
        ilWalletMnemonic = view.findViewById(R.id.il_wallet_mnemonic);
        ilWalletAddress = view.findViewById(R.id.il_wallet_address);
        ilWalletViewKey = view.findViewById(R.id.il_wallet_viewkey);
        ilWalletSpendKey = view.findViewById(R.id.il_wallet_spendkey);
        ilWalletRestoreHeight = view.findViewById(R.id.il_wallet_restoreheight);

        bGenerate = (Button) view.findViewById(R.id.bGenerate);

        ilWalletMnemonic.getEditText().setRawInputType(InputType.TYPE_CLASS_TEXT);
        ilWalletAddress.getEditText().setRawInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        ilWalletViewKey.getEditText().setRawInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        ilWalletSpendKey.getEditText().setRawInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        ilWalletName.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    checkName();
                }
            }
        });
        ilWalletMnemonic.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    checkMnemonic();
                }
            }
        });
        ilWalletAddress.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    checkAddress();
                }
            }
        });
        ilWalletViewKey.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    checkViewKey();
                }
            }
        });
        ilWalletSpendKey.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    checkSpendKey();
                }
            }
        });

        Helper.showKeyboard(getActivity());

        ilWalletName.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
                    if (checkName()) {
                        ilWalletPassword.requestFocus();
                    } // otherwise ignore
                    return true;
                }
                return false;
            }
        });

        if (type.equals(TYPE_NEW)) {
            ilWalletPassword.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
            ilWalletPassword.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                        Helper.hideKeyboard(getActivity());
                        generateWallet();
                        return true;
                    }
                    return false;
                }
            });
        } else if (type.equals(TYPE_SEED)) {
            ilWalletPassword.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
                        ilWalletMnemonic.requestFocus();
                        return true;
                    }
                    return false;
                }
            });
            ilWalletMnemonic.setVisibility(View.VISIBLE);
            ilWalletMnemonic.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
                        if (checkMnemonic()) {
                            ilWalletRestoreHeight.requestFocus();
                        }
                        return true;
                    }
                    return false;
                }
            });
        } else if (type.equals(TYPE_KEY) || type.equals(TYPE_VIEWONLY)) {
            ilWalletPassword.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
                        ilWalletAddress.requestFocus();
                        return true;
                    }
                    return false;
                }
            });
            ilWalletAddress.setVisibility(View.VISIBLE);
            ilWalletAddress.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener()

            {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
                        if (checkAddress()) {
                            ilWalletViewKey.requestFocus();
                        }
                        return true;
                    }
                    return false;
                }
            });
            ilWalletViewKey.setVisibility(View.VISIBLE);
            ilWalletViewKey.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
                        if (checkViewKey()) {
                            if (type.equals(TYPE_KEY)) {
                                ilWalletSpendKey.requestFocus();
                            } else {
                                ilWalletRestoreHeight.requestFocus();
                            }
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
        if (type.equals(TYPE_KEY)) {
            ilWalletSpendKey.setVisibility(View.VISIBLE);
            ilWalletSpendKey.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener()

            {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
                        if (checkSpendKey()) {
                            ilWalletRestoreHeight.requestFocus();
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
        if (!type.equals(TYPE_NEW)) {
            ilWalletRestoreHeight.setVisibility(View.VISIBLE);
            ilWalletRestoreHeight.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                        Helper.hideKeyboard(getActivity());
                        generateWallet();
                        return true;
                    }
                    return false;
                }
            });
        }
        bGenerate.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                Helper.hideKeyboard(getActivity());
                generateWallet();
            }
        });

        ilWalletPassword.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                checkPassword();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        ilWalletName.requestFocus();
        initZxcvbn();

        return view;
    }

    Zxcvbn zxcvbn = new Zxcvbn();

    // initialize zxcvbn engine in background thread
    private void initZxcvbn() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                zxcvbn.measure("");
            }
        }).start();
    }

    private void checkPassword() {
        String password = ilWalletPassword.getText();
        if (!password.isEmpty()) {
            Strength strength = zxcvbn.measure(password);
            int msg;
            double guessesLog10 = strength.getGuessesLog10();
            if (guessesLog10 < 10)
                msg = R.string.password_weak;
            else if (guessesLog10 < 11)
                msg = R.string.password_fair;
            else if (guessesLog10 < 12)
                msg = R.string.password_good;
            else if (guessesLog10 < 13)
                msg = R.string.password_strong;
            else
                msg = R.string.password_very_strong;
            ilWalletPassword.getTil().setError(getResources().getString(msg));
        } else {
            ilWalletPassword.getTil().setError(null);
        }
    }

    private boolean checkName() {
        String name = ilWalletName.getText();
        boolean ok = true;
        if (name.length() == 0) {
            ilWalletName.getTil().setError(getString(R.string.generate_wallet_name));
            ok = false;
        } else if (name.charAt(0) == '.') {
            ilWalletName.getTil().setError(getString(R.string.generate_wallet_dot));
            ok = false;
        } else {
            File walletFile = Helper.getWalletFile(getActivity(), name);
            if (WalletManager.getInstance().walletExists(walletFile)) {
                ilWalletName.getTil().setError(getString(R.string.generate_wallet_exists));
                ok = false;
            }
        }
        if (ok) {
            ilWalletName.getTil().setError(null);
        }
        return ok;
    }

    private boolean checkHeight() {
        long height = !type.equals(TYPE_NEW) ? getHeight() : 0;
        boolean ok = true;
        if (height < 0) {
            ilWalletRestoreHeight.getTil().setError(getString(R.string.generate_restoreheight_error));
            ok = false;
        }
        if (ok) {
            ilWalletRestoreHeight.getTil().setError(null);
        }
        return ok;
    }

    private long getHeight() {
        long height = 0;

        String restoreHeight = ilWalletRestoreHeight.getText();
        if (restoreHeight.isEmpty()) return -1;
        try {
            // is it a date?
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
            parser.setLenient(false);
            parser.parse(restoreHeight);
            height = RestoreHeight.getInstance().getHeight(restoreHeight);
        } catch (ParseException exPE) {
            try {
                // or is it a height?
                height = Long.parseLong(restoreHeight);
            } catch (NumberFormatException exNFE) {
                return -1;
            }
        }
        Timber.d("Using Restore Height = %d", height);
        return height;
    }

    private boolean checkMnemonic() {
        String seed = ilWalletMnemonic.getText();
        boolean ok = (seed.split("\\s").length == 25); // 25 words
        if (!ok) {
            ilWalletMnemonic.getTil().setError(getString(R.string.generate_check_mnemonic));
        } else {
            ilWalletMnemonic.getTil().setError(null);
        }
        return ok;
    }

    private boolean checkAddress() {
        String address = ilWalletAddress.getText();
        boolean ok = Wallet.isAddressValid(address);
        if (!ok) {
            ilWalletAddress.getTil().setError(getString(R.string.generate_check_address));
        } else {
            ilWalletAddress.getTil().setError(null);
        }
        return ok;
    }

    private boolean checkViewKey() {
        String viewKey = ilWalletViewKey.getText();
        boolean ok = (viewKey.length() == 64) && (viewKey.matches("^[0-9a-fA-F]+$"));
        if (!ok) {
            ilWalletViewKey.getTil().setError(getString(R.string.generate_check_key));
        } else {
            ilWalletViewKey.getTil().setError(null);
        }
        return ok;
    }

    private boolean checkSpendKey() {
        String spendKey = ilWalletSpendKey.getText();
        boolean ok = ((spendKey.length() == 0) || ((spendKey.length() == 64) && (spendKey.matches("^[0-9a-fA-F]+$"))));
        if (!ok) {
            ilWalletSpendKey.getTil().setError(getString(R.string.generate_check_key));
        } else {
            ilWalletSpendKey.getTil().setError(null);
        }
        return ok;
    }

    private void generateWallet() {
        if (!checkName()) return;
        if (!checkHeight()) return;

        String name = ilWalletName.getText();
        String password = ilWalletPassword.getText();

        long height = getHeight();
        if (height < 0) height = 0;

        if (type.equals(TYPE_NEW)) {
            bGenerate.setEnabled(false);
            activityCallback.onGenerate(name, password);
        } else if (type.equals(TYPE_SEED)) {
            if (!checkMnemonic()) return;
            String seed = ilWalletMnemonic.getText();
            bGenerate.setEnabled(false);
            activityCallback.onGenerate(name, password, seed, height);
        } else if (type.equals(TYPE_KEY) || type.equals(TYPE_VIEWONLY)) {
            if (checkAddress() && checkViewKey() && checkSpendKey()) {
                bGenerate.setEnabled(false);
                String address = ilWalletAddress.getText();
                String viewKey = ilWalletViewKey.getText();
                String spendKey = "";
                if (type.equals(TYPE_KEY)) {
                    spendKey = ilWalletSpendKey.getText();
                }
                activityCallback.onGenerate(name, password, address, viewKey, spendKey, height);
            }
        }
    }

    public void walletGenerateError() {
        bGenerate.setEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume()");
        activityCallback.setTitle(getString(R.string.generate_title) + " - " + getType());
        activityCallback.setToolbarButton(Toolbar.BUTTON_BACK);

    }

    String getType() {
        switch (type) {
            case TYPE_KEY:
                return getString(R.string.generate_wallet_type_key);
            case TYPE_NEW:
                return getString(R.string.generate_wallet_type_new);
            case TYPE_SEED:
                return getString(R.string.generate_wallet_type_seed);
            case TYPE_VIEWONLY:
                return getString(R.string.generate_wallet_type_view);
            default:
                Timber.e("unknown type %s", type);
                return "?";
        }
    }

    GenerateFragment.Listener activityCallback;

    public interface Listener {
        void onGenerate(String name, String password);

        void onGenerate(String name, String password, String seed, long height);

        void onGenerate(String name, String password, String address, String viewKey, String spendKey, long height);

        void setTitle(String title);

        void setToolbarButton(int type);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof GenerateFragment.Listener) {
            this.activityCallback = (GenerateFragment.Listener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement Listener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        switch (type) {
            case TYPE_KEY:
                inflater.inflate(R.menu.create_wallet_keys, menu);
                break;
            case TYPE_NEW:
                inflater.inflate(R.menu.create_wallet_new, menu);
                break;
            case TYPE_SEED:
                inflater.inflate(R.menu.create_wallet_seed, menu);
                break;
            case TYPE_VIEWONLY:
                inflater.inflate(R.menu.create_wallet_view, menu);
                break;
            default:
        }
        super.onCreateOptionsMenu(menu, inflater);
    }
}
