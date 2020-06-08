package com.example.phonebook.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.phonebook.database.entities.Contact;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends  SQLiteOpenHelper{
    private static final String DATABASE_NAME = "contacts_db";

    public DatabaseHelper(Context dbContext) {
        super(dbContext, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Contact.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Contact.TABLE_NAME);
        onCreate(db);
    }

    public long createContact(Contact contact) throws NullPointerException {
        if(contact == null){
            throw new NullPointerException();
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Contact.COLUMN_NAME, contact.getName());
        values.put(Contact.COLUMN_PHONE, contact.getPhone());
        values.put(Contact.COLUMN_DESCRIPTION, contact.getDescription());
        values.put(Contact.COLUMN_CATEGORY, contact.getCategory().name());

        long id = db.insert(Contact.TABLE_NAME, null, values);
        db.close();

        return id;
    }

    public Contact getContact(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Contact.TABLE_NAME,
                new String[]{Contact.COLUMN_ID, Contact.COLUMN_NAME, Contact.COLUMN_PHONE,
                        Contact.COLUMN_DESCRIPTION, Contact.COLUMN_CATEGORY},
                Contact.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, Contact.COLUMN_NAME, null);

        if (cursor != null)
            cursor.moveToFirst();

        Contact contact = new Contact(
                cursor.getInt(cursor.getColumnIndex(Contact.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(Contact.COLUMN_NAME)),
                cursor.getString(cursor.getColumnIndex(Contact.COLUMN_PHONE)),
                cursor.getString(cursor.getColumnIndex(Contact.COLUMN_DESCRIPTION)),
                Contact.Category.valueOf(cursor.getString(cursor.getColumnIndex(Contact.COLUMN_CATEGORY))));

        cursor.close();
        db.close();

        return contact;
    }

    public List<Contact> getAllContacts() {
        List<Contact> contacts = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + Contact.TABLE_NAME + " ORDER BY " + Contact.COLUMN_NAME + " ASC ";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact();
                contact.setId(cursor.getInt(cursor.getColumnIndex(Contact.COLUMN_ID)));
                contact.setName(cursor.getString(cursor.getColumnIndex(Contact.COLUMN_NAME)));
                contact.setPhone(cursor.getString(cursor.getColumnIndex(Contact.COLUMN_PHONE)));
                contact.setDescription(cursor.getString(cursor.getColumnIndex(Contact.COLUMN_DESCRIPTION)));
                contact.setCategory(Contact.Category.valueOf(cursor
                        .getString(cursor.getColumnIndex(Contact.COLUMN_CATEGORY))));

                contacts.add(contact);
            } while (cursor.moveToNext());
        }
        db.close();

        return contacts;
    }

    public int getContactsCount() {
        String countQuery = "SELECT * FROM " + Contact.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    public void updateContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Contact.COLUMN_NAME, contact.getName());
        values.put(Contact.COLUMN_PHONE, contact.getPhone());
        values.put(Contact.COLUMN_DESCRIPTION, contact.getDescription());
        values.put(Contact.COLUMN_CATEGORY, contact.getCategory().name());

        db.update(Contact.TABLE_NAME, values, Contact.COLUMN_ID + " = ?",
                new String[]{String.valueOf(contact.getId())});
    }

    public void deleteContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Contact.TABLE_NAME, Contact.COLUMN_ID + " = ?",
                new String[]{String.valueOf(contact.getId())});

        db.close();
    }

    public boolean checkIfContactWithNameAndPhoneExist(long ownerId, String name, String phone) {
        Contact contact = new Contact();

        String selectQuery = "SELECT  * FROM " + Contact.TABLE_NAME + " WHERE " + Contact.COLUMN_ID + " != ? AND " +
                Contact.COLUMN_NAME + " = ? AND " + Contact.COLUMN_PHONE + " = ?";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(ownerId), name, phone});

        if (cursor.moveToFirst()) {
            do {
                contact.setId(cursor.getInt(cursor.getColumnIndex(Contact.COLUMN_ID)));
                contact.setName(cursor.getString(cursor.getColumnIndex(Contact.COLUMN_NAME)));
                contact.setPhone(cursor.getString(cursor.getColumnIndex(Contact.COLUMN_PHONE)));
                contact.setDescription(cursor.getString(cursor.getColumnIndex(Contact.COLUMN_DESCRIPTION)));
                contact.setCategory(Contact.Category.valueOf(cursor
                        .getString(cursor.getColumnIndex(Contact.COLUMN_CATEGORY))));

            } while (cursor.moveToNext());
        }

        db.close();

        return contact.getId() > 0;
    }
}
