package com.example.pamir.myapplication;

public class Country {
    private String isoCode;
    private String dialingCode;
    private String flagResId;
    private String name;

    public Country() {
    }

    public Country(String isoCode, String dialingCode, String flagResId, String name) {
        this.isoCode = isoCode;
        this.dialingCode = dialingCode;
        this.flagResId = flagResId;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFlagResId() {
        return flagResId;
    }

    public void setFlagResId(String flagResId) {
        this.flagResId = flagResId;
    }

    public String getIsoCode() {
        return this.isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    public String getDialingCode() {
        return this.dialingCode;
    }

    public void setDialingCode(String dialingCode) {
        this.dialingCode = dialingCode;
    }
}
