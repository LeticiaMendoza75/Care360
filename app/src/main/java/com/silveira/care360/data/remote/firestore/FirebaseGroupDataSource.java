package com.silveira.care360.data.remote.firestore;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.silveira.care360.data.mapper.GroupMapper;
import com.silveira.care360.data.mapper.GroupMemberMapper;
import com.silveira.care360.data.mapper.UserMapper;
import com.silveira.care360.data.remote.dto.GroupDocumentDto;
import com.silveira.care360.data.remote.dto.GroupMemberDocumentDto;
import com.silveira.care360.data.remote.dto.UserDocumentDto;
import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.GroupMember;
import com.silveira.care360.domain.repository.GroupRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseGroupDataSource {
    private static final int JOIN_CODE_LEN = 8;

    private final FirebaseFirestore firestore;

    @Inject
    public FirebaseGroupDataSource(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public void createGroupForUser(String groupName, String careName, UserDocumentDto user, ResultCallback<Void> callback) {
        String joinCode = generateJoinCode(JOIN_CODE_LEN);
        String groupId = firestore.collection("groups").document().getId();

        GroupDocumentDto groupDto = new GroupDocumentDto();
        groupDto.setId(groupId);
        groupDto.setName(groupName);
        groupDto.setCareName(careName);
        groupDto.setJoinCode(joinCode);
        groupDto.setCreatedBy(user.getId());
        groupDto.setActive(true);

        GroupMemberDocumentDto memberDto = new GroupMemberDocumentDto();
        memberDto.setGroupId(groupId);
        memberDto.setUserId(user.getId());
        memberDto.setName(user.getDisplayName() != null ? user.getDisplayName() : "");
        memberDto.setEmail(user.getEmail());
        memberDto.setRole("admin");

        GroupMemberDocumentDto membershipDto = new GroupMemberDocumentDto();
        membershipDto.setGroupId(groupId);
        membershipDto.setGroupName(groupName);
        membershipDto.setCareName(careName);
        membershipDto.setRole("admin");

        WriteBatch batch = firestore.batch();
        batch.set(
                firestore.collection("groups").document(groupId),
                GroupMapper.toFirestoreMap(groupDto, FieldValue.serverTimestamp(), FieldValue.serverTimestamp())
        );
        batch.set(
                firestore.collection("groups").document(groupId).collection("members").document(user.getId()),
                GroupMemberMapper.toFirestoreMemberMap(
                        memberDto,
                        FieldValue.serverTimestamp(),
                        FieldValue.serverTimestamp()
                )
        );
        batch.set(
                firestore.collection("users").document(user.getId()).collection("memberships").document(groupId),
                GroupMemberMapper.toFirestoreMembershipMap(
                        membershipDto,
                        FieldValue.serverTimestamp(),
                        FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
        );
        batch.set(
                firestore.collection("users").document(user.getId()),
                UserMapper.toActiveGroupUpdateMap(groupId, FieldValue.serverTimestamp()),
                SetOptions.merge()
        );

        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError("Error creando el grupo"));
    }

    public void joinGroupByCode(String joinCode, UserDocumentDto user, ResultCallback<GroupRepository.JoinGroupResult> callback) {
        firestore.collection("groups")
                .whereEqualTo("joinCode", joinCode)
                .limit(1)
                .get()
                .addOnSuccessListener(qs -> {
                    if (qs.isEmpty()) {
                        callback.onError("Codigo no encontrado");
                        return;
                    }

                    DocumentSnapshot gdoc = qs.getDocuments().get(0);
                    String groupId = gdoc.getId();

                    GroupMemberDocumentDto memberDto = new GroupMemberDocumentDto();
                    memberDto.setGroupId(groupId);
                    memberDto.setUserId(user.getId());
                    memberDto.setEmail(user.getEmail());
                    memberDto.setName(user.getDisplayName() != null ? user.getDisplayName() : "");
                    memberDto.setRole("member");

                    GroupMemberDocumentDto membershipDto = new GroupMemberDocumentDto();
                    membershipDto.setGroupId(groupId);
                    membershipDto.setGroupName(gdoc.getString("name"));
                    membershipDto.setCareName(gdoc.getString("careName"));
                    membershipDto.setRole("member");

                    WriteBatch batch = firestore.batch();
                    batch.set(
                            firestore.collection("users").document(user.getId()),
                            UserMapper.toActiveGroupUpdateMap(groupId, FieldValue.serverTimestamp()),
                            SetOptions.merge()
                    );
                    batch.set(
                            firestore.collection("groups").document(groupId).collection("members").document(user.getId()),
                            GroupMemberMapper.toFirestoreMemberMap(
                                    memberDto,
                                    FieldValue.serverTimestamp(),
                                    FieldValue.serverTimestamp()
                            ),
                            SetOptions.merge()
                    );
                    batch.set(
                            firestore.collection("users").document(user.getId()).collection("memberships").document(groupId),
                            GroupMemberMapper.toFirestoreMembershipMap(
                                    membershipDto,
                                    FieldValue.serverTimestamp(),
                                    FieldValue.serverTimestamp()
                            ),
                            SetOptions.merge()
                    );

                    batch.commit()
                            .addOnSuccessListener(unused ->
                                    callback.onSuccess(GroupRepository.JoinGroupResult.joined()))
                            .addOnFailureListener(e -> callback.onError("Error uniendote al grupo"));
                })
                .addOnFailureListener(e -> callback.onError("Error buscando el codigo"));
    }

    public Task<QuerySnapshot> getUserGroups(String userId) {
        return firestore.collection("groups")
                .whereEqualTo("createdBy", userId)
                .get();
    }

    public Task<List<GroupMemberDocumentDto>> getGroupMembers(String groupId) {
        return firestore.collection("groups")
                .document(groupId)
                .collection("members")
                .get()
                .continueWith(task -> {
                    List<GroupMemberDocumentDto> members = new java.util.ArrayList<>();

                    if (!task.isSuccessful()) {
                        return members;
                    }

                    QuerySnapshot snapshot = task.getResult();
                    if (snapshot == null) {
                        return members;
                    }

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        GroupMemberDocumentDto member = GroupMemberMapper.fromFirestore(doc, groupId);
                        if (member != null) {
                            members.add(member);
                        }
                    }

                    return members;
                });
    }

    public Task<DocumentSnapshot> getGroupById(String groupId) {
        return firestore.collection("groups")
                .document(groupId)
                .get();
    }

    public void updateMemberRole(String groupId, String userId, String newRole, ResultCallback<Void> callback) {
        if (groupId == null || groupId.trim().isEmpty() || userId == null || userId.trim().isEmpty()) {
            callback.onError("No se pudo identificar el miembro");
            return;
        }

        WriteBatch batch = firestore.batch();

        java.util.Map<String, Object> memberUpdate = new java.util.HashMap<>();
        memberUpdate.put("role", newRole);
        memberUpdate.put("updatedAt", FieldValue.serverTimestamp());

        java.util.Map<String, Object> membershipUpdate = new java.util.HashMap<>();
        membershipUpdate.put("role", newRole);
        membershipUpdate.put("updatedAt", FieldValue.serverTimestamp());

        batch.set(
                firestore.collection("groups").document(groupId).collection("members").document(userId),
                memberUpdate,
                SetOptions.merge()
        );

        batch.set(
                firestore.collection("users").document(userId).collection("memberships").document(groupId),
                membershipUpdate,
                SetOptions.merge()
        );

        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError("No se pudo actualizar el rol"));
    }

    public void removeMember(String groupId, String userId, ResultCallback<Void> callback) {
        if (groupId == null || groupId.trim().isEmpty() || userId == null || userId.trim().isEmpty()) {
            callback.onError("No se pudo identificar el miembro");
            return;
        }

        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    WriteBatch batch = firestore.batch();
                    batch.delete(firestore.collection("groups").document(groupId).collection("members").document(userId));
                    batch.delete(firestore.collection("users").document(userId).collection("memberships").document(groupId));

                    if (groupId.equals(userDoc.getString("activeGroupId"))) {
                        batch.set(
                                userDoc.getReference(),
                                UserMapper.toClearActiveGroupUpdateMap(FieldValue.serverTimestamp()),
                                SetOptions.merge()
                        );
                    }

                    batch.commit()
                            .addOnSuccessListener(unused -> callback.onSuccess(null))
                            .addOnFailureListener(e -> callback.onError("No se pudo eliminar al miembro"));
                })
                .addOnFailureListener(e -> callback.onError("No se pudo cargar el miembro"));
    }

    public void leaveGroup(String groupId, String userId, String newOwnerUserId, ResultCallback<Void> callback) {
        if (groupId == null || groupId.trim().isEmpty() || userId == null || userId.trim().isEmpty()) {
            callback.onError("No se pudo identificar el grupo");
            return;
        }

        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    WriteBatch batch = firestore.batch();

                    batch.delete(firestore.collection("groups").document(groupId).collection("members").document(userId));
                    batch.delete(firestore.collection("users").document(userId).collection("memberships").document(groupId));

                    if (groupId.equals(userDoc.getString("activeGroupId"))) {
                        batch.set(
                                userDoc.getReference(),
                                UserMapper.toClearActiveGroupUpdateMap(FieldValue.serverTimestamp()),
                                SetOptions.merge()
                        );
                    }

                    if (newOwnerUserId != null && !newOwnerUserId.trim().isEmpty()) {
                        java.util.Map<String, Object> groupUpdate = new java.util.HashMap<>();
                        groupUpdate.put("createdBy", newOwnerUserId);
                        groupUpdate.put("updatedAt", FieldValue.serverTimestamp());

                        batch.set(
                                firestore.collection("groups").document(groupId),
                                groupUpdate,
                                SetOptions.merge()
                        );

                        java.util.Map<String, Object> memberUpdate = new java.util.HashMap<>();
                        memberUpdate.put("role", "admin");
                        memberUpdate.put("updatedAt", FieldValue.serverTimestamp());

                        batch.set(
                                firestore.collection("groups").document(groupId).collection("members").document(newOwnerUserId),
                                memberUpdate,
                                SetOptions.merge()
                        );

                        java.util.Map<String, Object> membershipUpdate = new java.util.HashMap<>();
                        membershipUpdate.put("role", "admin");
                        membershipUpdate.put("updatedAt", FieldValue.serverTimestamp());

                        batch.set(
                                firestore.collection("users").document(newOwnerUserId).collection("memberships").document(groupId),
                                membershipUpdate,
                                SetOptions.merge()
                        );
                    }

                    batch.commit()
                            .addOnSuccessListener(unused -> callback.onSuccess(null))
                            .addOnFailureListener(e -> callback.onError("No se pudo salir del grupo"));
                })
                .addOnFailureListener(e -> callback.onError("No se pudo cargar el usuario"));
    }

    public void deleteGroup(String groupId, List<GroupMember> members, ResultCallback<Void> callback) {
        if (groupId == null || groupId.trim().isEmpty()) {
            callback.onError("No se pudo identificar el grupo");
            return;
        }

        List<GroupMember> safeMembers = members != null ? members : new ArrayList<>();

        loadUserDocuments(safeMembers, 0, new ArrayList<>(), new ResultCallback<List<DocumentSnapshot>>() {
            @Override
            public void onSuccess(List<DocumentSnapshot> userDocs) {
                firestore.collection("groups")
                        .document(groupId)
                        .collection("medicamentos")
                        .get()
                        .addOnSuccessListener(medicamentosSnapshot ->
                                deleteMedicamentosRecursively(
                                        medicamentosSnapshot.getDocuments(),
                                        0,
                                        new ResultCallback<Void>() {
                                            @Override
                                            public void onSuccess(Void result) {
                                                commitDeleteGroupBatch(groupId, safeMembers, userDocs, callback);
                                            }

                                            @Override
                                            public void onError(String message) {
                                                callback.onError(message);
                                            }
                                        }))
                        .addOnFailureListener(e -> callback.onError("No se pudo eliminar la medicacion del grupo"));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void loadUserDocuments(List<GroupMember> members,
                                   int index,
                                   List<DocumentSnapshot> userDocs,
                                   ResultCallback<List<DocumentSnapshot>> callback) {
        if (index >= members.size()) {
            callback.onSuccess(userDocs);
            return;
        }

        GroupMember member = members.get(index);
        if (member == null || member.getUserId() == null || member.getUserId().trim().isEmpty()) {
            loadUserDocuments(members, index + 1, userDocs, callback);
            return;
        }

        firestore.collection("users")
                .document(member.getUserId())
                .get()
                .addOnSuccessListener(userDoc -> {
                    userDocs.add(userDoc);
                    loadUserDocuments(members, index + 1, userDocs, callback);
                })
                .addOnFailureListener(e -> callback.onError("No se pudo cargar la informacion de los miembros"));
    }

    private void deleteMedicamentosRecursively(List<DocumentSnapshot> medicamentos,
                                               int index,
                                               ResultCallback<Void> callback) {
        if (medicamentos == null || index >= medicamentos.size()) {
            callback.onSuccess(null);
            return;
        }

        DocumentSnapshot medicamentoDoc = medicamentos.get(index);
        if (medicamentoDoc == null) {
            deleteMedicamentosRecursively(medicamentos, index + 1, callback);
            return;
        }

        medicamentoDoc.getReference()
                .collection("dias")
                .get()
                .addOnSuccessListener(diasSnapshot -> {
                    WriteBatch batch = firestore.batch();

                    for (DocumentSnapshot diaDoc : diasSnapshot.getDocuments()) {
                        batch.delete(diaDoc.getReference());
                    }

                    batch.delete(medicamentoDoc.getReference());

                    batch.commit()
                            .addOnSuccessListener(unused ->
                                    deleteMedicamentosRecursively(medicamentos, index + 1, callback))
                            .addOnFailureListener(e -> callback.onError("No se pudo eliminar la medicacion del grupo"));
                })
                .addOnFailureListener(e -> callback.onError("No se pudo eliminar la medicacion del grupo"));
    }

    private void commitDeleteGroupBatch(String groupId,
                                        List<GroupMember> members,
                                        List<DocumentSnapshot> userDocs,
                                        ResultCallback<Void> callback) {
        WriteBatch batch = firestore.batch();

        for (GroupMember member : members) {
            if (member == null || member.getUserId() == null || member.getUserId().trim().isEmpty()) {
                continue;
            }

            String userId = member.getUserId();
            batch.delete(firestore.collection("groups").document(groupId).collection("members").document(userId));
            batch.delete(firestore.collection("users").document(userId).collection("memberships").document(groupId));
        }

        if (userDocs != null) {
            for (DocumentSnapshot userDoc : userDocs) {
                if (userDoc == null || !userDoc.exists()) {
                    continue;
                }

                String activeGroupId = userDoc.getString("activeGroupId");
                if (groupId.equals(activeGroupId)) {
                    batch.set(
                            userDoc.getReference(),
                            UserMapper.toClearActiveGroupUpdateMap(FieldValue.serverTimestamp()),
                            SetOptions.merge()
                    );
                }
            }
        }

        batch.delete(firestore.collection("groups").document(groupId));

        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError("No se pudo eliminar el grupo"));
    }

    public void updateCareProfile(String groupId,
                                  String careName,
                                  Integer careAge,
                                  String carePhotoUri,
                                  String carePhotoUrl,
                                  String carePhone,
                                  String careAddress,
                                  String emergencyContactName,
                                  String emergencyContactPhone,
                                  String careAllergies,
                                  String careConditions,
                                  ResultCallback<Void> callback) {
        if (groupId == null || groupId.trim().isEmpty()) {
            callback.onError("No se pudo identificar el grupo");
            return;
        }

        firestore.collection("groups")
                .document(groupId)
                .collection("members")
                .get()
                .addOnSuccessListener(membersSnapshot -> {
                    WriteBatch batch = firestore.batch();

                    java.util.Map<String, Object> groupUpdate = new java.util.HashMap<>();
                    groupUpdate.put("careName", careName);
                    groupUpdate.put("careAge", careAge);
                    groupUpdate.put("carePhotoUri", carePhotoUri);
                    groupUpdate.put("carePhotoUrl", carePhotoUrl);
                    groupUpdate.put("carePhone", carePhone);
                    groupUpdate.put("careAddress", careAddress);
                    groupUpdate.put("emergencyContactName", emergencyContactName);
                    groupUpdate.put("emergencyContactPhone", emergencyContactPhone);
                    groupUpdate.put("careAllergies", careAllergies);
                    groupUpdate.put("careConditions", careConditions);
                    groupUpdate.put("updatedAt", FieldValue.serverTimestamp());

                    batch.set(
                            firestore.collection("groups").document(groupId),
                            groupUpdate,
                            SetOptions.merge()
                    );

                    for (DocumentSnapshot memberDoc : membersSnapshot.getDocuments()) {
                        String memberUserId = memberDoc.getString("uid");
                        if (memberUserId == null || memberUserId.trim().isEmpty()) {
                            memberUserId = memberDoc.getId();
                        }

                        java.util.Map<String, Object> membershipUpdate = new java.util.HashMap<>();
                        membershipUpdate.put("careName", careName);
                        membershipUpdate.put("updatedAt", FieldValue.serverTimestamp());

                        batch.set(
                                firestore.collection("groups").document(groupId).collection("members").document(memberDoc.getId()),
                                membershipUpdate,
                                SetOptions.merge()
                        );

                        batch.set(
                                firestore.collection("users").document(memberUserId).collection("memberships").document(groupId),
                                membershipUpdate,
                                SetOptions.merge()
                        );
                    }

                    batch.commit()
                            .addOnSuccessListener(unused -> callback.onSuccess(null))
                            .addOnFailureListener(e -> callback.onError("No se pudo actualizar el perfil"));
                })
                .addOnFailureListener(e -> callback.onError("No se pudo actualizar el perfil"));
    }

    private String generateJoinCode(int len) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(r.nextInt(chars.length())));
        }
        return sb.toString().toUpperCase(Locale.ROOT);
    }
}
