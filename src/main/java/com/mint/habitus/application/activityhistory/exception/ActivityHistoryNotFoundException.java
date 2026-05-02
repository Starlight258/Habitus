package com.mint.habitus.application.activityhistory.exception;

public class ActivityHistoryNotFoundException extends RuntimeException {

    public ActivityHistoryNotFoundException(Long id) {
        super("Activity history not found");
    }
}
