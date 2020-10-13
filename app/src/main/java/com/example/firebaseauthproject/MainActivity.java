package com.example.firebaseauthproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    Button enter;
    EditText email;
    EditText password;
    TextView register;

    FirebaseAuth auth;//параметры аунтефикации пользователя
    FirebaseUser fUser;// личные данные пользователя

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enter = (Button)findViewById(R.id.enterButton);
        email = (EditText)findViewById(R.id.textMail);
        password = (EditText)findViewById(R.id.textPassword);
        register = (TextView)findViewById(R.id.registr);

        auth = FirebaseAuth.getInstance();
        fUser = auth.getCurrentUser();

        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailText = email.getText().toString();
                String passwordText = password.getText().toString();
                if(!emailText.contains("@") || emailText.equals("") || passwordText.equals(""))
                {
                    return;
                }
                inputAccount(emailText, passwordText);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegActivity.class);
                startActivity(intent);
            }
        });


    }
    //функция которая выполняет вход с передаваемыми ей email и password
    private void inputAccount(String emailText, String passwordText)
    {
        auth.signInWithEmailAndPassword(emailText, passwordText)//выполняем попытку входа
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {//вешаем слушателя если есть пользователь с таким email и password
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        if(fUser.isEmailVerified())//проверяем подтвердил ли пользователь свой email
                        {
                            //если программа попала в этот if то авторизация выполненна успешно и можно двигаться дальше
                            Intent intent = new Intent(getApplicationContext(), SettingAccountActivity.class);
                            startActivity(intent);

                        }else{
                            //если программа попала в этот else то пользователь выполнил регистрацию, но не подтвердил email
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Подтвердите email по отправленной вам ссылке",
                                    Toast.LENGTH_LONG).show();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {//вешаем слушателя если есть пользователь не найден или какието другие технические пробемы
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "ошибка подключения", Toast.LENGTH_LONG).show();
            }
        });
    }
}
