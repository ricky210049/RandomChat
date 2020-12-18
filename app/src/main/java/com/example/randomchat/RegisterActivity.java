package com.example.randomchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private Button mRegisterBtn;
    private TextInputLayout mRegisterEmail,mRegisterPassword,mRegisterPasswordCheck;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth=FirebaseAuth.getInstance();

        mRegisterBtn=findViewById(R.id.register_btn);
        mRegisterEmail=findViewById(R.id.register_email);
        mRegisterPassword=findViewById(R.id.register_password);
        mRegisterPasswordCheck=findViewById(R.id.register_password_check);

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog.showProgressDialog(RegisterActivity.this,"註冊中...");
                String email=mRegisterEmail.getEditText().getText().toString();
                String password=mRegisterPassword.getEditText().getText().toString();
                String password_check=mRegisterPasswordCheck.getEditText().getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(password_check)){

                    if(password.equals(password_check)){


                        Dialog.showProgressDialog(RegisterActivity.this,"註冊中");
                        register(email,password);


                    }else{

                        Dialog.dismiss();
                        Toast.makeText(RegisterActivity.this,"密碼不相同",Toast.LENGTH_LONG).show();

                    }

                }else{
                    Dialog.dismiss();
                    Toast.makeText(RegisterActivity.this,"請輸入完整資訊",Toast.LENGTH_LONG).show();
                }
            }
        });

    }


    public void register(String email,String password){

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    FirebaseUser current_user=FirebaseAuth.getInstance().getCurrentUser();
                    mDatabase= FirebaseDatabase.getInstance().getReference("Users").child(current_user.getUid());

                    FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                        @Override
                        public void onSuccess(InstanceIdResult instanceIdResult) {

                            String deviceToken =instanceIdResult.getToken();

                            Log.i("Token", deviceToken);
                            HashMap<String,String> map=new HashMap<>();
                            map.put("Setting_Status","NOTHING");
                            map.put("online","default");
                            map.put("DeviceToken", deviceToken);

                            mDatabase.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Log.i("註冊狀態","註冊成功");
                                        Dialog.dismiss();
                                        Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        //finish();

                                    }else{

                                        Log.i("註冊狀態","註冊失敗");
                                        Toast.makeText(RegisterActivity.this,"註冊失敗",Toast.LENGTH_LONG).show();

                                    }
                                }
                            });

                        }
                    });

                }else{
                    Dialog.dismiss();
                    Log.i("註冊狀態","註冊失敗");
                    Toast.makeText(RegisterActivity.this,"註冊失敗",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}