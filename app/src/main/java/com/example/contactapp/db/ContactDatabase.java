package com.example.contactapp.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.contactapp.model.Contact;

@Database(entities = Contact.class, version = 1)
public abstract class ContactDatabase extends RoomDatabase {
    public abstract ContactDAO contactDAO();
}
