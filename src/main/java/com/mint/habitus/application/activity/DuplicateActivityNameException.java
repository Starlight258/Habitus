package com.mint.habitus.application.activity;

public class DuplicateActivityNameException extends RuntimeException {

    public DuplicateActivityNameException(String name) {
        super("Activity name already exists");
    }
}
