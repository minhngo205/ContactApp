package com.example.contactapp.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.contactapp.adapter.ContactAdapter;
import com.example.contactapp.databinding.ActivityMainBinding;
import com.example.contactapp.model.Contact;
import com.example.contactapp.viewmodel.MainActivityViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    
    private ActivityMainBinding activityMainBinding;
    private List<Contact> dataset;
    private ContactAdapter adapter;

    private MainActivityViewModel mainActivityViewModel;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View viewRoot = activityMainBinding.getRoot();
        setContentView(viewRoot);

        activityMainBinding.fab1.setOnClickListener(view -> {
            Log.d(TAG, "onCreate: "+dataset.toString());
            Intent addIntent = new Intent(MainActivity.this, AddActivity.class);
            startActivity(addIntent);
        });

        activityMainBinding.contactList.setLayoutManager(new LinearLayoutManager(this));

        mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        setObserve();

        activityMainBinding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().isEmpty()){
                    onActionEnd();
                    return;
                }
                dataset.clear();
                dataset.addAll(mainActivityViewModel.onSearch(charSequence.toString()));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void setObserve() {
        mainActivityViewModel.getListContact().observe(this, contacts -> {
            dataset = contacts;
            adapter = new ContactAdapter(dataset);
            activityMainBinding.contactList.setAdapter(adapter);
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        onActionEnd();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void onActionEnd(){
        dataset.clear();
        dataset.addAll(mainActivityViewModel.onAddRestart());
        adapter.notifyDataSetChanged();
    }
}