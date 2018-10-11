package com.dhwani.picassodemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mContactList;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Contact");
        mDatabase.keepSynced(true);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        mContactList = (RecyclerView) findViewById(R.id.contact_list);
        mContactList.setHasFixedSize(true);
        mContactList.setLayoutManager(new LinearLayoutManager(this));
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              /*  Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();  */
                startActivity(new Intent(MainActivity.this, AddContact.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Contact, ContactViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Contact, ContactViewHolder>(
                        Contact.class,
                        R.layout.contact_row,
                        ContactViewHolder.class,
                        mDatabase
                ) {
                    @Override
                    protected void populateViewHolder(ContactViewHolder viewHolder, Contact model, int position) {
                        viewHolder.setName(model.getName());
                        viewHolder.setAddress(model.getAddress());
                        viewHolder.setImage(MainActivity.this, model.getImage());
                    }
                };
        mContactList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public ContactViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView contact_name = (TextView) mView.findViewById(R.id.contact_name);
            contact_name.setText(name);
        }

        public void setAddress(String address) {
            TextView contact_address = (TextView) mView.findViewById(R.id.contact_address);
            contact_address.setText(address);
        }

        public void setImage(final Context ctx, final String image) {
            final ImageView contact_image = (ImageView) mView.findViewById(R.id.contact_image);
            Picasso.with(ctx).load(image).into(contact_image, new Callback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(image).into(contact_image);
                }
            });
        }
    }
}