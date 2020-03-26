package com.lethdz.onlinechatdemo.dao;

import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.lethdz.onlinechatdemo.modal.ChatRoom;
import com.lethdz.onlinechatdemo.modal.User;
import com.lethdz.onlinechatdemo.modal.UserChatRoom;
import com.lethdz.onlinechatdemo.modal.UserDetail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FirebaseDAO {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private boolean duplicatedFriend = false;

    public void addFriend(UserDetail user, View v, List<UserDetail> getListUser, RecyclerView.Adapter getAdapter) {
        final UserDetail userAdding = user;
        final View view = v;
        final RecyclerView.Adapter adapter = getAdapter;
        final List<UserDetail> listUser = getListUser;

        String documentName = user.getUid() + auth.getCurrentUser().getUid();
        Timestamp timeCreated = new Timestamp(new Date());
        // Value for the friend
        UserChatRoom chatRoomFriend = new UserChatRoom(
                documentName,
                auth.getCurrentUser().getEmail(),
                "",
                timeCreated);

        // Value for the current user
        UserChatRoom chatRoomUser = new UserChatRoom(
                documentName,
                user.getEmail(),
                "",
                timeCreated);

        // Value for the chat room
        List<User> members = new ArrayList<User>();
        members.add(new User(user.getUid(), user.getEmail()));
        members.add(new User(auth.getCurrentUser().getUid(), auth.getCurrentUser().getEmail()));
        ChatRoom chatRoom = new ChatRoom(documentName, "", "", timeCreated, members);

        // Update the Friend.
        DocumentReference userDetailRef = db.collection("UserDetail").document(user.getUid());
        userDetailRef.update("chatRoom", FieldValue.arrayUnion(chatRoomFriend));

        // Update current user
        DocumentReference currentUserRef = db.collection("UserDetail").document(auth.getCurrentUser().getUid());
        currentUserRef.update("chatRoom", FieldValue.arrayUnion(chatRoomUser));

        // Add chat room
        db.collection("ChatRoom").
                document(documentName).
                set(chatRoom).
                addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(view.getContext(), "Add " + userAdding.getDisplayName() + " Success", Toast.LENGTH_LONG).show();
                    listUser.clear();
                    adapter.notifyDataSetChanged();
                } else {
                    Log.d("Fail", "Adding Friend Error ", task.getException());
                    Toast.makeText(view.getContext(), "Adding Friend Error!!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void searchFriend(String query, RecyclerView.Adapter getAdapter, List<UserDetail> getlistUser, View v) {
        final RecyclerView.Adapter adapter = getAdapter;
        final List<UserDetail> listUser = getlistUser;
        final View view = v;

        db.collection("UserDetail").whereEqualTo("email", query).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            listUser.clear();
                            if(!task.getResult().isEmpty()) {
                                for (final QueryDocumentSnapshot document : task.getResult()) {
                                    // -------------------------------------------------------------
                                    Log.d("Success", document.getId() + " => " + document.getData());
                                    // -------------------------------------------------------------
                                    // Check friend is duplicated.
                                    final String uid = document.getData().get("uid").toString();
                                    db.collection("UserDetail").
                                            document(auth.getCurrentUser().getUid()).
                                            get().
                                            addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    DocumentSnapshot currentUserDocument = task.getResult();
                                                    UserDetail currentUser = currentUserDocument.toObject(UserDetail.class);
                                                    if(!currentUserDocument.getData().isEmpty()) {
                                                        for (UserChatRoom element:
                                                                currentUser.getChatRoom()) {
                                                            if(element.getDocumentName().equals(uid + auth.getCurrentUser().getUid())) {
                                                                duplicatedFriend = true;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    if (!duplicatedFriend) {
                                                        String email = document.getData().get("email").toString();
                                                        String displayName = document.getData().get("displayName").toString();
                                                        Uri photoUrl = document.getData().get("photoURL") == null ? null : (Uri) document.getData().get("photoURL");
                                                        listUser.add(new UserDetail(uid, email, displayName, photoUrl));
                                                        adapter.notifyDataSetChanged();
                                                    } else {
                                                        Toast.makeText(view.getContext(), "Can not find the results!!! ", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });

                                }
                            } else {
                                listUser.clear();
                                adapter.notifyDataSetChanged();
                                Log.d("Success", "Can not find the results", task.getException());
                                Toast.makeText(view.getContext(), "Can not find the results!!! ", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.d("Fail", "Error getting document: ", task.getException());
                            Toast.makeText(view.getContext(), "Error getting result try again later!!! ", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void getChatRoom(final RecyclerView.Adapter adapter, final List<UserChatRoom> userChatRooms, final View view) {
        DocumentReference docRef = db.collection("UserDetail").document(auth.getCurrentUser().getUid());
        docRef.addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                // Check for error
                if (e != null) {
                    Log.w("Fail", "Listen failed.", e);
                    Toast.makeText(view.getContext(), "Error in getting chat room please try again later", Toast.LENGTH_LONG).show();
                    return;
                }
                // Check for change at where local or server.
                String source = documentSnapshot != null && documentSnapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                // Check for data is not null and display the new list to the screen.
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Log.d("Get Data", source + " data: " + documentSnapshot.getData());
                    UserDetail currentUser = documentSnapshot.toObject(UserDetail.class);
                    if(!documentSnapshot.getData().isEmpty()) {
                        userChatRooms.clear();
                        userChatRooms.addAll(currentUser.getChatRoom());
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Log.d("Get Data", source + " data: null");
                }
            }
        });
    }
}
