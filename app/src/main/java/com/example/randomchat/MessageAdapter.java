package com.example.randomchat;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> mMessageList;

    private FirebaseAuth mAuth;

    int SELF=0,OTHER=1;

    public MessageAdapter(List<Message> mMessageList) {
        this.mMessageList = mMessageList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i("create viewHolder","create viewHolder");

        if(viewType==SELF){

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_second_layout,parent,false);
            return new SelfMessageViewHolder(view);

        }else if(viewType==OTHER){

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);
            return new OtherMessageViewHolder(view);

        }else{
            return null;
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if(holder instanceof SelfMessageViewHolder){
            Message c=mMessageList.get(position);
            ((SelfMessageViewHolder) holder).messageText.setText(c.getMessage());
        }else if(holder instanceof OtherMessageViewHolder){
            Message c=mMessageList.get(position);
            ((OtherMessageViewHolder) holder).messageText.setText(c.getMessage());
        }


    }


    public static class SelfMessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageText,timeText,nameText;
        public ImageView image;

        public SelfMessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText=itemView.findViewById(R.id.message_single_second_text);
            timeText=itemView.findViewById(R.id.message_single_second_time);
            nameText=itemView.findViewById(R.id.message_single_second_name);
            image=itemView.findViewById(R.id.message_single_second_image);
        }
    }

    public static class OtherMessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageText,timeText,nameText;
        public ImageView image;

        public OtherMessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText=itemView.findViewById(R.id.message_single_text);
            timeText=itemView.findViewById(R.id.message_single_time);
            image=itemView.findViewById(R.id.message_single_image);
            nameText=itemView.findViewById(R.id.message_single_name);
        }
    }


    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {

        mAuth=FirebaseAuth.getInstance();
        String current_user_id=mAuth.getCurrentUser().getUid();
        Message c=mMessageList.get(position);
        String from_user=c.getFrom();

        if(current_user_id.equals(from_user)){
            return SELF;
        }else{
            return OTHER;
        }
    }
}
