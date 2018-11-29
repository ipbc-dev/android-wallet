package com.bittube.wallet.network;

public interface Callback<T> {

     void success(T t);

     void error(String errMsg);
}