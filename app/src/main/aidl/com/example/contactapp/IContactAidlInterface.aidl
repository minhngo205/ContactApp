// IContactAidlInterface.aidl
package com.example.contactapp;

// Declare any non-default types here with import statements
import com.example.contactapp.model.IContact;

interface IContactAidlInterface {
    List<Contact> getAll();
    void insertContact(in Contact input);
    void editContact(in Contact input);
    void deleteContact(in Contact input);
}