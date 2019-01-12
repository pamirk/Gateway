package com.example.pamir.myapplication;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
public interface AppComponent {

    TypefaceHelper typefaceHelper();

}
