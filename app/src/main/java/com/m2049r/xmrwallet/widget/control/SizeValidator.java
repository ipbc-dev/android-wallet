package com.m2049r.xmrwallet.widget.control;

import android.content.res.Resources;

import com.m2049r.xmrwallet.R;
import com.m2049r.xmrwallet.XmrWalletApplication;

public class SizeValidator implements Validator{

    private int mMin, mMax;

    public SizeValidator(int min, int max) {
        mMin = min;
        mMax = max;
    }

    @Override
    public boolean isValid(String text) {
        int size = text.length();
        if (size >= mMin && size <= mMax) {
            return true;
        }
        return false;
    }

    @Override
    public String error() {
        Resources resources = XmrWalletApplication.getContext().getResources();
        return resources.getString(R.string.size_validator_error, mMin, mMax);
    }
}
