package com.example.phonebook.view;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phonebook.R;
import com.example.phonebook.database.entities.Contact;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.CustomViewHolder> {
    private List<Contact> contactsList;

    public ContactsAdapter(List<Contact> contactsList) {
        this.contactsList = contactsList;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView dot;
        public TextView phone;
        public TextView category;

        public CustomViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            dot = view.findViewById(R.id.dot);
            phone = view.findViewById(R.id.phone);
            category = view.findViewById(R.id.category);
        }
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_list_row, parent, false);

        return new CustomViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        Contact contact = contactsList.get(position);
        holder.name.setText(contact.getName());
        holder.category.setText(contact.getCategory().name());
        holder.phone.setText(contact.getPhone());
        holder.dot.setText(Html.fromHtml("&#8226;"));
    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }
}