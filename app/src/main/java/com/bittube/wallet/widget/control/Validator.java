package com.bittube.wallet.widget.control;

public interface Validator {

    boolean isValid(String text);

    String error();
}