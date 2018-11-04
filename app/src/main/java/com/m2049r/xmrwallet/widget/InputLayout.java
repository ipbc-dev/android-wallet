package com.m2049r.xmrwallet.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.m2049r.xmrwallet.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputLayout extends FrameLayout {

    private Context mContext;
    private TextInputLayout til;
    private EditText et;

    boolean passwordToggleEnabled;
    boolean counterEnabled;
    private int type;
    private int maxCounter = 10;
    private int counter;
    private String hint = "";

    public InputLayout(Context context) {
        super(context);
        mContext = context;
        initComponents();
    }

    public InputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(attrs);
    }

    public InputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        inflate(mContext, R.layout.layout_input, this);
        til = findViewById(R.id.til);
        et = findViewById(R.id.et);


        TypedArray a = mContext.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.InputLayout,
                0, 0);

        try {
            passwordToggleEnabled = a.getBoolean(R.styleable.InputLayout_passwordToggleEnabled, false);
            counterEnabled = a.getBoolean(R.styleable.InputLayout_counterEnabled, false);
            type = a.getInteger(R.styleable.InputLayout_type, 0);
            hint = a.getString(R.styleable.InputLayout_hint);
            maxCounter = a.getInteger(R.styleable.InputLayout_maxCounter, 10);
        } finally {
            a.recycle();
        }

        initComponents();
    }

    private void initComponents() {
        counter = 0;
        til.setHint(hint);
        et.setText(" ");
        et.setSelection(et.getText().length());
        til.setErrorTextAppearance(R.style.InputError);

        et.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                onFocusTransformation(b);
            }
        });

        setInputType();

        onFocusTransformation(false);

        if (passwordToggleEnabled) {
            til.setPasswordVisibilityToggleEnabled(true);
            et.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            et.setTransformationMethod(new PasswordTransformationMethod());
        }

        if (counterEnabled) {
            til.setHint(til.getHint() + " (" + counter + "/" + maxCounter + ")");
            et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    til.setError(null);
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    /*String c = String.valueOf(charSequence);
                    if (!c.startsWith(" ")) {
                        et.setText(String.format(" %s", c));
                        et.setSelection(et.getText().length());
                    }*/
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    int cont = et.getText().length();
                    if (cont == 1 && " ".equals(et.getText().toString())) {
                        counter = 0;
                    } else {
                        counter = et.getText().length();
                    }
                    Pattern p = Pattern.compile("(.*)\\((.*?)\\)");
                    Matcher m = p.matcher(til.getHint());
                    if (m.find()) {
                        String text = m.group(1).trim();
                        til.setHint(text + " (" + counter + "/" + maxCounter + ")");
                    } else {
                        til.setHint(et.getText() + " (" + counter + "/" + maxCounter + ")");
                    }

                    if (counter > maxCounter) {
                        til.setErrorEnabled(true);
                        til.setError(" ");
                    } else {
                        til.setError(null);
                    }
                }
            });
        }

        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                til.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setInputType() {
        switch (type) {
            case 0:
                et.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            case 1:
                et.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                et.setTransformationMethod(new PasswordTransformationMethod());
                break;
            case 2:
                et.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                break;
            case 3:
                et.setInputType(InputType.TYPE_CLASS_DATETIME);
                break;
            case 4:
                et.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;

        }
    }

    public void onFocusTransformation(boolean b) {
        if (b) {
            setInputType();
            if (" ".equalsIgnoreCase(et.getText().toString())) {
                et.setText("");
            }
        } else {
            if (et.getText().toString().isEmpty()) {
                et.setText(" ");
            }
            if (" ".equalsIgnoreCase(et.getText().toString())) {
                et.setInputType(InputType.TYPE_CLASS_TEXT);
                et.setTransformationMethod(null);
            } else {
                if (type == 1) {
                    et.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    et.setTransformationMethod(new PasswordTransformationMethod());
                }
            }

        }
    }

    private void updateIsPassword() {
        if (passwordToggleEnabled) {
            til.setPasswordVisibilityToggleEnabled(true);
            et.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            et.setTransformationMethod(new PasswordTransformationMethod());
        } else {
            til.setPasswordVisibilityToggleEnabled(false);
            et.setInputType(InputType.TYPE_CLASS_TEXT);
            et.setTransformationMethod(null);
        }
    }

    public TextInputLayout getTil() {
        return til;
    }

    public EditText getEditText() {
        return et;
    }

    public String getText() {
        return et.getText().toString().trim();
    }

    public void setPasswordToggleEnabled(boolean enabled) {
        passwordToggleEnabled = enabled;
        updateIsPassword();
    }

    public void setHint(String hint) {
        this.hint = hint;
        if (til != null)
            til.setHint(hint);
    }

}
