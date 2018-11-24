package com.m2049r.xmrwallet.widget.control;

import android.content.res.Resources;
import android.text.TextUtils;

import com.m2049r.xmrwallet.R;
import com.m2049r.xmrwallet.XmrWalletApplication;
import com.m2049r.xmrwallet.widget.InputLayout;

public class EqualValidator implements Validator{

    private InputLayout mInputLayout;

    public EqualValidator(InputLayout inputLayout) {
        mInputLayout = inputLayout;
    }

    @Override
    public boolean isValid(String text) {
        if (TextUtils.equals(text, mInputLayout.getText())) {
            return true;
        }
        return false;
    }

    @Override
    public String error() {
        Resources resources = XmrWalletApplication.getContext().getResources();
        return resources.getString(R.string.compare_validator_error);
    }
}
