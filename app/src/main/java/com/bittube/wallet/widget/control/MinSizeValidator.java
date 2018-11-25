package com.bittube.wallet.widget.control;

import android.content.res.Resources;

import com.bittube.wallet.R;
import com.bittube.wallet.XmrWalletApplication;

public class MinSizeValidator implements Validator{

    private String mName;
    private int mMin;

    public MinSizeValidator(String name, int min) {
        mName = name;
        mMin = min;
    }

    @Override
    public boolean isValid(String text) {
        int size = text.length();
        if (size >= mMin) {
            return true;
        }
        return false;
    }

    @Override
    public String error() {
        Resources resources = XmrWalletApplication.getContext().getResources();
        return resources.getString(R.string.size_validator_error, mName);
    }
}
