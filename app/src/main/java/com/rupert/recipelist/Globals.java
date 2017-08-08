package com.rupert.recipelist;

public class Globals{

    private final static String BoardNameKey = "boardName";
    private final static String BoardIdKey = "boardId";
    private final static String IngredientArrayKey = "ingrArray";

    private final static String NoIngredientMessage = "Error:\nSelected Pin Has No Ingredient Information";

    private final static String EmptyJson = "{}";

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

    public static String getNoIngredientMessage() {
        return NoIngredientMessage;
    }

    public static String getEmptyJson() {
        return EmptyJson;
    }
}
