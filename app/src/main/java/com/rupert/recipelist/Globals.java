package com.rupert.recipelist;

public class Globals{

    private final static String BoardNameKey = "boardName";
    private final static String BoardIdKey = "boardId";
    private final static String IngredientArrayKey = "ingrArray";

    private Globals(){}

    public static String getBoardNameKey() {
        return BoardNameKey;
    }

    public static String getBoardIdKey() {
        return BoardIdKey;
    }

    public static String getIngredientArrayKey() {
        return IngredientArrayKey;
    }
}
