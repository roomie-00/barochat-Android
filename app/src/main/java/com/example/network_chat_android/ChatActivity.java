package com.example.network_chat_android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class ChatActivity extends AppCompatActivity {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream mychat;
    private String mymsg, msg, peoplenummsg;
    private String lastSender = ".";
    private String nickname, ip, address, chatname = "";
    private InetAddress ipAddress;
    private int portnum;


    private EditText msgEdit;
    private Button sendBtn;
    private ImageButton exitBtn;
    private TextView bubble, roomnameTv, senderTv, guideTv, peoplenumTv;
    private LinearLayout bubbleContainer, showbubbles;
    private ScrollView chatSV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ip = "192.168.19.246";

        // MainActivity.java에서 넘어온 데이터
        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        nickname = intent.getStringExtra("nickname");
        if(address.equals("101")) {
            portnum = 9996;
            chatname = "101동 채팅방";
        } else if (address.equals("102")) {
            portnum = 9997;
            chatname = "102동 채팅방";
        } else if (address.equals("103")) {
            portnum = 9998;
            chatname = "103동 채팅방";
        } else if (address.equals("104")) {
            portnum = 9999;
            chatname = "104동 채팅방";
        }

        msgEdit = (EditText)findViewById(R.id.messageEdit);
        sendBtn = (Button)findViewById(R.id.sendBtn);
        exitBtn = (ImageButton)findViewById(R.id.exitBtn);
        showbubbles = (LinearLayout) findViewById(R.id.chatLayout);
        roomnameTv = (TextView)findViewById(R.id.roomNameTv);
        guideTv = (TextView)findViewById(R.id.otherChatTv);
        chatSV = (ScrollView)findViewById(R.id.scrollView);

        // 소켓 연결 후, chatting 시작
        new Thread() {
            public void run() {
                connection();
                chatting();
            }
        }.start();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!String.valueOf(msgEdit. getText()).equals("")) {
                    // msgEdit TextView에 내용이 있다면 이를 보냄.
                    mymsg = String.valueOf(msgEdit.getText());
                    new Thread() {
                        public void run() {
                            sendMessage(mymsg);
                        }
                    }.start();
                    msgEdit.setText("");
                }
            }
        });

        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setMessage("채팅방을 나가시겠습니까?");
                builder.setTitle("나가기")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    socket.close();
                                    finish();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.setTitle("나가기");
                alert.show();
            }
        });
    }

    // 소켓 통신 연결 부분
    private void connection() {
        try {
            if(portnum != 0) {
                socket = new Socket(ip, portnum);
            }
            System.out.println("서버 연결 완료");

            mychat = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            mychat.writeUTF(nickname);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    guideTv.setVisibility(View.GONE);
                    roomnameTv.setText(chatname);
                }
            });
        } catch (IOException exception) {
            System.out.println("connection 연결 문제일까?");
            exception.printStackTrace();
        }
    }

    private void chatting() {
        try {
            // 상대방 말풍선용 layout 설정
            LinearLayout.LayoutParams bubbleparams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            bubbleparams.setMargins(30, 4, 30, 4);

            // 내 말풍선용 layout 설정
            LinearLayout.LayoutParams mybubbleparams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            mybubbleparams.setMargins(100, 4, 30, 4);
            mybubbleparams.gravity = Gravity.END;

            // 이름 표시용 layout 설정
            LinearLayout.LayoutParams msgEditparams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            msgEditparams.setMargins(50, 5, 50, 1);

            // 서버용 layout 설정
            LinearLayout.LayoutParams notificationparams = new LinearLayout.LayoutParams (
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            notificationparams.setMargins(5, 5, 5, 5);

            while (in != null) {
                msg = removeLastEnter(in.readUTF());
                peoplenummsg = "";
                final String sender = Sender(msg);

                // 새로운 채팅 내용인 textview를 담는 linearlayout이다.
                bubbleContainer = new LinearLayout(ChatActivity.this);
                bubbleContainer.setOrientation(LinearLayout.VERTICAL);

                // 새로운 채팅 내용을 전달받으면 bubble 형식으로 창에 출력한다.
                bubble = new TextView(ChatActivity.this);
                bubble.setMaxWidth(700);
                bubble.setText(Message(msg));
                bubble.setTextColor(0xAA3a286d);
                bubble.setTextSize(16);

                peoplenumTv = new TextView(ChatActivity.this);
                peoplenumTv.setMaxWidth(500);

                if(sender.equals("Server")) {      // 서버에서 보낸 안내 메세지일 경우
                    if(msg.contains("/ 채팅방 인원")) {
                        peoplenummsg = peoplenum(msg);

                        bubble.setText(ServerMessage(Message(msg)));
                        peoplenumTv.setText(peoplenummsg);

                        peoplenumTv.setLayoutParams(notificationparams);
                        peoplenumTv.setTextSize(10);
                        peoplenumTv.setBackgroundResource(R.drawable.border_round_white);
                        peoplenumTv.setTextColor(0xAA3a286d);
                        peoplenumTv.setGravity(Gravity.CENTER);
                    }

                    bubble.setLayoutParams(notificationparams);
                    bubble.setTextSize(10);
                    bubble.setBackgroundResource(R.drawable.border_round_purple);
                    bubble.setTextColor(Color.WHITE);
                    bubble.setGravity(Gravity.CENTER);
                    bubbleContainer.setGravity(Gravity.CENTER);
                }else if (sender.equals(nickname)) {       // 내가 보낸 메세지일 경우
                    bubble.setLayoutParams(mybubbleparams);
                    bubble.setBackgroundResource(R.drawable.border_round_yellow);
                    bubbleContainer.setGravity(Gravity.END);
                } else {                                   // 상대방이 보낸 메세지일 경우
                    bubble.setLayoutParams(bubbleparams);
                    bubble.setBackgroundResource(R.drawable.border_round_white);
                    bubbleContainer.setGravity(Gravity.START);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 마지막에 보낸 사람과 현재 보낸 사람이 같다면 메세지 위에 이름 뜨지 않게 설정
                        if(!sender.equals(lastSender)) {
                            senderTv = new TextView(ChatActivity.this);
                            senderTv.setText(sender);
                            senderTv.setTextColor(Color.WHITE);
                            senderTv.setTextSize(12);
                            senderTv.setLayoutParams(msgEditparams);

                            bubbleContainer.addView(senderTv);
                        }

                        System.out.println("sender : " + sender);
                        System.out.println("lastSender : " + lastSender);

                        bubbleContainer.addView(bubble);
                        if(peoplenummsg != "") {
                            bubbleContainer.addView(peoplenumTv);
                        }

                        showbubbles.addView(bubbleContainer);
                        lastSender = sender;
                        //chatSV.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        } catch (IOException exception) {
            System.out.println("chatAndroid IOException 문제일까?");
            exception.printStackTrace();
        }
        bubbleContainer.removeView(senderTv);
        bubbleContainer.removeView(bubble);
        showbubbles.removeView(bubbleContainer);
    }

    // 입력된 메세지를 보냄.
    public void sendMessage(String msg) {
        try {
            mychat.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 전달받은 메세지의 마지막 \n 부분을 삭제하고 msg를 전달해줌.
    private String removeLastEnter(String msg) {
        if(msg.charAt(msg.length()-1) == '\n') {
            return msg.substring(0, msg.length()-1);
        } else return msg;
    }

    // 서버에게 전달받은 메세지에서 현재 채팅을 작성한 사람이 누구인지 파싱한다.
    private String Sender(String msg) {
        int indexColon = msg.indexOf(":");

        return msg.substring(0, indexColon-1);
    }

    // 서버에게 전달받은 메세지에서 채팅 내용만 파싱한다.
    private String Message(String msg) {
        int indexColon = msg.indexOf(":");

        return msg.substring(indexColon+2);
    }

    private String ServerMessage(String msg) {
        int indexslice = msg.indexOf("/");
        return msg.substring(0, indexslice-1);
    }

    private String peoplenum(String msg) {
        int indexslice = msg.indexOf("/");

        return msg.substring(indexslice+2);
    }
}























