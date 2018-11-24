package com.m2049r.xmrwallet.widget.control;


import android.content.res.Resources;

import com.m2049r.xmrwallet.R;
import com.m2049r.xmrwallet.XmrWalletApplication;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailValidator implements Validator{

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public EmailValidator() {
    }

    @Override
    public boolean isValid(String text) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(text);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    @Override
    public String error() {
        Resources resources = XmrWalletApplication.getContext().getResources();
        return resources.getString(R.string.email_validator_error);
    }
}
