package com.m2049r.xmrwallet.widget.control;

public interface Validator {

    boolean isValid(String text);

    String error();
}