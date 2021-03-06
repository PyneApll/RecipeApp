package com.bizdev.recipeapp.cookitup;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;

/**
 * This is an abstract base activity to be extended by all regular activities.
 * It handles Firebase authentication and the creation of the toolbar and overflow menu
 * for every activity that extends it.
 * <p>
 * NOTE: The onCreate() method for subclass activities must call the setContentView() method
 * BEFORE calling the super.onCreate() method so that getSupportActionBar() does not
 * return a null reference.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected FirebaseAuth firebaseAuth;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        // Check if user is still logged in. If not, return to Login activity.
        if (firebaseAuth.getCurrentUser() == null && firebaseAuth.getCurrentUser()
                .isEmailVerified()) {
            //Profile activity here
            finish();
            startActivity(new Intent(getApplicationContext(),
                    Login.class));
        }

        // Create the toolbar and set it as the app bar for the activity
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar and enable Up button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.save_ing:
                startActivity(new Intent(this, MyIngredients.class));
                return true;
            case R.id.save_recipe:
                startActivity(new Intent(this, MyRecipes.class));
                return true;
            case R.id.recipe_favorites:
                startActivity(new Intent(this, Favorites.class));
                return true;
            case R.id.action_home:
                // User chose the "Home" item, show the Home activity
                startActivity(new Intent(this, Home.class));
                return true;
            case R.id.action_about_us:
                // User chose the "About Us" item, show the About Us activity
                startActivity(new Intent(this, AboutUs.class));
                return true;
            case R.id.action_logout:
                // User chose the "Log Out" item, log the user out and return to Registration
                // activity
                firebaseAuth = FirebaseAuth.getInstance();
                firebaseAuth.signOut();
                startActivity(new Intent(this, Login.class));
                return true;
            default:
                // The user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }


}
