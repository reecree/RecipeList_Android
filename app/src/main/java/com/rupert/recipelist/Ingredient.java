package com.rupert.recipelist;

import java.util.Locale;

public class Ingredient {
    private String _rawIngredientName;
    private String _rawIngredientAmount;
    private String _prettyIngredientName;
    private double _ingredientAmount;
    private String _ingredientUnits;

    public Ingredient(String name, String amount) {
        _rawIngredientName = name;
        _rawIngredientAmount = amount;
        _prettyIngredientName = prettifyIngredientType(name);
        _ingredientAmount = getIngredientAmount(amount);
        _ingredientUnits = getIngredientUnits(amount);
    }
//    public void setIngredientName(String name) {
//        _ingredientName = name;
//    }

//    public void setIngredientAmount(String amount) {
//        _ingredientAmount = amount;
//    }

    public String getPrettyIngredientName() {
        return _prettyIngredientName;
    }

    public double getIngredientAmount() {
        return _ingredientAmount;
    }

    public String get_ingredientUnits() {
        return _ingredientUnits;
    }

    public String getEasyIngredientDescription() {
        return String.format(Locale.US, "%s of %s", _rawIngredientAmount, _rawIngredientName);
    }

    public String getIngredientDescription() {
        return String.format(Locale.US, "%f %s of %s", _ingredientAmount, _ingredientUnits, _prettyIngredientName);
    }


    private String prettifyIngredientType(String word) {
        return word.replaceAll("\\,.*$", "");
    }

    private void prettifyIngredientAmount(String amount) {

    }

    private String getIngredientUnits(String amount) {
        return null;
    }

    private double getIngredientAmount(String amountString) {
        return 0;
    }
}
