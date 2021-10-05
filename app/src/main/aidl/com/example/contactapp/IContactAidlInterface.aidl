// IContactAidlInterface.aidl
package com.example.contactapp;

// Declare any non-default types here with import statements
import com.example.contactapp.model.IContact;

interface IContactAidlInterface {
    List<Contact> getAll();
    void insertContact(in Contact input);
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
}