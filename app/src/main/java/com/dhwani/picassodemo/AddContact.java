package com.dhwani.picassodemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class AddContact extends AppCompatActivity implements View.OnClickListener {

    private ImageView imageSelect;
    private EditText nameField;
    private EditText addressField;
    private Button addContactBtn;

    private Uri imageUri = null;
    private ProgressDialog progressDialog;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;

    private static final int GALLERY_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        imageSelect = findViewById(R.id.imageSelect);
        nameField = findViewById(R.id.nameField);
        addressField = findViewById(R.id.addressField);
        addContactBtn = findViewById(R.id.addContactBtn);

        progressDialog = new ProgressDialog(this);

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Contact");

        imageSelect.setOnClickListener(AddContact.this);
        addContactBtn.setOnClickListener(AddContact.this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.imageSelect:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);

                break;

            case R.id.addContactBtn:
                startAdding();
                break;
        }

    }

    private void startAdding() {
        final String name_val = nameField.getText().toString().trim();
        final String address_val = addressField.getText().toString().trim();

        if (!TextUtils.isEmpty(name_val) && !TextUtils.isEmpty(address_val) && imageUri != null) {
            progressDialog.setTitle("Adding Contact...");
            progressDialog.show();

            StorageReference filepath = storageReference.child("Contact_Images").child(imageUri.getLastPathSegment());

            filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final Uri downloadUri = Uri.parse(taskSnapshot.getMetadata().getReference().getDownloadUrl().toString());
                    final DatabaseReference newPost = databaseReference.push();

                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            newPost.child("name").setValue(name_val);
                            newPost.child("address").setValue(address_val);

                            newPost.child("image").setValue(downloadUri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        startActivity(new Intent(AddContact.this, MainActivity.class));
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    progressDialog.dismiss();

                    Toast.makeText(AddContact.this, "Adding Complete...", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                }
            });
        } else {
            Toast.makeText(this, "Failed....", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imageSelect.setImageURI(imageUri);
        }
    }
}
