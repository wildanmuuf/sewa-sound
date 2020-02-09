package com.unpam.sewasound;

import androidx.fragment.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class RegisterActivity extends Fragment {
    EditText editTextName;
    EditText editTextUserName;
    EditText editTextEmail;
    EditText editTextPassword;

    TextView textLogin;

    //Declaration TextInputLayout
    TextInputLayout textInputLayoutName;
    TextInputLayout textInputLayoutUserName;
    TextInputLayout textInputLayoutEmail;
    TextInputLayout textInputLayoutPassword;

    //Declaration Spinner
    Spinner hakAksesSpinner;
    String hakAkses = "";
    String[] hakAksesArray = {"-Daftar sebagai-","Penyewa", "Pelapak"};
    //Declaration Button
    Button buttonRegister;

    FirebaseAuth auth;
    DatabaseReference dbRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_register, container, false);
        hakAksesSpinner =  (Spinner) rootView.findViewById(R.id.hakAkses);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(),android.R.layout.simple_spinner_item, hakAksesArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hakAksesSpinner.setAdapter(adapter);

        getActivity().setTitle("Daftar");
        initViews(rootView);
        initTextViewLogin(rootView);
        setHakAkses();
        registerUser();
        return rootView;
    }

    public void setHakAkses(){
        hakAksesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                hakAkses = hakAksesArray[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getActivity(), "Silahkan pilih hak Akses", Toast.LENGTH_LONG);

            }
        });
    }

    public boolean valid(){
        boolean valid = false;
        String Name = editTextName.getText().toString();
        String UserName = editTextUserName.getText().toString();
        String Email = editTextEmail.getText().toString();
        String Password = editTextPassword.getText().toString();

        if(Name.isEmpty()){
            valid = false;
            textInputLayoutName.setError("Nama tidak boleh kosong");
        }else{
            valid = true;
            textInputLayoutName.setError(null);
        }

        //Handling validation for UserName field
        if (UserName.isEmpty()) {
            valid = false;
            textInputLayoutUserName.setError("Username tidak boleh kosong");
        } else {
            if (UserName.length() > 5) {
                valid = true;
                textInputLayoutUserName.setError(null);
            } else {
                valid = false;
                textInputLayoutUserName.setError("Username terlalu pendek! Minimal 5 karakter");
            }
        }

        //Handling validation for Email field
        if (Email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            valid = false;
            textInputLayoutEmail.setError("Silahkan masukan format email dengan benar!");
        } else {
            valid = true;
            textInputLayoutEmail.setError(null);
        }

        //Handling validation for Password field
        if (Password.isEmpty()) {
            valid = false;
            textInputLayoutPassword.setError("Silahkan masukan password");
        } else {
            if (Password.length() > 5) {
                valid = true;
                textInputLayoutPassword.setError(null);
            } else {
                valid = false;
                textInputLayoutPassword.setError("Password terlalu pendek");
            }
        }

        if(hakAksesSpinner.getSelectedItemPosition() ==0 ){
            valid = false;
            Snackbar.make(buttonRegister, "Silahkan pilih daftar sebagai apa!", Snackbar.LENGTH_LONG).show();
        }else{
            valid = true;
        }
        return valid;
    }



    public void registerUser(){
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog progressDialog = new ProgressDialog(getActivity(), R.style.Theme_AppCompat_DayNight_Dialog);
                progressDialog.setMessage("Proses mendaftar...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (valid() == true) {

                            final String Username = editTextUserName.getText().toString().trim();
                            final String Name = editTextName.getText().toString().trim();
                            final String Email = editTextEmail.getText().toString().trim();
                            final String Password = editTextPassword.getText().toString().trim();

                            final FragmentManager fm = getActivity().getSupportFragmentManager();
                            auth.createUserWithEmailAndPassword(Email, Password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressDialog.dismiss();
                                    if (!task.isSuccessful()) {
                                        Snackbar.make(buttonRegister, "Register gagal. Pesan : " + task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                                    } else {
                                        final User user = new User(Email, Password, Name, Username, hakAkses);
                                        final String userId = dbRef.push().getKey().toString();
                                        dbRef.child(userId).setValue(user).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (!task.isSuccessful()) {
                                                    Snackbar.make(buttonRegister, "Register gagal. Pesan : " + task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                                                } else {
                                                    Snackbar.make(buttonRegister, "Register Berhasil", Snackbar.LENGTH_LONG).show();
                                                    LoginActivity loginActivity = new LoginActivity();
                                                    Fragment fragment = loginActivity;
                                                    fm.beginTransaction()
                                                            .replace(R.id.frame_container, fragment)
                                                            .commit();
                                                }
                                            }
                                        });

                                    }
                                }
                            });
                        }else{
                            progressDialog.dismiss();
                        }
                    }
                }, 1000);
            }
        });
    }
    //this method used to set Login TextView click event
    private void initTextViewLogin(View view) {
        textLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new LoginActivity();
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
            }
        });
    }

    //this method is used to connect XML views to its Objects
    private void initViews(View view) {
        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("Users");
        textLogin = view.findViewById(R.id.link_login);
        editTextName = (EditText) view.findViewById(R.id.nama);
        editTextEmail = (EditText) view.findViewById(R.id.email);
        editTextPassword = (EditText) view.findViewById(R.id.password);
        editTextUserName = (EditText) view.findViewById(R.id.username);
        textInputLayoutName = (TextInputLayout) view.findViewById(R.id.textInputNama);
        textInputLayoutEmail = (TextInputLayout) view.findViewById(R.id.textInputEmail);
        textInputLayoutPassword = (TextInputLayout) view.findViewById(R.id.textInputPassword);
        textInputLayoutUserName = (TextInputLayout) view.findViewById(R.id.textInputUsername);
        buttonRegister = (Button) view.findViewById(R.id.btn_register);

    }

}
