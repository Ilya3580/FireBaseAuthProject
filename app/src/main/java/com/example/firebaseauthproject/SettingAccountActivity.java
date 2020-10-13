package com.example.firebaseauthproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SettingAccountActivity extends AppCompatActivity {
    String[] mas;
    ListView listView;
    TextView textView;

    FirebaseAuth auth;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_account);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        textView = (TextView)findViewById(R.id.textView);
        listView = (ListView)findViewById(R.id.listView);
        mas = new String[4];

        mas[0] = "Сменить имя пользователя";
        mas[1] = "Сменить фамилию пользователя";
        mas[2] = "Сменить email";
        mas[3] = "Сменить пароль";
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mas);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 3)
                {
                    setPassword();
                    return;
                }
                alertDialog(position);
            }
        });

        updateParametrs(user.getDisplayName(), user.getEmail());
    }

    private void alertDialog(final int id)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch (id)
        {
            case 0: builder.setTitle("Введите имя");break;
            case 1: builder.setTitle("Введите фамилию");break;
            case 2: builder.setTitle("Введите email");break;
            case 3: builder.setTitle("Введите пароль");break;
        }

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = input.getText().toString();
                UserProfileChangeRequest.Builder builderFireBase = new UserProfileChangeRequest.Builder();
                if(id <2)
                {
                    //здесь мы изменяем имя или фамилию пользователя
                    String namen = user.getDisplayName();
                    String[] nameAndSurname = namen.split("#");
                    nameAndSurname[id] = text;
                    builderFireBase.setDisplayName(nameAndSurname[0] + "#" + nameAndSurname[1]);
                    UserProfileChangeRequest u = builderFireBase.build();
                    user.updateProfile(u);
                    updateParametrs(nameAndSurname[0] + "#" + nameAndSurname[1], user.getEmail());
                }
                if(id == 2)
                {
                    setEmail(text);
                }

            }
        });

        builder.show();
    }

    private void setEmail(String email)
    {
        if(email.equals(user.getEmail()))
        {
            Toast.makeText(getApplicationContext(), "Введет не корректный email", Toast.LENGTH_LONG).show();
            return;
        }
        user.updateEmail(email)//меняем email
                .addOnCompleteListener(new OnCompleteListener<Void>() {//вешаем слушателя, который срабатывает если удалось изменить email
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())//если все пройдет успешно и удастся email поменяется то мы попадем в if иначе в else
                        {
                            user.sendEmailVerification()//отправляем письмо на измененный email для подтверждения
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {//вешаем слушателя, который срабатывает если удалось отправить письмо на email
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                dialogWindow("Вам отправленно ссылка на email. Для подтверждения перейдите по ней", true);

                                            }
                                        }

                                    });
                        }else{
                            Toast.makeText(getApplicationContext(), "Ошибка подключения", Toast.LENGTH_LONG).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {//вешаем слушателя, который срабатывает если не удалось изменить email
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Ошибка подключения", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void setPassword()
    {
        String email = user.getEmail();
        auth.sendPasswordResetEmail(email)//отправляем ссылку для смены пароля на email
                .addOnCompleteListener(new OnCompleteListener<Void>() {//вешаем слушателя, если получается отправить письмо
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {//проверяем отправилось ли письмо
                            dialogWindow("Вам отправленно письмо на email для сброса пароля", false);
                        }
                    }
                });
    }
    private void dialogWindow(String str, final boolean flag)
    {
        AlertDialog.Builder build = new AlertDialog.Builder(SettingAccountActivity.this);

        build.setMessage(str)
                .setCancelable(false)
                .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(flag) {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        }

                    }
                });

        AlertDialog alertDialog = build.create();
        alertDialog.show();

    }

    private void updateParametrs(String namen, String email)
    {
        String[] nameAndSurname = namen.split("#");
        textView.setText(email + "\n" + nameAndSurname[0] + "\n" + nameAndSurname[1]);
    }


}
