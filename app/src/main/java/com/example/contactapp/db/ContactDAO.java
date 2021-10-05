package com.example.contactapp.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.contactapp.model.Contact;

import java.util.List;

@Dao
public interface ContactDAO {
    String queryAll = "SELECT * FROM Contact";
    String search = "SELECT * FROM Contact WHERE name LIKE :searchStr";
    @Query(queryAll)
    List<Contact> getAll();

    @Insert
    void insert(Contact... contacts);

    @Query(search)
    List<Contact> search(String searchStr);
}
