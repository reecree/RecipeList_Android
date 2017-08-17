package com.rupert.recipelist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.google.gson.Gson;
import com.pinterest.android.pdk.PDKClient;
import com.pinterest.android.pdk.PDKPin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class IngredientsActivity extends AppCompatActivity {

    private ListView _listView;
    private IngredientAdapter _ingredientAdapter;
    private ImageView _addButton;
    private EditText _wholeNumET;
    private EditText _numeratorET;
    private EditText _denominatorET;
    private EditText _nameET;
    private EditText _unitET;
    private AutoCompleteTextView _categoryTV;
    private View _selectedIngredientView = null;
    private int _selectedIngredientPos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredients);
        _listView = (ListView) findViewById(R.id.ingredient_list_view);
        _ingredientAdapter = new IngredientAdapter(this);

        _listView.setAdapter(_ingredientAdapter);
        _listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                onListItemClicked(pos, v);
            }
        });

        _addButton= (ImageView) findViewById(R.id.ingredient_add_button);
        _addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddIngredientClicked();
            }
        });

        Bundle extras = getIntent().getExtras();
        String extraObject = null;
        if (extras != null) {
            extraObject = extras.getString(Globals.INGREDIENT_KEY);
        }

        ArrayList<String> metadataList = new Gson().fromJson(extraObject, ArrayList.class);
        ShoppingList ingredientList = new ShoppingList();
        for( String metadata : metadataList) {
            try {
                JSONObject jsonObject = new JSONObject(metadata);
                JSONObject recipe = jsonObject.getJSONObject("recipe");
                JSONArray ingredientArray = recipe.getJSONArray("ingredients");
                for (int i = 0; i < ingredientArray.length(); i++) {
                    JSONArray ingredientCategory = ingredientArray.getJSONObject(i).getJSONArray("ingredients");
                    String category = ingredientArray.getJSONObject(i).getString("category");
                    for (int j = 0; j < ingredientCategory.length(); j++) {
                        JSONObject ingredient = ingredientCategory.getJSONObject(j);
                        ingredientList.add(new Ingredient(ingredient.getString("name"),
                                                          ingredient.getString("amount"),
                                                          category));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        _ingredientAdapter.setIngredientList(ingredientList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ingredients, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                PDKClient.getInstance().logout();
                Globals.RemoveAccessToken(this);
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onRemoveOk() {
        if(_selectedIngredientPos >= 0) {
            _ingredientAdapter.removeItem(_selectedIngredientPos);
            onListItemClicked(_selectedIngredientPos, _selectedIngredientView);
        }
    }

    private void onEditOk() {
        Ingredient ingredient = (Ingredient)_ingredientAdapter.getItem(_selectedIngredientPos);
        String wholeNumResp = _wholeNumET.getText().toString();
        String numeratorResp = _numeratorET.getText().toString();
        String denominatorResp = _denominatorET.getText().toString();
        String nameResp = _nameET.getText().toString().trim();
        String categoryResp = _categoryTV.getText().toString().trim();

        if(checkEditTextResponses(wholeNumResp, numeratorResp, nameResp, categoryResp)) return;

        ingredient.setAmountWithFraction(
                wholeNumResp.isEmpty() ? 0 : Integer.valueOf(wholeNumResp),
                numeratorResp.isEmpty() ? 0 : Integer.valueOf(numeratorResp),
                denominatorResp.isEmpty() || denominatorResp.equals("0") ? 1 : Integer.valueOf(denominatorResp));
        ingredient.setIngredientUnits(_unitET.getText().toString().trim());
        _ingredientAdapter.updateIngredientList(ingredient, nameResp, categoryResp);

        onListItemClicked(_selectedIngredientPos, _selectedIngredientView);

    }

    private void onAddOk() {
        String wholeNumResp = _wholeNumET.getText().toString();
        String numeratorResp = _numeratorET.getText().toString();
        String denominatorResp = _denominatorET.getText().toString();
        String nameResp = _nameET.getText().toString().trim();
        String categoryResp = _categoryTV.getText().toString().trim();
        String unitResp = _unitET.getText().toString().trim();

        if(checkEditTextResponses(wholeNumResp, numeratorResp, nameResp, categoryResp)) return;

        Ingredient ingredient = new Ingredient(nameResp,
                wholeNumResp.isEmpty() ? 0 : Integer.valueOf(wholeNumResp),
                numeratorResp.isEmpty() ? 0 : Integer.valueOf(numeratorResp),
                denominatorResp.isEmpty() || denominatorResp.equals("0") ? 1 : Integer.valueOf(denominatorResp),
                unitResp, categoryResp);
        _ingredientAdapter.addItem(ingredient);
    }

    private boolean checkEditTextResponses(String wholeNum, String numerator, String name, String category) {
        if(name.isEmpty()) {
            showInputAlert(getResources().getString(R.string.dialog_alert_empty_name));
            return true;
        }
        if((wholeNum.isEmpty() && numerator.isEmpty() ||
                (wholeNum.equals("0") && numerator.equals("0")))) {
            showInputAlert(getResources().getString(R.string.dialog_alert_empty_numbers));
            return true;
        }
        if(category.isEmpty()) {
            showInputAlert(getResources().getString(R.string.dialog_alert_empty_category));
            return true;
        }

        return false;
    }

    private void setVisibility(View v, List<Integer> ids, int visibility) {
        for(int i : ids) {
            View view = v.findViewById(i);
            view.setVisibility(visibility);
        }
    }

    private void onListItemClicked(int pos, View v) {
        if(_selectedIngredientView != null && _selectedIngredientPos == pos) {
            TextView subView = (TextView) _selectedIngredientView.findViewById(R.id.title);
            subView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            setVisibility(_selectedIngredientView,
                          Arrays.asList(R.id.edit_button, R.id.remove_button), View.GONE);
            _selectedIngredientPos = -1;
            _selectedIngredientView = null;
            return;
        }
        else if(_selectedIngredientView != null) {
            TextView subView = (TextView) _selectedIngredientView.findViewById(R.id.title);
            subView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            setVisibility(_selectedIngredientView,
                    Arrays.asList(R.id.edit_button, R.id.remove_button), View.GONE);
        }

        TextView subView = (TextView) v.findViewById(R.id.title);
        subView.setBackgroundColor(getResources().getColor(R.color.colorGrey));
        setVisibility(v, Arrays.asList(R.id.edit_button, R.id.remove_button), View.VISIBLE);
        _selectedIngredientPos = pos;
        _selectedIngredientView = v;
    }

    private void onRemoveButtonClicked() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }

        String ingredientName = ((Ingredient)_ingredientAdapter.getItem(_selectedIngredientPos)).getPrettyIngredientName();
        builder.setTitle(getResources().getString(R.string.ingredient_remove_alert))
                .setMessage(String.format(Locale.US,getResources().getString(R.string.ingredient_remove_message),ingredientName))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onRemoveOk();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void onAddIngredientClicked() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_ingredient, null);

        _wholeNumET = view.findViewById(R.id.dialog_ingr_whole_num);
        _numeratorET = view.findViewById(R.id.dialog_ingr_numerator);
        _denominatorET = view.findViewById(R.id.dialog_ingr_den);
        _unitET = view.findViewById(R.id.dialog_ingr_unit);
        _nameET = view.findViewById(R.id.dialog_ingr_name);
        _categoryTV = view.findViewById(R.id.dialog_ingr_category);

        _wholeNumET.setText("");
        _nameET.setText("");
        _numeratorET.setText("");
        _denominatorET.setText("");
        _unitET.setText("");

        List<String> categoryNameList = new ArrayList<String>(_ingredientAdapter.getIngredientList().getCategoryNameSet());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(),
                android.R.layout.simple_dropdown_item_1line, categoryNameList);

        _categoryTV.setAdapter(adapter);
        _categoryTV.setText("");

        builder.setView(view)
                .setTitle(R.string.dialog_ingr_add_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        onAddOk();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void onEditButtonClicked() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_ingredient, null);

        Ingredient ingredient = (Ingredient)_ingredientAdapter.getItem(_selectedIngredientPos);
        _wholeNumET = view.findViewById(R.id.dialog_ingr_whole_num);
        _numeratorET = view.findViewById(R.id.dialog_ingr_numerator);
        _denominatorET = view.findViewById(R.id.dialog_ingr_den);
        _unitET = view.findViewById(R.id.dialog_ingr_unit);
        _nameET = view.findViewById(R.id.dialog_ingr_name);
        _categoryTV = view.findViewById(R.id.dialog_ingr_category);

        _wholeNumET.setText(String.valueOf(ingredient.getWholeNumber()));
        _nameET.setText(ingredient.getPrettyIngredientName());
        if(ingredient.getNumerator() > 0) {
            _numeratorET.setText(String.valueOf(ingredient.getNumerator()));
        }
        if(ingredient.getDenominator() > 0) {
            _denominatorET.setText(String.valueOf(ingredient.getDenominator()));
        }
        if(!ingredient.getIngredientUnits().isEmpty()) {
            _unitET.setText(ingredient.getIngredientUnits());
        }

        List<String> categoryNameList = new ArrayList<String>(_ingredientAdapter.getIngredientList().getCategoryNameSet());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(),
                android.R.layout.simple_dropdown_item_1line, categoryNameList);

        _categoryTV.setAdapter(adapter);
        _categoryTV.setText(ingredient.getCategory());

        builder.setView(view)
                .setTitle(R.string.dialog_ingr_edit_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        onEditOk();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showInputAlert(String message) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }

        builder.setTitle(getResources().getString(R.string.dialog_alert_input_title))
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private class IngredientAdapter extends BaseAdapter {

        private ShoppingList _ingredientList;
        private Context _context;

        public IngredientAdapter(Context c) {
            _context = c;
        }

        public void setIngredientList(ShoppingList list) {
            if (_ingredientList == null) _ingredientList = new ShoppingList();
            if (list == null) _ingredientList.clear();
            else _ingredientList.addAll(list);
            notifyDataSetChanged();
        }

        public void removeItem(int pos) {
            _ingredientList.remove(pos);
            notifyDataSetChanged();
        }

        public ShoppingList getIngredientList() {
            return _ingredientList;
        }

        @Override
        public int getCount() {
            return _ingredientList == null ? 0 : _ingredientList.size();
        }

        @Override
        public Object getItem(int pos) {
            return _ingredientList == null || _ingredientList.size() <= pos ? null : _ingredientList.get(pos);
        }

        @Override
        public long getItemId(int pos) { return pos; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            IngredientsActivity.IngredientAdapter.ViewHolderItem viewHolder;
            if (convertView == null) {
                LayoutInflater inflater = ((Activity) _context).getLayoutInflater();
                convertView = inflater.inflate(R.layout.list_item_ingredient, parent, false);

                viewHolder = new IngredientsActivity.IngredientAdapter.ViewHolderItem();
                viewHolder.textViewItem = convertView.findViewById(R.id.title);
                viewHolder.headerItem = convertView.findViewById(R.id.separator);
                viewHolder.editButton = convertView.findViewById(R.id.edit_button);
                viewHolder.removeButton = convertView.findViewById(R.id.remove_button);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (IngredientsActivity.IngredientAdapter.ViewHolderItem) convertView.getTag();
            }

            Ingredient ingredient = _ingredientList.get(position);
            if (ingredient != null) {
                viewHolder.textViewItem.setText(ingredient.getIngredientDescription());
                viewHolder.editButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onEditButtonClicked();
                    }
                });

                viewHolder.removeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onRemoveButtonClicked();
                    }
                });
            }
            if (ingredient != null && ingredient.isFirst()) {
                viewHolder.headerItem.setText(ingredient.getCategory());
                viewHolder.headerItem.setVisibility(View.VISIBLE);
            }
            else {
                viewHolder.headerItem.setVisibility(View.GONE);
            }

            return convertView;
        }

        public void updateIngredientList(Ingredient ingredient, String newName, String newCategory) {
            _ingredientList.update(ingredient, newName, newCategory);
            notifyDataSetChanged();
        }

        public void addItem(Ingredient ingredient) {
            _ingredientList.add(ingredient);
            notifyDataSetChanged();
        }

        private class ViewHolderItem {
            TextView textViewItem;
            TextView headerItem;
            ImageView editButton;
            ImageView removeButton;
        }
    }
}
