package com.rupert.recipelist;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;


import com.google.gson.Gson;
import com.pinterest.android.pdk.PDKBoard;
import com.pinterest.android.pdk.PDKCallback;
import com.pinterest.android.pdk.PDKPin;
import com.pinterest.android.pdk.PDKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IngredientsActivity extends AppCompatActivity {

    private PDKCallback myPinsCallback;
    private PDKResponse myPinsResponse;
    private Button ingredientsButton;
    private ListView _listView;
    //private Map<String,String> _ingredientList;

    private boolean _loading = false;

    private static final String PIN_FIELDS = "id,link,creator,image,counts,note,created_at,board,metadata";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredients);
        _listView = (ListView) findViewById(R.id.ingredient_list_view);
        Bundle extras = getIntent().getExtras();
        String extraObject = null;
        if (extras != null) {
            extraObject = extras.getString(Globals.getIngredientArrayKey());
        }

        ArrayList<PDKPin> pinList = new Gson().fromJson(extraObject, ArrayList.class);
        List<Ingredient> ingredientList = new ArrayList<Ingredient>();
        for( PDKPin pin : pinList) {
            String metadata = pin.getMetadata();
            try {
                JSONObject jsonObject = new JSONObject(metadata);
                JSONObject recipe = jsonObject.getJSONObject("recipe");
                JSONArray ingredientArray = recipe.getJSONArray("ingredients");
                for (int i = 0; i < ingredientArray.length(); i++) {
                    JSONArray ingredientCategory = ingredientArray.getJSONObject(i).getJSONArray("ingredients");
                    for (int j = 0; j < ingredientCategory.length(); j++) {
                        JSONObject ingredient = ingredientCategory.getJSONObject(j);

                        ingredientList.add(new Ingredient(ingredient.getString("name"), ingredient.getString("amount")));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_pins, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_pin:
                //createNewPin();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class IngredientAdapter extends BaseAdapter {

        private List<Ingredient> _ingredientList;
        private Context _context;

        public IngredientAdapter(Context c) {
            _context = c;
        }

        public void setIngredientList(List<Ingredient> list) {
            if (_ingredientList == null) _ingredientList = new ArrayList<Ingredient>();
            if (list == null) _ingredientList.clear();
            else _ingredientList.addAll(list);
            notifyDataSetChanged();
        }

        public List<Ingredient> getIngredientList() {
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
                convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);

                viewHolder = new IngredientsActivity.IngredientAdapter.ViewHolderItem();
                viewHolder.textViewItem = (TextView) convertView.findViewById(android.R.id.text1);

                convertView.setTag(viewHolder);

            } else {
                viewHolder = (IngredientsActivity.IngredientAdapter.ViewHolderItem) convertView.getTag();
            }

            PDKBoard boardItem = _boardList.get(position);
            if (boardItem != null) {
                viewHolder.textViewItem.setText(boardItem.getName());
            }

            return convertView;
        }

        private class ViewHolderItem {
            TextView textViewItem;
        }
    }
}
