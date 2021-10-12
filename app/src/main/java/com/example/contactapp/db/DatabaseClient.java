package com.example.contactapp.db;

import android.content.Context;

import androidx.room.Room;

public class DatabaseClient {
    private final Context mCtx;
    private static DatabaseClient instance;
    private final ContactDatabase contactDatabase;

    private DatabaseClient(Context mCtx) {
        this.mCtx = mCtx;

        //creating the app database with Room database builder
        //MyToDos is the name of the database
        contactDatabase = Room.databaseBuilder(mCtx, ContactDatabase.class, "contact_database").build();
    }

    public static synchronized DatabaseClient getInstance(Context mCtx) {
        if(instance==null){
            instance = new DatabaseClient(mCtx);
        }
        return instance;
    }

    public ContactDatabase getContactDatabase(){
        return contactDatabase;
    }
}
