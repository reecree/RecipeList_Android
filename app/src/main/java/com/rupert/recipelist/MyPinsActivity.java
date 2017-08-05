package com.rupert.recipelist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import com.google.gson.Gson;
import com.pinterest.android.pdk.PDKCallback;
import com.pinterest.android.pdk.PDKClient;
import com.pinterest.android.pdk.PDKException;
import com.pinterest.android.pdk.PDKPin;
import com.pinterest.android.pdk.PDKResponse;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MyPinsActivity extends AppCompatActivity {

    private PDKCallback myPinsCallback;
    private PDKResponse myPinsResponse;
    private Button ingredientsButton;
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

        ingredientsButton = (Button) findViewById(R.id.ingredient_button);
        ingredientsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onIngredients();
            }
        });

        if(extras != null) {
            setTitle(extras.getString(Globals.getBoardNameKey()));
            _boardId = extras.getString(Globals.getBoardIdKey());
        }
        else {
            setTitle("My Pins");
            _boardId = null;
        }
        _pinAdapter = new PinsAdapter(this);
        _gridView = (GridView) findViewById(R.id.grid_view);

        _gridView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenu.ContextMenuInfo menuInfo) {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.context_menu_boards, menu);
            }
        });
        _gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
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
                    ingredientsButton.setBackground(clickableCircleButton);
                    _isIngredientClickable = true;
                    //ingredientsButton.setClickable(false);
                    //ingredientsButton.setVisibility(View.VISIBLE);
                }
                else {
                    Drawable unclickableCircleButton = getResources().getDrawable(R.drawable.unclickable_circle_button);
                    ingredientsButton.setBackground(unclickableCircleButton);
                    _isIngredientClickable = false;
                    //ingredientsButton.setClickable(false);
                    //ingredientsButton.setVisibility(View.INVISIBLE);
                }
            }
        });
        _gridView.setAdapter(_pinAdapter);
        myPinsCallback = new PDKCallback() {
            @Override
            public void onSuccess(PDKResponse response) {
                _loading = false;
                myPinsResponse = response;
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

    private void onIngredients() {
        Intent i = new Intent(this, IngredientsActivity.class);

        List<PDKPin> selectedPins = new ArrayList<PDKPin>();
        for (int pos : _highlightedItems) {
            selectedPins.add((PDKPin) _pinAdapter.getItem(pos));
        }
        i.putExtra(Globals.getIngredientArrayKey(), new Gson().toJson(selectedPins));
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
            PDKClient.getInstance().getMyPins(PIN_FIELDS, myPinsCallback);
        }
        else {
            PDKClient.getInstance().getBoardPins(_boardId, PIN_FIELDS, myPinsCallback);
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
            case R.id.action_new_pin:
                //createNewPin();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createNewPin() {
        /*Intent i = new Intent(this, CreatePinActivity.class);
        startActivity(i);*/
    }

    /*private void deletePin(int position) {
        PDKClient.getInstance().deletePin(_pinAdapter.getPinList().get(position).getUid(),
                new PDKCallback() {
                    @Override
                    public void onSuccess(PDKResponse response) {
                        Log.d(getClass().getName(), "Response: " + response.getStatusCode());
                        fetchPins();
                    }

                    @Override
                    public void onFailure(PDKException exception) {
                        Log.e(getClass().getName(), "error: " + exception.getDetailMessage());
                    }
                });
    }*/

    private void loadNext() {
        if (!_loading && myPinsResponse.hasNext()) {
            _loading = true;
            myPinsResponse.loadNext(myPinsCallback);
        }
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
                viewHolder.textViewItem = (TextView) convertView.findViewById(R.id.title_view);
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image_view);

                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolderItem) convertView.getTag();
            }

            PDKPin pinItem = _pinList.get(position);
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
                Picasso.with(_context.getApplicationContext()).load(pinItem.getImageUrl()).into(viewHolder.imageView);
            }

            return convertView;
        }

        private class ViewHolderItem {
            TextView textViewItem;
            ImageView imageView;
        }
    }
}