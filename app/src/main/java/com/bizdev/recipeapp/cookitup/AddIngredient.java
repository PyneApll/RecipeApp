package com.bizdev.recipeapp.cookitup;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class AddIngredient extends BaseActivity {

    private static final int RESULT_IMAGE = 1;
    private static final int CAPTURE_CAMERA = 11;
    Spinner spinner_type;
    Spinner spinner_season;
    ArrayAdapter<CharSequence> adapter_ingredient_type;
    ArrayAdapter<CharSequence> adapter_ingredient_season;
    //user-related widgets
    private EditText mIngName;
    private EditText mIngDescription;
    private EditText mIngHistory;
    private ImageView imageDisplay;
    private Uri selectedImage;
    //database-related objects
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private StorageReference mStorage;
    private String userID;
    private DatabaseReference mRoot;
    private DatabaseReference mIngredients;
    private DatabaseReference mType_Ingredients;
    private String uid;
    private StorageReference uploadPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_add_ingredient);
        super.onCreate(savedInstanceState);

        //user-related display
        imageDisplay = findViewById(R.id.imageDisplay_ing);
        spinner_type = findViewById(R.id.spinner_ing_type);
        spinner_season = findViewById(R.id.spinner_ing_season);
        adapter_ingredient_type = ArrayAdapter.createFromResource
                (this, R.array.ingredient_types, android.R.layout.simple_spinner_item);
        adapter_ingredient_season = ArrayAdapter.createFromResource
                (this, R.array.ingredient_seasons, android.R.layout.simple_spinner_item);
        spinner_type.setAdapter(adapter_ingredient_type);
        spinner_season.setAdapter(adapter_ingredient_season);
        mIngName = findViewById(R.id.ing_name);
        mIngDescription = findViewById(R.id.ing_description);
        mIngHistory = findViewById(R.id.ing_history);

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));

        BottomNavigationView bottomNavigationView = findViewById(R.id.ing_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.pic_btn:
                                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                galleryIntent.setType("image/*");
                                startActivityForResult(galleryIntent, RESULT_IMAGE);
                                return true;
                            case R.id.firebase_ing_btn:
                                saveIngData();
                                return true;
                            case R.id.take_photo:
                                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(takePictureIntent, CAPTURE_CAMERA);
                                return true;
                            default:
                                return true;
                        }
                    }
                });

        //database-related
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();
        user = firebaseAuth.getCurrentUser();

        spinner_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinner_season.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void saveIngData() {

        String ingredientName = mIngName.getText().toString().trim();
        String ingredientDescription = mIngDescription.getText().toString().trim();
        String ingredientHistory = mIngHistory.getText().toString().trim();

        if (TextUtils.isEmpty(ingredientName)) {
            //ingredient name is empty
            Toast.makeText(this, "Please enter ingredient name", Toast.LENGTH_SHORT).show();
            //Stopping the function from executing
            return;
        }

        if (TextUtils.isEmpty(ingredientDescription)) {
            //Ingredient description is empty
            Toast.makeText(this, "Please enter ingredient description", Toast.LENGTH_SHORT).show();
            //Stopping the function from executing
            return;
        }

        //user tree
        mRoot = FirebaseDatabase.getInstance().getReference().child(userID)
                .child("Added Ingredients");
        ;
        mStorage = FirebaseStorage.getInstance().getReference().child("Ingredients");
        mIngredients = mRoot.child("Ingredients");
        mType_Ingredients = mRoot.child("Type_Ingredients");
        saveIngredient(ingredientName, ingredientDescription, ingredientHistory, false);

        //default tree
        mRoot = FirebaseDatabase.getInstance().getReference();
        mIngredients = mRoot.child("Ingredients");
        mType_Ingredients = mRoot.child("Type_Ingredients");
        saveIngredient(ingredientName, ingredientDescription, ingredientHistory, true);
    }

    public void saveIngredient(String ingredientName, String ingredientDescription,
                               String ingredientHistory, boolean admin) {
        mIngredients.child(ingredientName).child("Description").setValue(ingredientDescription);
        mIngredients.child(ingredientName).child("Type")
                .setValue(spinner_type.getSelectedItem().toString());
        mIngredients.child(ingredientName).child("History").setValue(ingredientHistory);
        mIngredients.child(ingredientName).child("Season")
                .setValue(spinner_season.getSelectedItem().toString());
        mType_Ingredients.child(spinner_type.getSelectedItem()
                .toString()).child(ingredientName).setValue(ingredientName);
        uploadFile(ingredientName, admin);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case CAPTURE_CAMERA:
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    imageDisplay.setImageBitmap(imageBitmap);
                    break;
                case RESULT_IMAGE:
                    selectedImage = data.getData();
                    imageDisplay.setImageURI(selectedImage);
                    break;
            }
        }
    }


    private void uploadFile(String ingredientName, boolean admin) {
        uid = UUID.randomUUID().toString();
        if (selectedImage != null) {
            if (!admin) {
                uploadPath = mStorage.child(user.getUid()).child(uid);
            } else {
                uploadPath = mStorage.child("lRxFd3PSkGNfeUfZ3qOfpSRoaO12").child(uid);
            }
            uploadPath.putFile(selectedImage).addOnSuccessListener
                    (new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(AddIngredient.this, "Upload Completed successfully", Toast.LENGTH_LONG).show();

                        }
                    });
            mIngredients.child(ingredientName).child("Image").setValue(uploadPath.toString());
        }
    }

}

