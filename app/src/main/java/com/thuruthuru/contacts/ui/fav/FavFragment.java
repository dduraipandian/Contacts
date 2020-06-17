package com.thuruthuru.contacts.ui.fav;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.thuruthuru.contacts.R;
import com.thuruthuru.contacts.ui.frag.BaseContactsFragment;


public class FavFragment extends BaseContactsFragment {

    public FavFragment() {
        SEARCH_ENABLED = false;
        ONLY_FAV = true;
    }

    @Override
    public View getCustomView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_fav, parent, false);
        ListView v = (ListView) root.findViewById(R.id.contactListView);
        TextView em = (TextView) root.findViewById(R.id.empty);
        em.setText("No favorites to display");
        v.setEmptyView(em);
        return root;
    }
}