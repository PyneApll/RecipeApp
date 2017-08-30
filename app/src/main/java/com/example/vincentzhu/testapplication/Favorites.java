package com.example.vincentzhu.testapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Favorites extends BaseActivity {
    private ArrayList<String> favorite_recipes, favorite_ingredients;
    private DatabaseReference mRoot;
    private DatabaseReference mFavorite;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private String userID;
    private String activityName;
    private ArrayAdapter<String> recipes_adapter;
    private ArrayAdapter<String> ingredients_adapter;

    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_favorites);
        super.onCreate(savedInstanceState);

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        activityName = this.getLocalClassName();
        favorite_recipes = new ArrayList<>();
        favorite_ingredients = new ArrayList<>();

        TabLayout tabLayout = new TabLayout(this);
        tabLayout.addTab(tabLayout.newTab().setText("Recipes"));
        tabLayout.addTab(tabLayout.newTab().setText("Ingredients"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        populateFavoriteRecipes();

        recipes_adapter = new ArrayAdapter<>(Favorites.this,
                android.R.layout.simple_list_item_1, favorite_recipes);

        ListView lv_fav_recipes = findViewById(R.id.lv_favorite_recipes);
        lv_fav_recipes.setAdapter(recipes_adapter);
        lv_fav_recipes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                displayRecipe(favorite_recipes.get(i));
            }
        });

        populateFavoriteIngredients();

        ingredients_adapter = new ArrayAdapter<>(Favorites.this,
                android.R.layout.simple_list_item_1, favorite_ingredients);

        ListView lv_fav_ingredients = findViewById(R.id.lv_fav_ingredients);
        lv_fav_ingredients.setAdapter(ingredients_adapter);
        lv_fav_ingredients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                displayIngredient(favorite_ingredients.get(i));
            }
        });
    }

    private void displayRecipe(String item) {
        Intent intent = new Intent(Favorites.this, RecipePage.class);
        intent.putExtra("SELECTED_ITEM", item);
        startActivity(intent);
    }

    private void displayIngredient(String item) {
        Intent intent = new Intent(Favorites.this, IngredientPage.class);
        intent.putExtra("SELECTED_INGREDIENT", item);
        startActivity(intent);
    }

    private void populateFavoriteRecipes() {
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        userID = user.getUid();
        mRoot = FirebaseDatabase.getInstance().getReference().child("Recipes");
        mRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (!favorite_recipes.contains(ds.getKey()) && ds.child("Favorited By").child(userID).exists()) {
                        favorite_recipes.add(ds.getKey());
                        recipes_adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mFavorite = FirebaseDatabase.getInstance().getReference().child(userID).child("Added Recipes").child("Recipes");
        mFavorite.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    if (!favorite_recipes.contains(ds.getKey()) && ds.child("Favorited By").child(userID).exists()) {
                        favorite_recipes.add(ds.getKey());
                        recipes_adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void populateFavoriteIngredients() {
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        userID = user.getUid();
        mRoot = FirebaseDatabase.getInstance().getReference().child("Ingredients");
        mRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.child("Favorited By").child(userID).exists()) {
                        favorite_ingredients.add(ds.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mFavorite = FirebaseDatabase.getInstance().getReference().child(userID).child("Added Ingredients").child("Ingredients");
        mFavorite.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    if (!favorite_ingredients.contains(ds.getKey()) && ds.child("Favorited By").child(userID).exists()) {
                        favorite_ingredients.add(ds.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
