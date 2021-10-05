package com.example.contactapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.example.contactapp.IContactAidlInterface;
import com.example.contactapp.db.ContactDAO;
import com.example.contactapp.db.ContactDatabase;
import com.example.contactapp.db.DatabaseClient;
import com.example.contactapp.model.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactService extends Service {
    private static final String TAG = "ContactService";
    private List<Contact> contactList;

    private ContactDatabase database;
    private ContactDAO contactDAO;

    @Override
    public void onCreate() {
        super.onCreate();
        if(contactList==null){
            contactList = new ArrayList<>();
        }
//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return dbAidl.asBinder();
    }

    private final IContactAidlInterface.Stub dbAidl = new IContactAidlInterface.Stub() {
        @Override
        public List<Contact> getAll() throws RemoteException {
//            List<Contact> result = new ArrayList<>();
//            result = contactList = DatabaseClient
//                        .getInstance(getApplicationContext())
//                        .getContactDatabase()
//                        .contactDAO()
//                        .getAll();
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    contactList = DatabaseClient
                        .getInstance(getApplicationContext())
                        .getContactDatabase()
                        .contactDAO()
                        .getAll();
                }
            });
            return contactList;
//            return result;
        }

        @Override
        public void insertContact(Contact input) throws RemoteException {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    DatabaseClient.getInstance(getApplicationContext())
                        .getContactDatabase()
                        .contactDAO()
                        .insert(input);
                }
            });
            Log.d(TAG, "insertContact: "+contactList.toString());
        }

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

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

//    private void saveContact() {
//        class AddContact extends AsyncTask<Void, Void, Void> {
//
//            @Override
//            protected Void doInBackground(Void... voids) {
//                Contact test = new Contact(
//                        "Ngô Hoàng Minh",
//                        "0983912847",
//                        "ngohoangminh205@gmail.com"
//                );
//                DatabaseClient.getInstance(getApplicationContext())
//                        .getContactDatabase()
//                        .contactDAO()
//                        .insert(test);
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void unused) {
//                super.onPostExecute(unused);
//            }
//        }
//        AddContact add = new AddContact();
//        add.execute();
//    }
//
//    private void getContacts() {
//        class GetContacts extends AsyncTask<Void,Void,List<Contact>>{
//            @Override
//            protected List<Contact> doInBackground(Void... voids) {
//                contactList = DatabaseClient
//                        .getInstance(getApplicationContext())
//                        .getContactDatabase()
//                        .contactDAO()
//                        .getAll();
//
//                return contactList;
//            }
//
//            @Override
//            protected void onPostExecute(List<Contact> contacts) {
//                super.onPostExecute(contacts);
//                contactList = contacts;
//            }
//        }
//
//        GetContacts getContacts = new GetContacts();
//        getContacts.execute();
//    }
}