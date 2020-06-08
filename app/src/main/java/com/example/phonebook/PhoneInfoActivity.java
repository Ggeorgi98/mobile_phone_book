package com.example.phonebook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phonebook.database.DatabaseHelper;
import com.example.phonebook.database.entities.Contact;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class PhoneInfoActivity extends AppCompatActivity {
    protected String ID;
    protected TextView name, phone, category, description;
    private DatabaseHelper dbHelper;
    private ExecutorService threadpool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_info);
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        category = findViewById(R.id.category);
        description = findViewById(R.id.description);
        threadpool = Executors.newCachedThreadPool();

        dbHelper = new DatabaseHelper(this);

        Bundle mainBundle = getIntent().getExtras();
        if(mainBundle != null){
            ID = mainBundle.getString("ID");
            Future<Long> futureTask = (Future<Long>) threadpool.submit(new Runnable() {
                @Override
                public void run() {
                    Contact contact = dbHelper.getContact(Integer.parseInt(ID));
                    name.setText(contact.getName());
                    phone.setText(contact.getPhone());
                    description.setText(contact.getDescription());
                    category.setText(contact.getCategory().name());
                }
            });
        }
    }
}
