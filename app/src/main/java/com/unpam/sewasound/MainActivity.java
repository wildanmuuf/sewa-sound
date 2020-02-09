package com.unpam.sewasound;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.Gravity;
import android.view.SubMenu;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    NavigationView navigationView;
    View header;
    Menu nav_menu;
    FirebaseAuth auth;
    DatabaseReference dbRef;
    TextView hakAksesTextView, namaTextView ;
    ImageView imageProfile;
    String hakAkses;
    User user;
    FragmentManager fragmentManager;
    Fragment fragment = null;
    NetworkService network;
    SharedPreferences sharedHakAkses;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        initView();
        network.CheckConnection(this);
        MenuUser();
        navigationView.setNavigationItemSelectedListener(this);
        isLogin();
        Intent i = getIntent();
        if(getIntent() != null){
            String fragmentName = i.getStringExtra("profile");
            if(fragmentName != null){
                fragment = new ProfileActivity();
                callFragment(fragment);
            }else{
                if (savedInstanceState == null) {
                    fragment = new Root();
                    callFragment(fragment);
                }
            }
        }
    }



    public void MenuUser() {
        nav_menu.clear();
        network.progressDialog.dismiss();
        nav_menu.add("Beranda").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                fragment = new Root();
                callFragment(fragment);
                return false;
            }
        });
        final SubMenu trxMenu = nav_menu.addSubMenu("Transaksi");
        final SubMenu acoountMenu = nav_menu.addSubMenu("Kelola Akun");
        if (auth.getCurrentUser() != null) {
            String email = auth.getCurrentUser().getEmail();
            dbRef.child("Users").orderByChild("email").equalTo(email).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot val : dataSnapshot.getChildren()) {
                        User.setKey(val.getKey());
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference("photo/" + User.getKey());
                        PhotoOptions photoOptions = new PhotoOptions();
                        photoOptions.downloadImage(getApplicationContext(), storageReference, imageProfile, "Navigation Drawer");
                        user = val.getValue(User.class);
                        editor.putString("hakAkses", user.getHakAkses());
                        editor.apply();
                        namaTextView.setText(user.getName());
                        hakAksesTextView.setText(user.getHakAkses());

                        Intent broadcast1 = new Intent("penyewaan");
                        broadcast1.putExtra("idUser", val.getKey());
                        sendBroadcast(broadcast1);

                    }
                    trxMenu.clear();

                    if (user.getHakAkses().equals("Penyewa")) {
                        trxMenu.add("Pesanan Saya").setOnMenuItemClickListener((MenuItem item) -> {
                                    navigationView.setCheckedItem(item);
                                    fragment = new PesananSaya();
                                    callFragment(fragment);
                                    return false;
                        });
                    } else if (user.getHakAkses().equals("Pelapak")) {
                        trxMenu.add("Kelola Sound").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                navigationView.setCheckedItem(item);
                                Fragment fragment = new KelolaSound();
                                FragmentManager fragmentManager = getSupportFragmentManager();
                                fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
                                return false;
                            }
                        });
                        trxMenu.add("Daftar Pesanan").setOnMenuItemClickListener((MenuItem item) -> {
                            navigationView.setCheckedItem(item);
                            fragment = new DaftarPesanan();
                            callFragment(fragment);
                            return false;
                        });
                    }
                    acoountMenu.clear();
                    acoountMenu.add("Profile").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            navigationView.setCheckedItem(item);
                            Fragment fragment = new ProfileActivity();
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
                            return false;
                        }
                    });

                    acoountMenu.add("Logout").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            dbRef = FirebaseDatabase.getInstance().getReference();
                            Map<String, Object> tokenMap = new HashMap<>();
                            tokenMap.put("token-id", "");
                            dbRef.child("Users").child(User.getKey()).updateChildren(tokenMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    auth.signOut();
                                    File dir = new File(getFilesDir().getParent() + "/shared_prefs/");
                                    String fileHakAkses = "hak-akses";
                                    String fileEmailPassword = "email-password";
                                    getSharedPreferences(fileHakAkses.replace(".xml", ""), Context.MODE_PRIVATE).edit().clear().commit();
                                    new File(dir, fileHakAkses).delete();
                                    getSharedPreferences(fileEmailPassword.replace(".xml", ""), Context.MODE_PRIVATE).edit().clear().commit();
                                    new File(dir, fileEmailPassword).delete();

                                    finish();
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                }
                            });

                            return false;
                        }
                    });

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        } else {
            namaTextView.setText("Anda belum masuk");
            acoountMenu.add("Login").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    fragment = new LoginActivity();
                    callFragment(fragment);
                    return false;
                }
            });

            acoountMenu.add("Register").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    fragment = new RegisterActivity();
                    callFragment(fragment);
                    return false;
                }
            });
        }

        nav_menu.add("Tentang").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                fragment = new Tentang();
                callFragment(fragment);
                return false;
            }
        });
        nav_menu.add("Keluar").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new AlertDialog.Builder(MainActivity.this).
                        setTitle("KELUAR").
                        setMessage("Apakah anda yakin ingin keluar?.").setCancelable(false).
                        setPositiveButton("BATAL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setNegativeButton("YA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        System.exit(0);
                    }
                }).create().show();
                return false;
            }
        });
    }

    public void initView(){
        sharedHakAkses = getSharedPreferences("hak-akses", Context.MODE_PRIVATE);
        editor = sharedHakAkses.edit();
        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        nav_menu = navigationView.getMenu();
        header = navigationView.getHeaderView(0);
        imageProfile = (ImageView) header.findViewById(R.id.photoImage);
        hakAksesTextView = (TextView) header.findViewById(R.id.hakAksesHeader);
        namaTextView = (TextView)header.findViewById(R.id.namaHeader);
        network = new NetworkService();
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
    }
    public void isLogin() {


    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_container);
        if(!(fragment instanceof  OnBackPressedListner)|| !((OnBackPressedListner)fragment).onBackPressed()){
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.login) {

        } else if (id == R.id.register) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(Gravity.LEFT);
        return true;
    }
    private void callFragment(Fragment fragment) {
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    public interface OnBackPressedListner{
        boolean onBackPressed();
    }
}
