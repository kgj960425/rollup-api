package com.rollup.rollupapi.common.service;

import com.rollup.rollupapi.common.dto.request.RegisterRequest;
import com.rollup.rollupapi.common.dto.response.UserResponse;
import com.rollup.rollupapi.common.dto.response.UserStatsResponse;
import com.rollup.rollupapi.common.entity.User;
import com.rollup.rollupapi.common.entity.UserGameStats;
import com.rollup.rollupapi.common.exception.ResourceNotFoundException;
import com.rollup.rollupapi.common.repository.UserGameStatsRepository;
import com.rollup.rollupapi.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserGameStatsRepository userGameStatsRepository;

    @Transactional
    public UserResponse registerUser(String firebaseUid, RegisterRequest request) {
        // 이미 등록된 유저인지 확인
        if (userRepository.existsByFirebaseUid(firebaseUid)) {
            User existing = userRepository.findByFirebaseUid(firebaseUid)
                    .orElseThrow();
            return UserResponse.from(existing);
        }

        User user = User.builder()
                .firebaseUid(firebaseUid)
                .nickname(request.getNickname())
                .email(request.getEmail())
                .build();

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(String firebaseUid) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다"));
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public User getUserEntity(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다"));
    }

    @Transactional(readOnly = true)
    public List<UserStatsResponse> getUserStats(String firebaseUid) {
        User user = getUserEntity(firebaseUid);
        List<UserGameStats> stats = userGameStatsRepository.findByUser(user);
        return stats.stream()
                .map(UserStatsResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserStatsResponse getUserStatsByGameType(String firebaseUid, String gameType) {
        User user = getUserEntity(firebaseUid);
        UserGameStats stats = userGameStatsRepository.findByUserAndGameType(user, gameType)
                .orElse(UserGameStats.builder()
                        .user(user)
                        .gameType(gameType)
                        .build());
        return UserStatsResponse.from(stats);
    }

    @Transactional
    public void updateUserStats(String firebaseUid, String gameType, int score, boolean isWinner) {
        User user = getUserEntity(firebaseUid);

        UserGameStats stats = userGameStatsRepository.findByUserAndGameType(user, gameType)
                .orElse(UserGameStats.builder()
                        .user(user)
                        .gameType(gameType)
                        .build());

        stats.setGamesPlayed(stats.getGamesPlayed() + 1);
        stats.setTotalScore(stats.getTotalScore() + score);

        if (isWinner) {
            stats.setGamesWon(stats.getGamesWon() + 1);
        }

        if (stats.getBestScore() == null || score > stats.getBestScore()) {
            stats.setBestScore(score);
        }

        userGameStatsRepository.save(stats);
    }
}
