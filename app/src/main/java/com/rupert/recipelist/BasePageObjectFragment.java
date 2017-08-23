package com.rupert.recipelist;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BasePageObjectFragment extends Fragment {
    private Context _context;

    public void setContext(Context c) {
        _context = c;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(
                R.layout.tab_base_shopping_list, container, false);
        Bundle args = getArguments();

        ListView listView = rootView.findViewById(R.id.tab_base_list_view);

        Globals.Tabs tab = (Globals.Tabs) args.get(Globals.BASE_ID_KEY);
        switch (tab) {
            case ShoppingList:
                List<ShoppingList> lists = getSavedShoppingLists();
                if(lists != null) {
                    ShoppingListAdapter sla = new ShoppingListAdapter(_context);
                    sla.setShoppingList(lists);
                    listView.setAdapter(sla);
                }
                else {
                    listView.setVisibility(View.GONE);
                    TextView tv = rootView.findViewById(R.id.tab_base_text_view);
                    tv.setText(getString(R.string.base_no_shopping_list));
                    tv.setVisibility(View.VISIBLE);
                }
                break;
            case Recipe:
                listView.setVisibility(View.GONE);
                TextView tv = rootView.findViewById(R.id.tab_base_text_view);
                tv.setText(getString(R.string.base_no_recipes));
                tv.setVisibility(View.VISIBLE);
                break;
        }

        ArrayList<String> al = new ArrayList<String>();
        al.add("Test STraing" + args.getInt(Globals.BASE_ID_KEY));
        listView.setAdapter(new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_list_item_1, al));
        //ShoppingListAdapter slAdapter = new ShoppingListAdapter();
        return rootView;
    }

    private List<ShoppingList> getSavedShoppingLists() {
        FileInputStream fis = null;
        try {
            String jsonShoppingList = "";
            int content;

            fis = _context.openFileInput(Globals.SHOPPING_LIST_FILE_NAME);
            while ((content = fis.read()) != -1) {
                jsonShoppingList += (char)content;
            }
            Type listType = new TypeToken<ArrayList<ShoppingList>>(){}.getType();
            return new Gson().fromJson(jsonShoppingList, listType);
        } catch (IOException e) {
            return null;
        }
        finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ShoppingListAdapter extends BaseAdapter {
        private List<ShoppingList> _shoppingList;
        private Context _context;
        public ShoppingListAdapter(Context c) {
            _shoppingList = new ArrayList<ShoppingList>();
            _context = c;
        }

        public void setShoppingList(List<ShoppingList> sl) {
            if(sl == null) _shoppingList.clear();
            else _shoppingList.addAll(sl);
        }

        @Override
        public int getCount() {
            return _shoppingList.size();
        }

        @Override
        public Object getItem(int i) {
            return _shoppingList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            ViewHolderItem viewHolder;

            if (convertView == null) {
                LayoutInflater inflater = ((Activity) _context).getLayoutInflater();
                convertView = inflater.inflate(R.layout.list_item_base_shopping, viewGroup, false);

                viewHolder = new ViewHolderItem();
                viewHolder.listTitle = convertView.findViewById(R.id.list_title);
                viewHolder.ingredientNum = convertView.findViewById(R.id.ingredient_number);

                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolderItem) convertView.getTag();
            }

            ShoppingList sl = _shoppingList.get(i);
            if(sl != null) {
                viewHolder.listTitle.setText(sl.getListName());
                viewHolder.ingredientNum.setText(String.format(Locale.US,
                        getString(R.string.base_shopping_ingredient_num), sl.size()));
            }
            return convertView;
        }

        private class ViewHolderItem {
            TextView listTitle;
            TextView ingredientNum;
        }
    }
}