package com.rupert.recipelist;

public class Ingredient {
    private String ingredientName;
    private String ingredientAmount;

    public Ingredient(String name, String amount) {
        ingredientName = name;
        ingredientAmount = amount;
    }
    public void setIngredientName(String name) {
        ingredientName = name;
    }

    public void setIngredientAmount(String amount) {
        ingredientAmount = amount;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public String getIngredientAmount() {
        return ingredientAmount;
    }
}
