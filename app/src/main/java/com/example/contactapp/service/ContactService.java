package com.example.contactapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.example.contactapp.IContactAidlInterface;
import com.example.contactapp.db.DatabaseClient;
import com.example.contactapp.model.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactService extends Service {
    private static final String TAG = "ContactService";
    private List<Contact> contactList;

    @Override
    public void onCreate() {
        super.onCreate();
        if(contactList==null){
            contactList = new ArrayList<>();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if("BIND_SERVICE".equals(intent.getAction())) return dbAidl.asBinder();
        return null;
    }

    private final IContactAidlInterface.Stub dbAidl = new IContactAidlInterface.Stub() {
        @Override
        public List<Contact> getAll() throws RemoteException {
            AsyncTask.execute(() -> contactList = DatabaseClient
                .getInstance(getApplicationContext())
                .getContactDatabase()
                .contactDAO()
                .getAll());
            return contactList;
//            return result;
        }

        @Override
        public void insertContact(Contact input) throws RemoteException {
            AsyncTask.execute(() -> DatabaseClient.getInstance(getApplicationContext())
                .getContactDatabase()
                .contactDAO()
                .insert(input));
            Log.d(TAG, "insertContact: "+contactList.toString());
        }

        @Override
        public void editContact(Contact input) throws RemoteException {
            Log.d(TAG, "editContact: input"+input.getId());
            AsyncTask.execute(() ->
                    DatabaseClient.getInstance(getApplicationContext())
                            .getContactDatabase()
                            .contactDAO()
                            .updateContact(input));
        }

        @Override
        public void deleteContact(Contact input) throws RemoteException {
            AsyncTask.execute(() ->
                    DatabaseClient.getInstance(getApplicationContext())
                            .getContactDatabase()
                            .contactDAO()
                            .deleteContact(input));
        }
    };

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "onTaskRemoved: called.");
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called.");
    }
}