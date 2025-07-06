package me.Herzchen.minereposync.core;

public class GitSyncException extends Exception {
    public GitSyncException(String message) {
        super(message);
    }

    public GitSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}