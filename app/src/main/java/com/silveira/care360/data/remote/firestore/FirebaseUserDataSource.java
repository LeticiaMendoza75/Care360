package com.silveira.care360.data.remote.firestore;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.silveira.care360.data.mapper.GroupMemberMapper;
import com.silveira.care360.data.mapper.UserMapper;
import com.silveira.care360.data.remote.dto.GroupMemberDocumentDto;
import com.silveira.care360.data.remote.dto.UserDocumentDto;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseUserDataSource {

    private final FirebaseFirestore firestore;

    @Inject
    public FirebaseUserDataSource(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public Task<DocumentSnapshot> getUserProfileTask(String uid) {
        return firestore.collection("users").document(uid).get();
    }

    public Task<Void> createMinimalUserProfile(UserDocumentDto user) {
        return firestore.collection("users").document(user.getId()).set(UserMapper.toFirestoreMap(user));
    }

    public Task<QuerySnapshot> searchUserByEmail(String email) {
        return firestore.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get();
    }

    public Task<List<GroupMemberDocumentDto>> getUserMemberships(String uid) {
        return firestore.collection("users").document(uid)
                .collection("memberships")
                .get()
                .continueWith(task -> {
                    List<GroupMemberDocumentDto> memberships = new java.util.ArrayList<>();

                    if (!task.isSuccessful()) {
                        return memberships;
                    }

                    QuerySnapshot snapshot = task.getResult();
                    if (snapshot == null) {
                        return memberships;
                    }

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        GroupMemberDocumentDto dto = GroupMemberMapper.fromFirestore(doc, doc.getString("groupId"));
                        if (dto != null) {
                            if (dto.getGroupId() == null || dto.getGroupId().trim().isEmpty()) {
                                dto.setGroupId(doc.getId());
                            }
                            dto.setUserId(uid);
                            memberships.add(dto);
                        }
                    }

                    return memberships;
                });
    }

    public Task<Void> setActiveGroup(String uid, String groupId) {
        return firestore.collection("users").document(uid)
                .update(UserMapper.toActiveGroupUpdateMap(groupId, System.currentTimeMillis()));
    }
}
