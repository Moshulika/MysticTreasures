package com.Moshu.TreasureHunt.Core.API.Exceptions;

public class TreasureAlreadyRunningException extends RuntimeException {
    public TreasureAlreadyRunningException(String message) {
        super(message);
    }
}
