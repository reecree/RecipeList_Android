package com.rupert.recipelist;

public class Globals{

    private final static String BoardNameKey = "boardName";
    private final static String BoardIdKey = "boardId";

    private Globals(){}

    public static String getBoardNameKey() {
        return BoardNameKey;
    }

    public static String getBoardIdKey() {
        return BoardIdKey;
    }
}
