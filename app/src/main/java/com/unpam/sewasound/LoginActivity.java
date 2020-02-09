package com.unpam.sewasound;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import android.app.FragmentTransaction;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends Fragment {

    EditText editTextPassword;
    EditText editTextEmail;
    TextInputLayout textInputLayoutEmail;
    TextInputLayout textInputLayoutPassword;
    Button buttonLogin;
    FirebaseAuth auth;
    DatabaseReference dbRef;
    TextView textViewCreateAccount;
    SharedPreferences sharedEmailPassword;
    SharedPreferences.Editor editor;
    ProgressDialog progressDialog;
    User user;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_login, container, false);
        getActivity().setTitle("Masuk");
        sharedEmailPassword = getActivity().getSharedPreferences("email-password", Context.MODE_PRIVATE);
        editor = sharedEmailPassword.edit();
        editTextEmail = (EditText) rootView.findViewById(R.id.emailLogin);
        textViewCreateAccount = (TextView) rootView.findViewById(R.id.link_register);
        editTextPassword = (EditText) rootView.findViewById(R.id.passwordLogin);
        textInputLayoutEmail = (TextInputLayout) rootView.findViewById(R.id.textInputEmailLogin);
        textInputLayoutPassword = (TextInputLayout) rootView.findViewById(R.id.textInputPasswordLogin);
        buttonLogin = (Button) rootView.findViewById(R.id.btn_login);
        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        initCreateAccountTextView();
        Login();
        //set click event of login button
        return rootView;
    }

    public void Login(){
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    final String emailUsername = editTextEmail.getText().toString();
                    final String password = editTextPassword.getText().toString();
                    progressDialog = new ProgressDialog(getActivity(), R.style.Theme_AppCompat_DayNight_Dialog);
                    progressDialog.setMessage("Proses Login...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailUsername).matches()) {
                                signInByEmail(emailUsername, password);
                            }else{
                                dbRef.child("Users").orderByChild("username").equalTo(emailUsername).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()) {
                                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                user = ds.getValue(User.class);
                                                editor.putString("idUser", ds.getKey());
                                                dbRef = FirebaseDatabase.getInstance().getReference();
                                                String tokenId = FirebaseInstanceId.getInstance().getToken();
                                                Map<String, Object> tokenMap = new HashMap<>();
                                                tokenMap.put("token-id", tokenId);
                                                dbRef.child("Users").child(ds.getKey()).updateChildren(tokenMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        signInByEmail(user.getEmail(), password);
                                                    }
                                                });

                                            }
                                        }else{
                                            progressDialog.dismiss();
                                            Snackbar.make(buttonLogin, "Login gagal! Username/Password salah", Snackbar.LENGTH_LONG).show();
                                            editTextPassword.setText("");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }

                        }
                    }, 1200);

                }
            }
        });
    }

    private void signInByEmail(final String email, final String password){
        if(getActivity() !=null) {
            if(!email.isEmpty() || !password.isEmpty()) {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {

                            editor.putString("email", email);
                            editor.putString("password", password);
                            editor.apply();
                            getActivity().finish();
                            getActivity().startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
                            Snackbar.make(buttonLogin, "Login Berhasil", Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(buttonLogin, "Login gagal! Username/Password salah", Snackbar.LENGTH_LONG).show();
                            editTextPassword.setText("");
                        }
                    }
                });
            }else{
                Snackbar.make(buttonLogin, "Isi format log in dengan benar", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    //this method used to set Create account TextView text and click event( maltipal colors
    // for TextView yet not supported in Xml so i have done it programmatically)
    private void initCreateAccountTextView() {

        textViewCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new RegisterActivity();
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();

            }
        });
    }


    //this method is used to connect XML views to its Objects
    private void initViews() {

    }

    //This method is for handling fromHtml method deprecation
    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    //This method is used to validate input given by user
    public boolean validate() {
        boolean valid = false;

        //Get values from EditText fields
        String Email = editTextEmail.getText().toString();
        String Password = editTextPassword.getText().toString();

        //Handling validation for Email field
        if(Email.isEmpty()){
            valid= false;
            textInputLayoutEmail.setError("Username atau email tidak boleh kosong");
        }else{
            valid = true;
            textInputLayoutEmail.setError(null);
        }

        //Handling validation for Password field
        if (Password.isEmpty()) {
            valid = false;
            textInputLayoutPassword.setError("Password tidak boleh kosong");
        } else {
            if (Password.length() > 5) {
                valid = true;
                textInputLayoutPassword.setError(null);
            } else {
                valid = false;
                textInputLayoutPassword.setError("Password terlalu pendek!");
            }
        }

        return valid;
    }
}
