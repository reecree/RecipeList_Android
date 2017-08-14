package com.rupert.recipelist;

import android.support.v4.util.ArrayMap;

import org.apache.commons.math3.fraction.Fraction;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShoppingList {

    private Map<String, Ingredient> _combinedIngredientMap;
    private Map<String, List<String>> _categories;

    public ShoppingList() {
        _combinedIngredientMap = new HashMap<String,Ingredient>();
        _categories = new ArrayMap<String, List<String>>();
    }

    public void add(Ingredient ingredient) {
        String ingredientPrettyName = ingredient.getPrettyIngredientName();
        if(isBannedIngredient(ingredientPrettyName))
            return;
        Ingredient storedIngredient = _combinedIngredientMap.get(ingredientPrettyName);

        if(storedIngredient == null) {
            if(!_categories.containsKey(ingredient.getCategory())) {
                ArrayList<String> categoryList = new ArrayList<String>();
                categoryList.add(ingredientPrettyName);
                _categories.put(ingredient.getCategory(), categoryList);
                ingredient.setIsHeader(true);
            }
            else {
                ingredient.setIsHeader(false);
                _categories.get(ingredient.getCategory()).add(ingredientPrettyName);
            }

            _combinedIngredientMap.put(ingredientPrettyName, ingredient);
        }
        else {
            String ingredientUnits = ingredient.getIngredientUnits();
            Fraction ingredientAmount = ingredient.getIngredientAmount();
            if(!storedIngredient.addIngredientAmount(ingredientAmount, ingredientUnits)) {
                String ingredientNameWithUnits = appendUnitsToName(ingredientPrettyName, ingredientUnits);
                Ingredient storedIngredientWithUnits = _combinedIngredientMap.get(ingredientNameWithUnits);
                if(storedIngredientWithUnits == null ||
                        !storedIngredientWithUnits.addIngredientAmount(ingredientAmount, ingredientUnits)) {
                    if(!_categories.containsKey(ingredient.getCategory())) {
                        ArrayList<String> categoryList = new ArrayList<String>();
                        categoryList.add(ingredientNameWithUnits);
                        _categories.put(ingredient.getCategory(), categoryList);
                        ingredient.setIsHeader(true);
                    }

                    else {
                        ingredient.setIsHeader(false);
                        _categories.get(ingredient.getCategory()).add(ingredientNameWithUnits);
                    }

                    ingredient.setPrettyName(ingredientNameWithUnits);
                    _combinedIngredientMap.put(ingredientNameWithUnits, ingredient);
                }
            }
        }
    }

    public void clear() {
        _combinedIngredientMap.clear();
    }

    public Collection<Ingredient> getEnumberableList() {
        return _combinedIngredientMap.values();
    }

    public void addAll(ShoppingList list) {
        for(Ingredient ingredient : list.getEnumberableList()) {
            add(ingredient);
        }
    }

    public int size() {
        return _combinedIngredientMap.size();
    }

    public Ingredient get(int pos) {
        int count = 0;
        for(List<String> categorySection : _categories.values()) {
            if(pos < count + categorySection.size()) {
                return _combinedIngredientMap.get(categorySection.get(pos - count));
            }
            count += categorySection.size();
        }
        return null;
    }

    private String appendUnitsToName(String originalName, String units) {
        return originalName + " (" + units + ")";
    }

    private boolean isBannedIngredient(String name) {
        Pattern p = Pattern.compile("^(salt|pepper|salt.*pepper)$", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(name);
        if(m.find()) {
            return true;
        }
        return false;
    }
}