package com.rollup.rollupapi.common.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.rollup.rollupapi.common.dto.response.RoomResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirestoreService {

    private final Firestore firestore;

    private static final String ROOMS_COLLECTION = "rooms";
    private static final String INFO_DOC = "info";
    private static final String STATE_DOC = "state";
    private static final String PRIVATE_COLLECTION = "private";

    // ===== Room Operations =====

    public String createRoom(String gameType, String hostId, String hostNickname, int maxPlayers, String roomName) {
        String roomId = UUID.randomUUID().toString();

        Map<String, Object> roomInfo = new HashMap<>();
        roomInfo.put("gameType", gameType);
        roomInfo.put("hostId", hostId);
        roomInfo.put("roomName", roomName != null ? roomName : "Room " + roomId.substring(0, 8));
        roomInfo.put("maxPlayers", maxPlayers);
        roomInfo.put("status", "waiting");
        roomInfo.put("createdAt", FieldValue.serverTimestamp());

        List<Map<String, Object>> players = new ArrayList<>();
        Map<String, Object> host = new HashMap<>();
        host.put("id", hostId);
        host.put("nickname", hostNickname);
        host.put("isReady", false);
        host.put("isConnected", true);
        players.add(host);
        roomInfo.put("players", players);

        try {
            firestore.collection(ROOMS_COLLECTION)
                    .document(roomId)
                    .collection("data")
                    .document(INFO_DOC)
                    .set(roomInfo)
                    .get();
            return roomId;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to create room", e);
            throw new RuntimeException("방 생성에 실패했습니다", e);
        }
    }

    @SuppressWarnings("unchecked")
    public RoomResponse getRoomInfo(String roomId) {
        try {
            DocumentSnapshot doc = firestore.collection(ROOMS_COLLECTION)
                    .document(roomId)
                    .collection("data")
                    .document(INFO_DOC)
                    .get()
                    .get();

            if (!doc.exists()) {
                return null;
            }

            Map<String, Object> data = doc.getData();
            List<Map<String, Object>> playerList = (List<Map<String, Object>>) data.get("players");
            List<RoomResponse.PlayerInfo> players = new ArrayList<>();

            for (Map<String, Object> p : playerList) {
                players.add(RoomResponse.PlayerInfo.builder()
                        .id((String) p.get("id"))
                        .nickname((String) p.get("nickname"))
                        .isReady(Boolean.TRUE.equals(p.get("isReady")))
                        .isConnected(Boolean.TRUE.equals(p.get("isConnected")))
                        .build());
            }

            return RoomResponse.builder()
                    .roomId(roomId)
                    .gameType((String) data.get("gameType"))
                    .hostId((String) data.get("hostId"))
                    .roomName((String) data.get("roomName"))
                    .maxPlayers(((Number) data.get("maxPlayers")).intValue())
                    .status((String) data.get("status"))
                    .players(players)
                    .build();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to get room info", e);
            throw new RuntimeException("방 정보 조회에 실패했습니다", e);
        }
    }

    public List<RoomResponse> getRoomsByGameType(String gameType) {
        try {
            QuerySnapshot snapshot = firestore.collectionGroup("data")
                    .whereEqualTo("gameType", gameType)
                    .whereEqualTo("status", "waiting")
                    .get()
                    .get();

            List<RoomResponse> rooms = new ArrayList<>();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                if (INFO_DOC.equals(doc.getId())) {
                    String roomId = doc.getReference().getParent().getParent().getId();
                    RoomResponse room = getRoomInfo(roomId);
                    if (room != null) {
                        rooms.add(room);
                    }
                }
            }
            return rooms;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to get rooms by game type", e);
            throw new RuntimeException("방 목록 조회에 실패했습니다", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void joinRoom(String roomId, String playerId, String nickname) {
        DocumentReference docRef = firestore.collection(ROOMS_COLLECTION)
                .document(roomId)
                .collection("data")
                .document(INFO_DOC);

        try {
            firestore.runTransaction(transaction -> {
                DocumentSnapshot doc = transaction.get(docRef).get();
                if (!doc.exists()) {
                    throw new RuntimeException("방을 찾을 수 없습니다");
                }

                Map<String, Object> data = doc.getData();
                String status = (String) data.get("status");
                if (!"waiting".equals(status)) {
                    throw new RuntimeException("이미 게임이 시작된 방입니다");
                }

                List<Map<String, Object>> players = (List<Map<String, Object>>) data.get("players");
                int maxPlayers = ((Number) data.get("maxPlayers")).intValue();

                if (players.size() >= maxPlayers) {
                    throw new RuntimeException("방이 가득 찼습니다");
                }

                // 이미 참가 중인지 확인
                for (Map<String, Object> p : players) {
                    if (playerId.equals(p.get("id"))) {
                        throw new RuntimeException("이미 참가 중입니다");
                    }
                }

                Map<String, Object> newPlayer = new HashMap<>();
                newPlayer.put("id", playerId);
                newPlayer.put("nickname", nickname);
                newPlayer.put("isReady", false);
                newPlayer.put("isConnected", true);
                players.add(newPlayer);

                transaction.update(docRef, "players", players);
                return null;
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to join room", e);
            throw new RuntimeException(e.getCause() != null ? e.getCause().getMessage() : "방 입장에 실패했습니다", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void leaveRoom(String roomId, String playerId) {
        DocumentReference docRef = firestore.collection(ROOMS_COLLECTION)
                .document(roomId)
                .collection("data")
                .document(INFO_DOC);

        try {
            firestore.runTransaction(transaction -> {
                DocumentSnapshot doc = transaction.get(docRef).get();
                if (!doc.exists()) {
                    return null;
                }

                Map<String, Object> data = doc.getData();
                List<Map<String, Object>> players = (List<Map<String, Object>>) data.get("players");
                String hostId = (String) data.get("hostId");

                players.removeIf(p -> playerId.equals(p.get("id")));

                if (players.isEmpty()) {
                    // 방에 아무도 없으면 삭제
                    transaction.delete(docRef);
                } else {
                    // 방장이 나갔으면 다음 사람에게 양도
                    if (playerId.equals(hostId) && !players.isEmpty()) {
                        String newHostId = (String) players.get(0).get("id");
                        transaction.update(docRef, "hostId", newHostId, "players", players);
                    } else {
                        transaction.update(docRef, "players", players);
                    }
                }
                return null;
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to leave room", e);
            throw new RuntimeException("방 퇴장에 실패했습니다", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void toggleReady(String roomId, String playerId) {
        DocumentReference docRef = firestore.collection(ROOMS_COLLECTION)
                .document(roomId)
                .collection("data")
                .document(INFO_DOC);

        try {
            firestore.runTransaction(transaction -> {
                DocumentSnapshot doc = transaction.get(docRef).get();
                if (!doc.exists()) {
                    throw new RuntimeException("방을 찾을 수 없습니다");
                }

                List<Map<String, Object>> players = (List<Map<String, Object>>) doc.get("players");
                for (Map<String, Object> p : players) {
                    if (playerId.equals(p.get("id"))) {
                        boolean currentReady = Boolean.TRUE.equals(p.get("isReady"));
                        p.put("isReady", !currentReady);
                        break;
                    }
                }

                transaction.update(docRef, "players", players);
                return null;
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to toggle ready", e);
            throw new RuntimeException("준비 상태 변경에 실패했습니다", e);
        }
    }

    public void updateRoomStatus(String roomId, String status) {
        try {
            firestore.collection(ROOMS_COLLECTION)
                    .document(roomId)
                    .collection("data")
                    .document(INFO_DOC)
                    .update("status", status)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to update room status", e);
            throw new RuntimeException("방 상태 변경에 실패했습니다", e);
        }
    }

    // ===== Game State Operations =====

    public void saveGameState(String roomId, Map<String, Object> state) {
        try {
            firestore.collection(ROOMS_COLLECTION)
                    .document(roomId)
                    .collection("data")
                    .document(STATE_DOC)
                    .set(state)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to save game state", e);
            throw new RuntimeException("게임 상태 저장에 실패했습니다", e);
        }
    }

    public Map<String, Object> getGameState(String roomId) {
        try {
            DocumentSnapshot doc = firestore.collection(ROOMS_COLLECTION)
                    .document(roomId)
                    .collection("data")
                    .document(STATE_DOC)
                    .get()
                    .get();

            return doc.exists() ? doc.getData() : null;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to get game state", e);
            throw new RuntimeException("게임 상태 조회에 실패했습니다", e);
        }
    }

    public void savePrivateState(String roomId, String playerId, Map<String, Object> privateState) {
        try {
            firestore.collection(ROOMS_COLLECTION)
                    .document(roomId)
                    .collection(PRIVATE_COLLECTION)
                    .document(playerId)
                    .set(privateState)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to save private state", e);
            throw new RuntimeException("비공개 상태 저장에 실패했습니다", e);
        }
    }

    public Map<String, Object> getPrivateState(String roomId, String playerId) {
        try {
            DocumentSnapshot doc = firestore.collection(ROOMS_COLLECTION)
                    .document(roomId)
                    .collection(PRIVATE_COLLECTION)
                    .document(playerId)
                    .get()
                    .get();

            return doc.exists() ? doc.getData() : null;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to get private state", e);
            throw new RuntimeException("비공개 상태 조회에 실패했습니다", e);
        }
    }
}
