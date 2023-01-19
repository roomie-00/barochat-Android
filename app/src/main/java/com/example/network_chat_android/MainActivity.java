package com.example.network_chat_android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class MainActivity extends AppCompatActivity {
    private String nickname, address = "";
    private EditText editNick;
    private RadioButton address101, address102, address103, address104;
    private Button btnEnter, btnExit;

    // 처음 닉네임 및 ip, 포트 번호 입력 화면
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        address101 = (RadioButton)findViewById(R.id.radio101);
        address102 = (RadioButton)findViewById(R.id.radio102);
        address103 = (RadioButton)findViewById(R.id.radio103);
        address104 = (RadioButton)findViewById(R.id.radio104);
        editNick = (EditText)findViewById(R.id.edit_nick);

        btnEnter = (Button) findViewById(R.id.btn_enter);
        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nickname = String.valueOf(editNick.getText());
                if(!nickname.equals("")) {
                    if(nickname.length() > 8) {
                        Toast.makeText(getApplicationContext(),
                                "닉네임을 8글자 이하로 설정해주세요.", Toast.LENGTH_SHORT).show();
                    } else {
                        if(address101.isChecked()){
                            address = "101";
                        } else if(address102.isChecked()) {
                            address = "102";
                        } else if(address103.isChecked()) {
                            address = "103";
                        } else if(address104.isChecked()) {
                            address = "104";
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "동 번호를 선택해주세요.", Toast.LENGTH_SHORT). show();
                        }

                        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                        intent.putExtra("nickname", nickname);
                        intent.putExtra("address", address);
                        startActivity(intent);
                    }
                }
            }
        });

        btnExit = (Button)findViewById(R.id.btn_exit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}