package com.example.phonebook.database.entities;

public class Contact {
    public static final String TABLE_NAME = "Contacts";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_PHONE = "Phone";
    public static final String COLUMN_DESCRIPTION = "Description";
    public static final String COLUMN_CATEGORY = "Category";

    private int id;
    private String name;
    private String phone;
    private String description;
    private Category category;

    public enum Category{
        Friends,
        Colleagues,
        Family,
        Others
    }

    public static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_NAME + " TEXT NOT NULL, "
                    + COLUMN_PHONE + " TEXT NOT NULL, "
                    + COLUMN_DESCRIPTION + " TEXT NULL, "
                    + COLUMN_CATEGORY + " TEXT NOT NULL,"
                    + " unique(" + COLUMN_NAME + ", "+ COLUMN_PHONE + ") "
                    + ")";

    public Contact() {
    }

    public Contact(int id, String name, String phone, String description, Category category) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.description = description;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
