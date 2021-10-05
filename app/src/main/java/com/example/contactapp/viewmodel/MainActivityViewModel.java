package com.example.contactapp.viewmodel;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.contactapp.db.DatabaseClient;
import com.example.contactapp.model.Contact;

import java.util.ArrayList;
import java.util.List;

public class MainActivityViewModel extends AndroidViewModel {


    private MutableLiveData<List<Contact>> listContact;

    private final DatabaseClient database;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        database = DatabaseClient.getInstance(application);
    }


    public LiveData<List<Contact>> getListContact(){
        if(listContact==null){
            listContact = new MutableLiveData<>();
            listContact.setValue(getAllContact());
        }
        return listContact;
    }

    private List<Contact> getAllContact() {
        List<Contact> result = new ArrayList<>();
        AsyncTask.execute(() -> result.addAll(database.getContactDatabase().contactDAO().getAll()));
        return result;
    }

    private List<Contact> searchContact(String search) {
        List<Contact> result = new ArrayList<>();
        AsyncTask.execute(() -> result.addAll(database.getContactDatabase().contactDAO().search(search)));
        return result;
    }

    public List<Contact> onAddRestart(){
        listContact.setValue(getAllContact());
        return listContact.getValue();
    }

    public List<Contact> onSearch(String search){
        String searchQuery = "%"+search+"%";
        listContact.setValue(searchContact(searchQuery));
        return listContact.getValue();
    }
}