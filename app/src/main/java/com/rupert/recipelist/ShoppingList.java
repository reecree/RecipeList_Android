package com.rupert.recipelist;

import android.support.v4.util.ArrayMap;

import org.apache.commons.math3.fraction.Fraction;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public void remove(int pos) {
        int count = 0;
        for(List<String> categorySection : _categories.values()) {
            if(pos < count + categorySection.size()) {
                Ingredient removedIngredient =
                        _combinedIngredientMap.remove(categorySection.remove(pos - count));
                if(removedIngredient.isFirst()) {
                    if(!categorySection.isEmpty()) {
                        _combinedIngredientMap.get(categorySection.get(0)).setIsHeader(true);
                    }
                }
                return;
            }
            count += categorySection.size();
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

    public Set<String> getCategoryNameSet() {
        return _categories.keySet();
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

    public void update(Ingredient ingredient, String newName, String newCategory) {
        String originalName = ingredient.getPrettyIngredientName();
        String originalCategory = ingredient.getCategory();
        if(originalName.equals(newName) && originalCategory.equals(newCategory))
            return;

        if(!originalName.equals(newName)) {
            Ingredient ingr = _combinedIngredientMap.remove(originalName);
            _combinedIngredientMap.put(newName, ingr);
        }

        _categories.get(originalCategory).remove(originalName);
        if(_categories.containsKey(newCategory)) {
            _categories.get(newCategory).add(newName);
            ingredient.setIsHeader(false);
        }
        else {
            ArrayList<String> newList = new ArrayList<String>();
            newList.add(newName);
            _categories.put(newCategory, newList);
            ingredient.setIsHeader(true);
        }

        if(_categories.get(originalCategory).isEmpty()) {
            _categories.remove(originalCategory);
        }
        else {
            _combinedIngredientMap.get(_categories.get(originalCategory).get(0)).setIsHeader(true);
        }

        ingredient.setCategory(newCategory);
        ingredient.setPrettyName(newName);
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