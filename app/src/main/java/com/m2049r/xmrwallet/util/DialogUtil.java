package com.m2049r.xmrwallet.util;

import android.app.Dialog;
import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.m2049r.xmrwallet.LoginActivity;
import com.m2049r.xmrwallet.R;
import com.m2049r.xmrwallet.widget.InputLayout;

public class DialogUtil {

    public static void showInfoDialog(Context context, String title, String msg,
                                      String textOk, String textCancel, final InfoClickListener infoClickListener) {

        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        final View alertDialogView = LayoutInflater.from(context).inflate(R.layout.dialog, null);
        alertDialog.setView(alertDialogView);

        TextView titleTV = alertDialogView.findViewById(R.id.tv_title);
        TextView descriptionTV = alertDialogView.findViewById(R.id.tv_description);

        if (title != null) {
            titleTV.setVisibility(View.VISIBLE);
            titleTV.setText(title);
        }
        descriptionTV.setText(msg);

        Button cancelBTN = alertDialogView.findViewById(R.id.btn_cancel);
        Button okBTN = alertDialogView.findViewById(R.id.btn_ok);

        cancelBTN.setText(textCancel);
        okBTN.setText(textOk);

        cancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                infoClickListener.cancelClick();
                alertDialog.dismiss();
            }
        });
        okBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                infoClickListener.okClick();
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    public static AlertDialog showInputDialog(Context context, String inputTitle, boolean isPassword,
                                         final InputClickListener inputClickListener) {

        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        final View alertDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_password, null);
        alertDialog.setView(alertDialogView);

        final InputLayout inputIL = alertDialogView.findViewById(R.id.il_dialog_input);
        inputIL.setHint(inputTitle);
        inputIL.setPasswordToggleEnabled(isPassword);

        Button cancelBTN = alertDialogView.findViewById(R.id.btn_cancel);
        Button okBTN = alertDialogView.findViewById(R.id.btn_ok);

        cancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputClickListener.cancelClick();
                alertDialog.dismiss();
            }
        });
        okBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputClickListener.okClick(alertDialog, inputIL.getTil());
            }
        });


        inputIL.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    inputClickListener.okClick(alertDialog, inputIL.getTil());
                    return true;
                }
                return false;
            }
        });

        alertDialog.show();
        return alertDialog;
    }

    public interface InputClickListener {
        void okClick(Dialog dialog, TextInputLayout textInputLayout);
        void cancelClick();
    }

    public interface InfoClickListener {
        void okClick();
        void cancelClick();
    }

}
