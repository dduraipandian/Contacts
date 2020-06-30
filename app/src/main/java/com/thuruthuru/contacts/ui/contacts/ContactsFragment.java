package com.thuruthuru.contacts.ui.contacts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.thuruthuru.contacts.R;
import com.thuruthuru.contacts.ui.frag.BaseContactsFragment;


public class ContactsFragment extends BaseContactsFragment {

    public ContactsFragment() {
        SEARCH_ENABLED = true;
        ONLY_FAV = false;
    }

    @Override
    public View getCustomView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_contacts, parent, false);
        ListView v = (ListView) root.findViewById(R.id.contactListView);
        TextView em = (TextView) root.findViewById(R.id.empty);
        v.setEmptyView(em);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setElevation(0);
        return root;
    }
}