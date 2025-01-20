package com.example.emailservice.common;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ResponseBean<T> {

    private int rCode;
    private HttpStatus rStatus;
    private String displayMessage;
    private String rMsg;
    private T rData;
    private Pagination pagination;


    public ResponseBean() {
        rStatus = HttpStatus.OK;
        rCode = HttpStatus.OK.value();
        rMsg = "";
    }

    public ResponseBean(HttpStatus responseCode, String message) {
        this.rStatus = responseCode;
        this.rCode = responseCode.value();
        this.rMsg = message;
    }

    public ResponseBean(HttpStatus responseCode, T rData) {
        this.rStatus = responseCode;
        this.rCode = responseCode.value();
        this.rData = rData;
        this.rMsg = "";
    }

    public ResponseBean(T rData) {
        this(HttpStatus.OK, rData);
    }

    public ResponseBean(HttpStatus responseCode, String sMessage, T rData) {
        this.rStatus = responseCode;
        this.rCode = responseCode.value();
        this.rMsg = sMessage;
        this.rData = rData;
    }

    public ResponseBean(HttpStatus responseCode, String sMessage, String displayMessage, T rData) {
        this.rStatus = responseCode;
        this.rCode = responseCode.value();
        this.rMsg = sMessage;
        this.rData = rData;
        this.displayMessage = displayMessage;
    }

    public ResponseBean(HttpStatus responseCode, int rCode, String sMessage, String displayMessage, T rData) {
        this.rStatus = responseCode;
        this.rCode = rCode;
        this.rMsg = sMessage;
        this.rData = rData;
        this.displayMessage = displayMessage;
    }

    public ResponseBean(HttpStatus responseCode, int rCode, String sMessage, String displayMessage, T rData, Pagination pagination) {
        this.rStatus = responseCode;
        this.rCode = rCode;
        this.rMsg = sMessage;
        this.rData = rData;
        this.displayMessage = displayMessage;
        this.pagination = pagination;
    }



    @Override
    public String toString() {
        return "ResponseBean{" +
                "rCode=" + rCode +
                ", rStatus=" + rStatus +
                ", rMsg='" + rMsg + '\'' +
                ", rData=" + rData +
                '}';
    }
}