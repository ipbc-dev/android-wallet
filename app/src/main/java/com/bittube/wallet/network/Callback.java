package com.bittube.wallet.network;

public interface Callback<T> {

     void sucess(T t);

     void error(String errMsg);
}