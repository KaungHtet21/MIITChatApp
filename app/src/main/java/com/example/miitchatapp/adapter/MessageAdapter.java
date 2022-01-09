package com.example.miitchatapp.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.miitchatapp.R;
import com.example.miitchatapp.activity.ChatActivity;
import com.example.miitchatapp.activity.ImageViewActivity;
import com.example.miitchatapp.activity.MainActivity;
import com.example.miitchatapp.modules.Messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    List<Messages> userMsgList;
    FirebaseAuth auth;
    DatabaseReference userRef;

    public MessageAdapter (List<Messages> userMsgList){
        this.userMsgList = userMsgList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_msg_layout, parent, false);
        auth = FirebaseAuth.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Messages messages = userMsgList.get(position);

        String senderId = auth.getCurrentUser().getUid();
        String fromUserId = messages.getFrom();
        String fromMsgType = messages.getType();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("image")){
                    String retImage = snapshot.child("image").getValue().toString();
                    Picasso.get().load(retImage).placeholder(R.drawable.profile).into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.senderMsgInput.setVisibility(View.GONE);
        holder.receiverMsgInput.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderPictureInput.setVisibility(View.GONE);
        holder.receiverPictureInput.setVisibility(View.GONE);

        if (fromMsgType.equals("text")){
            if (fromUserId.equals(senderId)){
                holder.senderMsgInput.setVisibility(View.VISIBLE);
                holder.senderMsgInput.setText(messages.getMessage() + "\n\n" + messages.getTime() + " - " + messages.getDate());
            }else {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMsgInput.setVisibility(View.VISIBLE);
                holder.receiverMsgInput.setText(messages.getMessage() + "\n\n" + messages.getTime() + " - " + messages.getDate());
            }
        }else if (fromMsgType.equals("image")){
            if (fromUserId.equals(senderId)){
                holder.senderPictureInput.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.senderPictureInput);
            }else {
                holder.receiverPictureInput.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.receiverPictureInput);
            }
        }else if (fromMsgType.equals("pdf") || fromMsgType.equals("docx")){
            if (fromUserId.equals(senderId)){
                holder.senderPictureInput.setVisibility(View.VISIBLE);

                holder.senderPictureInput.setBackgroundResource(R.drawable.file);

                holder.senderPictureInput.getLayoutParams().height = 300;
                holder.senderPictureInput.getLayoutParams().width = 300;

            }else {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverPictureInput.setVisibility(View.VISIBLE);

                holder.receiverPictureInput.setBackgroundResource(R.drawable.file);

                holder.receiverPictureInput.getLayoutParams().height = 300;
                holder.receiverPictureInput.getLayoutParams().width = 300;

            }
        }
        if (fromUserId.equals(senderId)){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userMsgList.get(position).getType().equals("pdf") || userMsgList.get(position).getType().equals("docx")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete For me",
                                "Download and View This Document",
                                "Cancel",
                                "Delete For Everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteSentMessage(holder.getAdapterPosition(), holder);

                                }

                                if (which == 1){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMsgList.get(holder.getAdapterPosition()).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }

                                if (which == 3){
                                    deleteMessageForEveryone(holder.getAdapterPosition(), holder);
                                }
                            }
                        });
                        builder.show();
                    } else if (userMsgList.get(position).getType().equals("text")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete For me",
                                "Cancel",
                                "Delete For Everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteSentMessage(holder.getAdapterPosition(), holder);

                                }

                                if (which == 2){
                                    deleteMessageForEveryone(holder.getAdapterPosition(), holder);
                                }
                            }
                        });
                        builder.show();
                    } else if (userMsgList.get(position).getType().equals("image")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete For me",
                                "View This Image",
                                "Cancel",
                                "Delete For Everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteSentMessage(holder.getAdapterPosition(), holder);

                                }

                                if (which == 1){
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewActivity.class);
                                    intent.putExtra("url", userMsgList.get(holder.getAdapterPosition()).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }

                                if (which == 3){
                                    deleteMessageForEveryone(holder.getAdapterPosition(), holder);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userMsgList.get(position).getType().equals("pdf") || userMsgList.get(position).getType().equals("docx")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete For me",
                                "Download and View This Document",
                                "Cancel",
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteReceiveMessage(holder.getAdapterPosition(), holder);

                                }

                                if (which == 1){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMsgList.get(holder.getAdapterPosition()).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    } else if (userMsgList.get(position).getType().equals("text")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete For me",
                                "Cancel",
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteReceiveMessage(holder.getAdapterPosition(), holder);

                                }

                            }
                        });
                        builder.show();
                    } else if (userMsgList.get(position).getType().equals("image")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete For me",
                                "View This Image",
                                "Cancel",
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteReceiveMessage(holder.getAdapterPosition(), holder);
                                }

                                if (which == 1){
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewActivity.class);
                                    intent.putExtra("url", userMsgList.get(holder.getAdapterPosition()).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return userMsgList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView receiverProfileImage;
        TextView receiverMsgInput, senderMsgInput;
        ImageView senderPictureInput, receiverPictureInput;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            receiverProfileImage = itemView.findViewById(R.id.msg_profile_image);
            receiverMsgInput = itemView.findViewById(R.id.msg_receiver_input);
            senderMsgInput = itemView.findViewById(R.id.msg_sender_input);

            senderPictureInput = itemView.findViewById(R.id.msg_sender_image_view);
            receiverPictureInput = itemView.findViewById(R.id.msg_receiver_image_view);
        }
    }

    private void deleteSentMessage(final int position, final ViewHolder holder){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Messages")
                .child(userMsgList.get(position).getFrom())
                .child(userMsgList.get(position).getTo())
                .child(userMsgList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, userMsgList.size());
                    Toast.makeText(holder.itemView.getContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(holder.itemView.getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteReceiveMessage(final int position, final ViewHolder holder){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Messages")
                .child(userMsgList.get(position).getTo())
                .child(userMsgList.get(position).getFrom())
                .child(userMsgList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, userMsgList.size());
                    Toast.makeText(holder.itemView.getContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(holder.itemView.getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void deleteMessage(final Messages messages) {
        userMsgList.remove(messages);
        notifyDataSetChanged();
    }

    private void deleteMessageForEveryone(final int position, final ViewHolder holder){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Messages")
                .child(userMsgList.get(position).getTo())
                .child(userMsgList.get(position).getFrom())
                .child(userMsgList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    reference.child("Messages")
                            .child(userMsgList.get(position).getFrom())
                            .child(userMsgList.get(position).getTo())
                            .child(userMsgList.get(position).getMessageId())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, userMsgList.size());
                                Toast.makeText(holder.itemView.getContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else {
                    Toast.makeText(holder.itemView.getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
