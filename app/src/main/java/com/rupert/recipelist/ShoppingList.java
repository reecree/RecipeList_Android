package com.rupert.recipelist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShoppingList {

    private Map<String, String> _combinedIngredientMap;
    private List<Ingredient> _ingredientList;

    public ShoppingList() {
        _combinedIngredientMap = new HashMap<String,String>();
        _ingredientList = new ArrayList<Ingredient>();
    }

    public void add(Ingredient ingredient) {
        _ingredientList.add(ingredient);

    }
}