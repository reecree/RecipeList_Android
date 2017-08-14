package com.rupert.recipelist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.google.gson.Gson;
import com.pinterest.android.pdk.PDKClient;
import com.pinterest.android.pdk.PDKPin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class IngredientsActivity extends AppCompatActivity {

    private ListView _listView;
    private IngredientAdapter _ingredientAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredients);
        _listView = (ListView) findViewById(R.id.ingredient_list_view);
        _ingredientAdapter = new IngredientAdapter(this);

        _listView.setAdapter(_ingredientAdapter);
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

        public ShoppingList getIngredientList() {
            return _ingredientList;
        }

        @Override
        public int getCount() {
            return _ingredientList == null ? 0 : _ingredientList.size();
        }

        @Override
        public Object getItem(int pos) {
            return _ingredientList == null || _ingredientList.size() >= pos ? null : _ingredientList.get(pos);
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
                viewHolder.textViewItem = (TextView) convertView.findViewById(R.id.title);
                viewHolder.headerItem = (TextView) convertView.findViewById(R.id.separator);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (IngredientsActivity.IngredientAdapter.ViewHolderItem) convertView.getTag();
            }

            Ingredient ingredient = _ingredientList.get(position);
            if (ingredient != null) {
                viewHolder.textViewItem.setText(ingredient.getIngredientDescription());
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

        private class ViewHolderItem {
            TextView textViewItem;
            TextView headerItem;
        }
    }
}
