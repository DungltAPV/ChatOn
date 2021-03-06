package com.lethdz.onlinechatdemo.home;

import android.content.Intent;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.lethdz.onlinechatdemo.MessageListActivity;
import com.lethdz.onlinechatdemo.R;
import com.lethdz.onlinechatdemo.modal.UserChatRoom;

import java.text.SimpleDateFormat;
import java.util.List;

public class MessageHomeRecyclerViewAdapter extends RecyclerView.Adapter<MessageHomeRecyclerViewAdapter.ViewHolder> {
    private List<UserChatRoom> listChatRoom;
    private FragmentActivity activity;
    private long mLastClickTime = 0;

    public MessageHomeRecyclerViewAdapter(List<UserChatRoom> listChatRoom, FragmentActivity activity) {
        this.listChatRoom = listChatRoom;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.card_chat_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserChatRoom chatRoom = listChatRoom.get(position);

        String pattern = "EEE-HH:mm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        holder.avatar.setImageResource(R.drawable.chatroom_icon);
        holder.displayText.setText(chatRoom.getTitle());
        holder.lastMessage.setText(chatRoom.getLastMessage());
        holder.timeStamp.setText(simpleDateFormat.format(chatRoom.getTimeStamp().toDate()));
    }

    @Override
    public int getItemCount() {
        return listChatRoom.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView avatar;
        public TextView displayText;
        public TextView lastMessage;
        public TextView timeStamp;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.iv_avatar);
            displayText = itemView.findViewById(R.id.txt_displayName);
            lastMessage = itemView.findViewById(R.id.txt_lastMessage);
            timeStamp = itemView.findViewById(R.id.txt_timeStamp);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    int position = getAdapterPosition();
                    UserChatRoom chatRoom = listChatRoom.get(position);
                    String documentName = chatRoom.getDocumentName();
                    Intent intent = new Intent(activity, MessageListActivity.class);
                    intent.putExtra("DOCUMENT_NAME", documentName);
                    activity.startActivity(intent);
                }
            });
        }
    }
}
