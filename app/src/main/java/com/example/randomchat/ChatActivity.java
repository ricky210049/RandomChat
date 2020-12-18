package com.example.randomchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private String mChatUserId,mCurrentUserId;

    private FirebaseAuth mAuth;

    private DatabaseReference mRootRef;
    private DatabaseReference messageRef;

    private CollectionReference mPairListRef;

    private  MessageAdapter mAdapter;

    private List<Message> messageList=new ArrayList<>();

    private LinearLayoutManager mLinearLayout;

    private ImageButton mChatAddBtn,mChatSendBtn;
    private EditText mChatMessageView;

    private RecyclerView mMessageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth=FirebaseAuth.getInstance();
        mCurrentUserId=mAuth.getCurrentUser().getUid();

        mRootRef= FirebaseDatabase.getInstance().getReference();

        final FirebaseFirestore db=FirebaseFirestore.getInstance();
        mPairListRef=db.collection("PairList");


        mChatAddBtn=findViewById(R.id.chat_add_btn);
        mChatSendBtn=findViewById(R.id.chat_send_btn);
        mChatMessageView=findViewById(R.id.chat_message_view);

        mAdapter=new MessageAdapter(messageList);

        mMessageList=findViewById(R.id.message_list);
        mLinearLayout=new LinearLayoutManager(this);
        mMessageList.setHasFixedSize(true);
        mMessageList.setLayoutManager(mLinearLayout);
        mMessageList.setAdapter(mAdapter);

        mPairListRef.document(mAuth.getCurrentUser().getUid()).delete();

        mChatUserId=getIntent().getStringExtra("chat_userId");
        Log.i("開始聊天","對象"+":"+mChatUserId);

        if(mChatUserId==null){
            Intent intent =new Intent(ChatActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }



    }

    @Override
    protected void onStart() {
        super.onStart();

        loadMessage();

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage();

            }
        });
    }

    @Override
    public void onBackPressed() {
        final FirebaseFirestore db=FirebaseFirestore.getInstance();
        final CollectionReference mPairListRef=db.collection("PairList");
        AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("確定要離開?");
        builder.setMessage("哈哈哈哈哈");
        builder.setCancelable(true);
        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mPairListRef.document(mCurrentUserId).delete();
                finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void loadMessage() {

        Log.i("loadMessage","loadMessage");

        messageRef=mRootRef.child("messages").child(mCurrentUserId).child(mChatUserId);

        Query messageQuery=messageRef.limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.i("onChildAdded","onChildAdded");
                Message message=dataSnapshot.getValue(Message.class);
                String messageKey=dataSnapshot.getKey();

                Log.i("messageKey",messageKey);

                messageList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessageList.scrollToPosition(messageList.size()-1);


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.i("onChildChanged","onChildChanged");
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.i("onChildRemoved","onChildRemoved");
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.i("onChildMoved","onChildMoved");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("onCancelled","onCancelled");
            }
        });

    }

    private void sendMessage() {

        final String message = mChatMessageView.getText().toString();
        if (!TextUtils.isEmpty(message)) {

            String user_message_pushId = mRootRef.child("messages").child(mCurrentUserId).child(mChatUserId).push().getKey();

            Map<String, Object> map = new HashMap<>();
            map.put("message", message);
            map.put("type", "text");
            map.put("time", ServerValue.TIMESTAMP);
            map.put("from", mCurrentUserId);

            Map<String, Object> mapAddress = new HashMap<>();
            mapAddress.put("messages/" + mCurrentUserId + "/" + mChatUserId + "/" + user_message_pushId, map);
            mapAddress.put("messages/" + mChatUserId + "/" + mCurrentUserId + "/" + user_message_pushId, map);

            mRootRef.updateChildren(mapAddress, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                    if (databaseError != null) {
                        Log.e("傳送訊息錯誤", databaseError.getMessage());
                    } else {
                        Log.i("傳送訊息成功 內容:", message);
                    }

                    mChatMessageView.setText("");

                }
            });


        }

    }


    private void sendToStart() {

        Intent intent =new Intent(ChatActivity.this,StartActivity.class);
        startActivity(intent);
        finish();

    }

}