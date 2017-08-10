package com.rupert.recipelist;

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
    private List<String> _lookupArray;

    public ShoppingList() {
        _combinedIngredientMap = new HashMap<String,Ingredient>();
        _lookupArray = new ArrayList<String>();
    }

    public void add(Ingredient ingredient) {
        String ingredientPrettyName = ingredient.getPrettyIngredientName();
        if(isBannedIngredient(ingredientPrettyName))
            return;
        Ingredient storedIngredient = _combinedIngredientMap.get(ingredientPrettyName);

        if(storedIngredient == null) {
            _combinedIngredientMap.put(ingredientPrettyName, ingredient);
            _lookupArray.add(ingredientPrettyName);
        }
        else {
            String ingredientUnits = ingredient.getIngredientUnits();
            Fraction ingredientAmount = ingredient.getIngredientAmount();
            if(!storedIngredient.addIngredientAmount(ingredientAmount, ingredientUnits)) {
                String ingredientNameWithUnits = appendUnitsToName(ingredientPrettyName, ingredientUnits);
                Ingredient storedIngredientWithUnits = _combinedIngredientMap.get(ingredientNameWithUnits);
                if(storedIngredientWithUnits == null ||
                        !storedIngredientWithUnits.addIngredientAmount(ingredientAmount, ingredientUnits)) {
                    ingredient.setPrettyName(ingredientNameWithUnits);
                    _combinedIngredientMap.put(ingredientNameWithUnits, ingredient);
                    _lookupArray.add(ingredientPrettyName);
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
        return _combinedIngredientMap.get(_lookupArray.get(pos));
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