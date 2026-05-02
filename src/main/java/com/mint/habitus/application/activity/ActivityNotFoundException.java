package com.mint.habitus.application.activity;

public class ActivityNotFoundException extends RuntimeException {

    public ActivityNotFoundException(Long id) {
        super("Activity not found");
    }
}
