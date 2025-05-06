package com.example.contactsapp;

import java.util.List;
import com.example.contactsapp.ContactInfo;

interface IContactService {
    List<ContactInfo> searchContacts(String name);
    void deleteDuplicatesContacts();
    boolean hasDuplicates();
}