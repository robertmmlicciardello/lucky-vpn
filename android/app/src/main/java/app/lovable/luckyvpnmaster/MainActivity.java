
package app.lovable.luckyvpnmaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import app.lovable.luckyvpnmaster.fragments.HomeFragment;
import app.lovable.luckyvpnmaster.fragments.ServersFragment;
import app.lovable.luckyvpnmaster.fragments.RewardsFragment;
import app.lovable.luckyvpnmaster.fragments.ProfileFragment;
import app.lovable.luckyvpnmaster.auth.AuthManager;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigation;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authManager = new AuthManager(this);
        
        // Check if user is logged in
        if (!authManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupBottomNavigation();
        
        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (itemId == R.id.nav_servers) {
                fragment = new ServersFragment();
            } else if (itemId == R.id.nav_rewards) {
                fragment = new RewardsFragment();
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }
            
            return loadFragment(fragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
            return true;
        }
        return false;
    }
}
