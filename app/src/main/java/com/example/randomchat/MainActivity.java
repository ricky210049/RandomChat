package com.example.randomchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Button mLogoutBtn;
    private Button mPairBtn;
    private String mPairStatus=null;
    private String pairUid = null;
    private String pairUid2 = null;
    private CollectionReference mPairListRef;

    private Handler handler;

    private volatile boolean isPairing = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();

        mDatabase= FirebaseDatabase.getInstance().getReference();

        final FirebaseFirestore db=FirebaseFirestore.getInstance();
        mPairListRef=db.collection("PairList");

        handler=new Handler();


        mLogoutBtn=findViewById(R.id.logout_btn);
        mPairBtn=findViewById(R.id.main_pair_btn);

        if(mAuth.getCurrentUser() == null){
            Log.i("用戶登入狀態","尚未登入");

            sendToStart();
        }else {
            Log.i("用戶登入狀態","已登入"+mAuth.getCurrentUser().getUid());

        }

    }

    @Override
    protected void onStart() {
        super.onStart();


        final FirebaseUser current_user=mAuth.getCurrentUser();

        mPairListRef.document(current_user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){
                    DocumentSnapshot document =task.getResult();
                    if(document.exists()){

                        mPairBtn.setText("配對中");
                        mPairStatus="PAIRING";
                        Log.i("配對狀態",current_user.getUid()+"正在配對");

                    }else{

                        mPairBtn.setText("配對");
                        mPairStatus="NO_PAIRING";
                        Log.i("配對狀態",current_user.getUid()+"沒在配對");

                    }
                }else {
                    Log.e("GetDocument","失敗");
                }

            }
        });



        mPairListRef.document(current_user.getUid()).addSnapshotListener(MetadataChanges.INCLUDE,new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                if(error!=null){
                    Log.e("Listen failed",error.toString());
                }

                if(value != null && value.exists()){
                    Log.i("Current Data",value.getData().toString());

                    if(value.get("配對對象")!=null){
                        Log.i("配對對象",value.get("配對對象").toString());
                        sendToChat(value.get("配對對象").toString());
                    }



                }else{
                    Log.i("current Data","null");
                }

            }
        });





        mLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                sendToStart();
            }
        });

        mPairBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(mPairStatus.equals("NO_PAIRING")){



                    Map<String,Object> map =new HashMap<>();
                    map.put("進入時間", FieldValue.serverTimestamp());

                    mPairListRef.document(current_user.getUid()).set(map, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mDatabase.child("airList").child(current_user.getUid()).setValue(ServerValue.TIMESTAMP);
                            mPairBtn.setText("配對中");
                            mPairStatus="PAIRING";

                            Log.i("配對","進入配對列表成功");

                            isPairing=false;

                            PairRunnable runnable=new PairRunnable();
                            new Thread(runnable).start();

                        }
                    });


                }else if(mPairStatus.equals("PAIRING")){

                    isPairing=true;

                    mPairListRef.document(current_user.getUid()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mDatabase.child("PairList").child(current_user.getUid()).removeValue();
                            mPairBtn.setText("配對");
                            mPairStatus="NO_PAIRING";




                            Log.i("配對","退出配對列表成功");
                        }
                    });
                }


            }

        });

    }

    class PairRunnable implements Runnable{
        @Override
        public void run() {

            while (!isPairing){
                try {
                    pairing();
                    Thread.sleep(1200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    };



    private void pairing(){

        mPairListRef.orderBy("進入時間").limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if(task.isSuccessful()){

                    Log.i("次數","一次");


                    for(QueryDocumentSnapshot document : task.getResult()){

                        Log.i("排序現有用戶",document.getId()+" ==> "+document.getData()+" 數量 "+task.getResult().size());
                        pairUid=document.getId();

                    }

                    if(pairUid != null && !pairUid.equals(mAuth.getCurrentUser().getUid())){

                        Log.i("配對對象",pairUid);

                        mPairStatus="NO_PAIRING";

                        isPairing=true;

                        Map<String,Object> map=new HashMap<>();
                        map.put("配對對象",mAuth.getCurrentUser().getUid());

                        mPairListRef.document(pairUid).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });

                        sendToChat(pairUid);

                    }else{

                        isPairing=false;


                    }

                }

            }

        });

    }

    private void sendToStart() {

        Intent intent =new Intent(MainActivity.this,StartActivity.class);
        startActivity(intent);
        finish();

    }

    private void sendToChat(String pair){

        isPairing=true;



        Intent intent =new Intent(MainActivity.this,ChatActivity.class);
        intent.putExtra("chat_userId",pair);
        startActivity(intent);


    }

}