package io.oobeya.committracker.enums;

public enum CommitStatus {
    UNKNOWN("Unknown"),
    NO_MESSAGE("No message"),
    UNKNOWN_AUTHOR("Unknown author"),
    UNKNOWN_DATE("Unknown date");

    private final String message;

    //TODO javada hata messajlar nasıl hndle edilir, nasıl tanımlanır, enum ile record arasındaki farka bakabilirsin

    CommitStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}