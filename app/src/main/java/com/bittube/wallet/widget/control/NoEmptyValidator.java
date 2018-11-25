package com.bittube.wallet.widget.control;

import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.bittube.wallet.R;
import com.bittube.wallet.XmrWalletApplication;

import java.util.Locale;

public class NoEmptyValidator implements Validator{

    private String mName;

    public NoEmptyValidator(String name) {
        mName = name;
    }

    @Override
    public boolean isValid(String text) {
        if (!TextUtils.isEmpty(text)) {
            return true;
        }
        return false;
    }

    @Override
    public String error() {
        Resources resources = XmrWalletApplication.getContext().getResources();
        return resources.getString(R.string.no_empty_error, mName);
    }
}