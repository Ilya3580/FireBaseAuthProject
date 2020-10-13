package com.example.firebaseauthproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegActivity extends AppCompatActivity {
    Button buttonEnter;
    Button buttonRotate;
    EditText name;
    EditText surname;
    EditText email;
    EditText password;


    FirebaseAuth auth;
    FirebaseUser user;
    UserParametrs userParametrs;//класс который задает параметры пользователя


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        buttonEnter = (Button)findViewById(R.id.rightButton);
        buttonRotate = (Button)findViewById(R.id.leftButton);

        name = (EditText)findViewById(R.id.name);
        surname = (EditText)findViewById(R.id.surname);
        email = (EditText)findViewById(R.id.email);
        password = (EditText)findViewById(R.id.password);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        buttonRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        buttonEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameText = name.getText().toString();
                String surnameText = surname.getText().toString();
                String emailText = email.getText().toString();
                String passwordText = password.getText().toString();

                if(nameText.equals("") || surnameText.equals("") || emailText.equals("") || passwordText.equals(""))
                {
                    Toast.makeText(getApplicationContext(), "Не все параметры заданы", Toast.LENGTH_LONG).show();
                    return;
                }

                userParametrs = new UserParametrs(nameText, surnameText, emailText, passwordText);
                regUser();
            }
        });

    }

    private void regUser()
    {
        auth.createUserWithEmailAndPassword(userParametrs.getEmail(), userParametrs.getPassword())//регистрируем пользователя
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {//вешаем слушателя если удалось зарегистрировать пользователя
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        user.sendEmailVerification()//отправляем письмо с подтверждение email
                                .addOnCompleteListener(new OnCompleteListener<Void>() {//вешаем слушателя если удалось зарегистрировать пользователя
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        dialogWindow("Вам отправленно ссылка на email. Для подтверждения перейдите по ней");

                                        //далле мы сохраняем имя и фамилию пользователя в user.displayName
                                        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();
                                        builder.setDisplayName(userParametrs.getName() + "#" + userParametrs.getSurname());
                                        UserProfileChangeRequest u = builder.build();
                                        user.updateProfile(u);
                                    }
                                });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {//вешаем слушателя если что-то пошло не так
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("TAGA", e.getMessage());
            }
        });


    }

    private void dialogWindow(String str)
    {
        AlertDialog.Builder build = new AlertDialog.Builder(RegActivity.this);

        build.setMessage(str)
                .setCancelable(false)
                .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Intent intent = new Intent(RegActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });

        AlertDialog alertDialog = build.create();
        alertDialog.show();

    }
}
