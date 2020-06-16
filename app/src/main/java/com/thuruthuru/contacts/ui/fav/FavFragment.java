package com.thuruthuru.contacts.ui.fav;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        return root;
    }
}