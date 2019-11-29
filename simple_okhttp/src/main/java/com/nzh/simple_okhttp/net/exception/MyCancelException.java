package com.nzh.simple_okhttp.net.exception;


public class MyCancelException extends Exception {

    String msg;

    public MyCancelException(String msg) {
        this.msg = msg;
    }

}
