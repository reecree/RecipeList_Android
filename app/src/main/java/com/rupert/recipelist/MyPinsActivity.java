package com.rupert.recipelist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.pinterest.android.pdk.PDKCallback;
import com.pinterest.android.pdk.PDKClient;
import com.pinterest.android.pdk.PDKException;
import com.pinterest.android.pdk.PDKPin;
import com.pinterest.android.pdk.PDKResponse;
import com.rupert.recipelist.CustomViews.ExpandableListView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MyPinsActivity extends AppCompatActivity {

    private PDKCallback _myPinsCallback;
    private PDKResponse _myPinsResponse;
    private ImageView _ingredientsButton;
    private View _noMetadataView;
    private View _recipeView;
    private ImageView _popupExitButton;
    private GridView _gridView;
    private PinsAdapter _pinAdapter;
    private String _boardId;
    private HashSet<Integer> _highlightedItems;
    private boolean _loading = false;
    private boolean _isIngredientClickable = false;
    private static final String PIN_FIELDS = "id,link,creator,image,counts,note,created_at,board,metadata";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_pins);
        Bundle extras = getIntent().getExtras();
        _highlightedItems = new HashSet<Integer>();
        _noMetadataView = findViewById(R.id.pin_no_metadata);
        _recipeView = findViewById(R.id.pin_recipe);

        _ingredientsButton = (ImageView) findViewById(R.id.ingredient_button);
        _ingredientsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_isIngredientClickable)
                    onIngredients();
            }
        });

        _popupExitButton = (ImageView) findViewById(R.id.exit_popout_button);
        _popupExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _recipeView.setVisibility(View.GONE);
                _noMetadataView.setVisibility(View.GONE);
                _popupExitButton.setVisibility(View.GONE);
            }
        });

        if(extras != null) {
            setTitle(extras.getString(Globals.BOARD_NAME_KEY));
            _boardId = extras.getString(Globals.BOARD_ID_KEY);
        }
        else {
            setTitle("My Pins");
            _boardId = null;
        }
        _pinAdapter = new PinsAdapter(this);
        _gridView = (GridView) findViewById(R.id.grid_view);

        _gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                onGridItemClicked(pos, v);
            }
        });
        _gridView.setAdapter(_pinAdapter);
        _myPinsCallback = new PDKCallback() {
            @Override
            public void onSuccess(PDKResponse response) {
                _loading = false;
                _myPinsResponse = response;
                _pinAdapter.setPinList(response.getPinList());
            }

            @Override
            public void onFailure(PDKException exception) {
                _loading = false;
                Log.e(getClass().getName(), exception.getDetailMessage());
            }
        };
        _loading = true;
    }

    private void onGridItemClicked(int pos, View v) {
        String metadata = ((PDKPin) _pinAdapter.getItem(pos)).getMetadata();

        if(!containsIngredients(metadata)) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.toast_no_ingredient), Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        if(!_highlightedItems.contains(pos)) {
            Drawable border = getResources().getDrawable(R.drawable.highlighted_border);
            v.setBackground(border);
            _highlightedItems.add(pos);
        }
        else {
            Drawable border = getResources().getDrawable(R.drawable.border);
            v.setBackground(border);
            _highlightedItems.remove(pos);
        }
        if(!_highlightedItems.isEmpty()) {
            Drawable clickableCircleButton = getResources().getDrawable(R.drawable.circle_button);
            _ingredientsButton.setBackground(clickableCircleButton);
            _isIngredientClickable = true;
        }
        else {
            Drawable unclickableCircleButton = getResources().getDrawable(R.drawable.unclickable_circle_button);
            _ingredientsButton.setBackground(unclickableCircleButton);
            _isIngredientClickable = false;
        }
    }

    private boolean containsIngredients(String metadata) {
        if(isMetadataEmpty(metadata)) return false;

        try {
            JSONObject jsonObject = new JSONObject(metadata);
            JSONObject recipe = jsonObject.getJSONObject("recipe");
            if(recipe != null) {
                JSONArray ingredientArray = recipe.getJSONArray("ingredients");
                if(ingredientArray != null && ingredientArray.length() > 0) return true;
            }

            return false;

        } catch (JSONException e) {
            return false;
        }
    }

    private void onIngredients() {
        Intent i = new Intent(this, IngredientsActivity.class);

        List<String> selectedPins = new ArrayList<String>();
        for (int pos : _highlightedItems) {
            selectedPins.add(((PDKPin) _pinAdapter.getItem(pos)).getMetadata());
        }
        i.putExtra(Globals.INGREDIENT_KEY, new Gson().toJson(selectedPins));
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchPins();
    }

    private void fetchPins() {
        _pinAdapter.setPinList(null);
        if(_boardId == null) {
            PDKClient.getInstance().getMyPins(PIN_FIELDS, _myPinsCallback);
        }
        else {
            PDKClient.getInstance().getBoardPins(_boardId, PIN_FIELDS, _myPinsCallback);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_pins, menu);
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

    private void loadNext() {
        if (!_loading && _myPinsResponse.hasNext()) {
            _loading = true;
            _myPinsResponse.loadNext(_myPinsCallback);
        }
    }

    private boolean isMetadataEmpty(String metadata) {
        return metadata == null || metadata.isEmpty() || metadata.equals(Globals.EMPTY_JSON);
    }

    private void onExpandButtonClicked(PDKPin pin) {
        String metadata = pin.getMetadata();
        _popupExitButton.setVisibility(View.VISIBLE);
        if (isMetadataEmpty(metadata)) {
            displayNoMetadataPin(pin);
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(metadata);
            JSONObject recipe = jsonObject.getJSONObject("recipe");
            displayRecipePin(pin, recipe);
            return;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        displayNoMetadataPin(pin);
    }

    private void displayRecipePin(PDKPin pin, JSONObject recipe) {
        ImageView mainImage = _recipeView.findViewById(R.id.recipe_main_image);
        TextView noteView = _recipeView.findViewById(R.id.recipe_note);
        TextView servingView = _recipeView.findViewById(R.id.recipe_serving);
        RelativeLayout customGrid = _recipeView.findViewById(R.id.recipe_custom_grid);
        customGrid.removeAllViews();

        Picasso.with(getApplicationContext()).load(pin.getImageUrl()).into(mainImage);
        String note = pin.getNote();
        if(note != null && !note.isEmpty()) {
            noteView.setText(note);
            noteView.setVisibility(View.VISIBLE);
        }
        else {
            noteView.setVisibility(View.GONE);
        }

        try {
            JSONObject servings = recipe.getJSONObject("servings");
            String serves = servings.getString("serves");
            servingView.setText(String.format(Locale.US, getString(R.string.pin_recipe_serving), serves));
            servingView.setVisibility(View.VISIBLE);
        } catch (JSONException e) {
            servingView.setVisibility(View.GONE);
        }

        ArrayMap<String, List<String>> categories = new ArrayMap<String, List<String>>();
        try {
            JSONArray ingredientArray = recipe.getJSONArray("ingredients");
            for (int i = 0; i < ingredientArray.length(); i++) {
                JSONArray ingredientCategory = ingredientArray.getJSONObject(i).getJSONArray("ingredients");
                String category = ingredientArray.getJSONObject(i).getString("category");
                List<String> ingredients = new ArrayList<String>();
                for (int j = 0; j < ingredientCategory.length(); j++) {
                    JSONObject ingredient = ingredientCategory.getJSONObject(j);
                    ingredients.add(ingredient.getString("amount") + " " + ingredient.getString("name"));
                }
                categories.put(category, ingredients);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        addLayoutToCustomGrid(categories, customGrid, this);

        _recipeView.setBackgroundColor(getResources().getColor(R.color.colorTransparentBackground));
        _recipeView.setVisibility(View.VISIBLE);
    }

    private void addLayoutToCustomGrid(ArrayMap<String, List<String>> categories, RelativeLayout customGrid, Context c) {
        int curId;
        int aboveIdCol0 = -1;
        int aboveIdCol1 = -1;
        int lastId = -1;
        int col = 0;
        int halfScreenWidth = getHalfScreenWidth();
        for(Map.Entry<String, List<String>> entry : categories.entrySet()) {
            curId = View.generateViewId();
            LayoutInflater inflater = ((Activity) c).getLayoutInflater();
            LinearLayout item = (LinearLayout) inflater.inflate(R.layout.pin_recipe_grid_item, customGrid, false);
            item.setLayoutParams(new RelativeLayout.LayoutParams(halfScreenWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
            item.setId(curId);

            TextView tv = item.findViewById(R.id.recipe_grid_header);
            ExpandableListView lv = item.findViewById(R.id.recipe_grid_list);

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(c,
                    android.R.layout.simple_list_item_1, android.R.id.text1, entry.getValue());
            tv.setText(entry.getKey());
            lv.setAdapter(arrayAdapter);
            lv.setExpanded(true);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)item.getLayoutParams();
            if(lastId >= 0 && col == 0) {
                params.addRule(RelativeLayout.ALIGN_PARENT_START);
                if(aboveIdCol0 >= 0) {
                    params.addRule(RelativeLayout.BELOW, aboveIdCol0);
                }

                aboveIdCol0 = curId;
                col = 1;
            }
            else if(lastId >= 0 && col ==1) {
                params.addRule(RelativeLayout.END_OF, lastId);
                if(aboveIdCol1 >= 0) {
                    params.addRule(RelativeLayout.BELOW, aboveIdCol1);
                }

                aboveIdCol1 = curId;
                col = 0;
            }
            else {
                aboveIdCol0 = curId;
                col = 1;
            }

            item.setLayoutParams(params);
            lastId = curId;
            customGrid.addView(item);
        }
    }

    private void displayNoMetadataPin(PDKPin pin) {
        ImageView mainImage = _noMetadataView.findViewById(R.id.no_metadata_image_view);
        TextView noteView = _noMetadataView.findViewById(R.id.no_metadata_note);

        Picasso.with(getApplicationContext()).load(pin.getImageUrl()).into(mainImage);
        String note = pin.getNote();
        if(note != null && !note.isEmpty()) {
            noteView.setText(note);
            noteView.setVisibility(View.VISIBLE);
        }
        else {
            noteView.setVisibility(View.GONE);
        }

        _noMetadataView.setBackgroundColor(getResources().getColor(R.color.colorTransparentBackground));
        _noMetadataView.setVisibility(View.VISIBLE);
    }

    private int getHalfScreenWidth() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.widthPixels/2;
    }

    private class PinsAdapter extends BaseAdapter {

        private List<PDKPin> _pinList;
        private Context _context;
        public PinsAdapter(Context c) {
            _context = c;
        }

        public void setPinList(List<PDKPin> list) {
            if (_pinList == null) _pinList = new ArrayList<PDKPin>();
            if (list == null) _pinList.clear();
            else _pinList.addAll(list);
            notifyDataSetChanged();
        }

        public List<PDKPin> getPinList() {
            return _pinList;
        }
        @Override
        public int getCount() {
            return _pinList == null ? 0 : _pinList.size();
        }

        @Override
        public Object getItem(int position) {
            if(_pinList == null || position >= _pinList.size())
                return null;
            else
                return _pinList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolderItem viewHolder;

            //load more pins if about to reach end of list
            if (_pinList.size() - position < 5) {
                loadNext();
            }

            if (convertView == null) {
                LayoutInflater inflater = ((Activity) _context).getLayoutInflater();
                convertView = inflater.inflate(R.layout.list_item_pin, parent, false);

                viewHolder = new ViewHolderItem();
                viewHolder.textViewItem = convertView.findViewById(R.id.title_view);
                viewHolder.imageViewMain = convertView.findViewById(R.id.image_view);
                viewHolder.expandButton = convertView.findViewById(R.id.expand_button);

                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolderItem) convertView.getTag();
            }

            final PDKPin pinItem = _pinList.get(position);
            if (pinItem != null) {
                if(_highlightedItems.contains(position)) {
                    Drawable border = getResources().getDrawable(R.drawable.highlighted_border);
                    convertView.setBackground(border);
                }
                else {
                    Drawable border = getResources().getDrawable(R.drawable.border);
                    convertView.setBackground(border);
                }
                viewHolder.textViewItem.setText(pinItem.getNote());
                Picasso.with(_context.getApplicationContext()).load(pinItem.getImageUrl()).into(viewHolder.imageViewMain);
                viewHolder.expandButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onExpandButtonClicked(pinItem);
                    }
                });
            }

            return convertView;
        }

        private class ViewHolderItem {
            TextView textViewItem;
            ImageView imageViewMain;
            ImageView expandButton;
        }
    }
}