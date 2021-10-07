package com.example.contactapp.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.contactapp.model.Contact;

import java.util.List;

@Dao
public interface ContactDAO {
    String queryAll = "SELECT * FROM Contact ORDER BY name ASC";
    String search = "SELECT * FROM Contact WHERE name LIKE :searchStr";
    @Query(queryAll)
    List<Contact> getAll();

    @Delete
    void deleteContact(Contact... contacts);

    @Insert
    void insert(Contact... contacts);

    @Query(search)
    List<Contact> search(String searchStr);

    @Update
    void updateContact(Contact... contacts);
}
