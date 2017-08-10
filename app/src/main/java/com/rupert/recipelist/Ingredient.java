package com.rupert.recipelist;

import org.apache.commons.math3.fraction.Fraction;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ingredient {
    private String _rawIngredientName;
    private String _rawIngredientAmount;
    private String _prettyIngredientName;
    private Fraction _ingredientAmount;
    private String _ingredientUnits;

    public Ingredient(String name, String amount) {
        _rawIngredientName = name;
        _rawIngredientAmount = amount;
        _prettyIngredientName = prettifyIngredientType(name);
        _ingredientAmount = parseIngredientAmount(amount);
        _ingredientUnits = parseIngredientUnits(amount);
    }

    public String getPrettyIngredientName() {
        return _prettyIngredientName;
    }

    public Fraction getIngredientAmount() {
        return _ingredientAmount;
    }

    public String getIngredientUnits() {
        return _ingredientUnits;
    }

    public void setPrettyName(String name) {
        _prettyIngredientName = name;
    }

    public boolean addIngredientAmount(Fraction amount, String units) {
        Fraction fraction;
        if(units.equals(_ingredientUnits)) {
            _ingredientAmount = _ingredientAmount.add(amount);
        }
        else {
            try {
                _ingredientAmount = _ingredientAmount.add(convertUnits(amount, units, _ingredientUnits));
            } catch(NoConversionException e) {
                return false;
            }
        }
        return true;
    }

    public String getIngredientDescription() {
        String amount = getIngredientAmountPretty();
        if(_ingredientUnits.isEmpty()) {
            return String.format(Locale.US, "%s %s", amount, _prettyIngredientName);
        }
        else {
            return String.format(Locale.US, "%s %s of %s", amount, _ingredientUnits, _prettyIngredientName);
        }
    }

    private String getIngredientAmountPretty() {
        int a = _ingredientAmount.getNumerator() / _ingredientAmount.getDenominator();
        int b = _ingredientAmount.getNumerator() % _ingredientAmount.getDenominator();
        if(a != 0 && b != 0) {
            return a + "&" + b + "/" + _ingredientAmount.getDenominator();
        }
        else if(a != 0) {
            return String.valueOf(a);
        }
        else {
            return b + "/" + _ingredientAmount.getDenominator();
        }
    }

    private Fraction convertUnits(Fraction amount, String initialUnit, String convertedUnit) throws NoConversionException {
        throw new NoConversionException();
    }

    private String prettifyIngredientType(String word) {
        return word.replaceAll("\\,.*$", "");
    }

    private String parseIngredientUnits(String amount) {
        return amount.replaceAll("(^\\d+$)|(^\\d+ )*(\\d+/\\d+ )*", "");
    }

    private Fraction parseIngredientAmount(String amountString) {
        Fraction fraction = getFraction(amountString);
        fraction = fraction.add(getFirstNumber(amountString));
        return fraction;
    }

    private Fraction getFraction(String amount) {
        Pattern p = Pattern.compile("(\\d+)/(\\d+)");
        Matcher m = p.matcher(amount);

        if (m.find()) {
            return new Fraction(Integer.valueOf(m.group(1)), Integer.valueOf(m.group(2)));
        }
        else {
            return new Fraction(0);
        }
    }

    private int getFirstNumber(String word) {
        Pattern p = Pattern.compile("(^\\d+)");
        Matcher m = p.matcher(word);
        if(m.find()) {
            return Integer.valueOf(m.group());
        }
        return 0;
    }

    private class NoConversionException extends Exception
    {
        // Parameterless Constructor
        NoConversionException() {}

        // Constructor that accepts a message
        public NoConversionException(String message)
        {
            super(message);
        }
    }
}
