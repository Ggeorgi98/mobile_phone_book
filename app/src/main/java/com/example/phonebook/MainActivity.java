package com.example.phonebook;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.example.phonebook.database.DatabaseHelper;
import com.example.phonebook.database.entities.Contact;
import com.example.phonebook.utils.RecyclerTouchListenerHelper;
import com.example.phonebook.view.ContactsAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.SystemClock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    private ContactsAdapter contactsAdapter;
    private List<Contact> contacts = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView noContactsView;
    private DatabaseHelper dbHelper;
    private ExecutorService threadPool;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);
        noContactsView = findViewById(R.id.no_contacts_view);
        dbHelper = new DatabaseHelper(this);
        contacts.addAll(dbHelper.getAllContacts());
        threadPool = Executors.newCachedThreadPool();
        loadingDialog = new LoadingDialog(MainActivity.this);

        FloatingActionButton fab = findViewById(R.id.addNew);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showContactDialog(false, null, -1);
            }
        });

        contactsAdapter = new ContactsAdapter(contacts);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(contactsAdapter);

        toggleEmptyContacts();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListenerHelper(this,
                recyclerView, new RecyclerTouchListenerHelper.IClickListener() {
            @Override
            public void onClick(View view, final int position) {
                Intent phoneInfoIntent = new Intent(MainActivity.this, PhoneInfoActivity.class);
                Bundle phoneInfoBundle = new Bundle();

                Contact contact = contacts.get(position);
                if(contact == null){
                    return;
                }
                int ID = contact.getId();
                phoneInfoBundle.putString("ID", String.valueOf(ID));

                phoneInfoIntent.putExtras(phoneInfoBundle);
                startActivityForResult(phoneInfoIntent, 200, phoneInfoBundle);
            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(view, position);
            }
        }));
    }

    private void createContact(String name, String phone, String description, Contact.Category category) {
        startLoadingBar();
        Contact contactToAdd = new Contact(0, name, phone, description, category);
        if(dbHelper.checkIfContactWithNameAndPhoneExist(0, name, phone)){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingDialog.hideLoadingDialog();
                    Toast.makeText(getApplicationContext(), "There is already a contact with this name and phone",
                            Toast.LENGTH_LONG).show();
                }
            });

            return;
        }

        long id = 0;
        try{
            id = dbHelper.createContact(contactToAdd);
        }catch (final Exception ex){
            hideLoadingBar();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), ex.getLocalizedMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        }

        if(id <= 0){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingDialog.hideLoadingDialog();
                    Toast.makeText(getApplicationContext(), "There was a problem saving the contact",
                            Toast.LENGTH_LONG).show();
                }
            });

            return;
        }

        final long finalId = id;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Contact contact = dbHelper.getContact(finalId);

                if (contact != null) {
                    contacts.clear();
                    loadingDialog.hideLoadingDialog();
                    contacts.addAll(dbHelper.getAllContacts());

                    contactsAdapter.notifyDataSetChanged();

                    toggleEmptyContacts();
                }
            }
        });
    }

    private void updateContact(String name, String phone, String description,
                               Contact.Category category, int position) {
        startLoadingBar();
        //uncomment the line with SystemClock if you want to see the loading bar because it usually disappears too quickly
        /*SystemClock.sleep(7000);*/
        Contact contact = contacts.get(position);
        contact.setName(name);
        contact.setPhone(phone);
        contact.setDescription(description);
        contact.setCategory(category);
        try{
            dbHelper.updateContact(contact);
        }catch (final Exception ex){
            hideLoadingBar();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), ex.getLocalizedMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        }
        finally {
            dbHelper.close();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                contacts.clear();
                loadingDialog.hideLoadingDialog();
                contacts.addAll(dbHelper.getAllContacts());
                contactsAdapter.notifyDataSetChanged();

                toggleEmptyContacts();
            }
        });
    }

    private void deleteContact(Contact contact, final int position) {
        startLoadingBar();
        dbHelper.deleteContact(contact);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingDialog.hideLoadingDialog();
                contacts.remove(position);
                contactsAdapter.notifyItemRemoved(position);

                toggleEmptyContacts();
                Toast.makeText(MainActivity.this, "Successfully deleted contact!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showActionsDialog(View view, final int position) {
        Contact contact = contacts.get(position);

        CharSequence[] options = new CharSequence[]{"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose option");
        final Contact finalContact = contact;
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int clicked) {
                if (clicked == 0) {
                    showContactDialog(true, finalContact, position);
                } else {
                    try {
                        Future<Long> futureTask = (Future<Long>) threadPool.submit(new Runnable() {
                            @Override
                            public void run() {
                                deleteContact(finalContact, position);
                            }
                        });

                    } catch (final Exception ex) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), ex.getLocalizedMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                        hideLoadingBar();
                    }
                }
            }
        });
        builder.show();
    }

    private void showContactDialog(final boolean shouldUpdate, final Contact contact, final int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
        View view = layoutInflater.inflate(R.layout.add_edit_contact, null);

        AlertDialog.Builder alertDialogBuilderContactInputs = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilderContactInputs.setView(view);

        final EditText editName = view.findViewById(R.id.name);
        final EditText editPhone = view.findViewById(R.id.phone);
        final EditText editDescription = view.findViewById(R.id.description);
        final Spinner editCategory = view.findViewById(R.id.category);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText(!shouldUpdate ? getString(R.string.lbl_new_contact_title) :
                getString(R.string.lbl_edit_contact_title));

        if (shouldUpdate && contact != null) {
            editName.setText(contact.getName());
            editPhone.setText(contact.getPhone());
            editDescription.setText(contact.getDescription());
            editCategory.setSelection(((ArrayAdapter)editCategory.getAdapter())
                    .getPosition(contact.getCategory().name()));
        }
        alertDialogBuilderContactInputs
                .setCancelable(false)
                .setPositiveButton(shouldUpdate ? "update" : "save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                .setNegativeButton("cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilderContactInputs.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(editName.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Enter Owner's name!", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (TextUtils.isEmpty(editPhone.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Enter phone!", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (TextUtils.isEmpty(editCategory.getSelectedItem().toString())) {
                    Toast.makeText(MainActivity.this, "Choose category!", Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    alertDialog.dismiss();
                }

                if (shouldUpdate && contact != null) {
                    Future<Long> futureTask = (Future<Long>) threadPool.submit(new Runnable() {
                        @Override
                        public void run() {
                            updateContact(editName.getText().toString(), editPhone.getText().toString(),
                                    editDescription.getText().toString(),
                                    Contact.Category.valueOf(editCategory.getSelectedItem().toString()),
                                    position);
                        }
                    });
                } else {
                    Future<Long> futureTask = (Future<Long>) threadPool.submit(new Runnable() {
                        @Override
                        public void run() {
                            createContact(editName.getText().toString(), editPhone.getText().toString(),
                                    editDescription.getText().toString(),
                                    Contact.Category.valueOf(editCategory.getSelectedItem().toString()));
                        }
                    });
                }
            }
        });
    }

    private void toggleEmptyContacts() {
        if (dbHelper.getContactsCount() > 0) {
            noContactsView.setVisibility(View.GONE);
        } else {
            noContactsView.setVisibility(View.VISIBLE);
        }
    }

    private void startLoadingBar(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingDialog.startLoadingDialog();
            }
        });
    }

    private void hideLoadingBar(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingDialog.hideLoadingDialog();
            }
        });
    }
}
