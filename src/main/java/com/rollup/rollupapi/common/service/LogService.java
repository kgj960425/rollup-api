package com.rollup.rollupapi.common.service;

import com.rollup.rollupapi.common.entity.GameLog;
import com.rollup.rollupapi.common.entity.GameResult;
import com.rollup.rollupapi.common.entity.User;
import com.rollup.rollupapi.common.repository.GameLogRepository;
import com.rollup.rollupapi.common.repository.GameResultRepository;
import com.rollup.rollupapi.common.repository.UserRepository;
import com.rollup.rollupapi.games.core.GameResultData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {

    private final GameLogRepository gameLogRepository;
    private final GameResultRepository gameResultRepository;
    private final UserRepository userRepository;

    @Async
    @Transactional
    public void saveGameLog(String roomId, String gameType, String userId, String eventType, Map<String, Object> payload) {
        try {
            User user = userRepository.findByFirebaseUid(userId).orElse(null);

            GameLog gameLog = GameLog.builder()
                    .roomId(roomId)
                    .gameType(gameType)
                    .user(user)
                    .eventType(eventType)
                    .eventData(payload)
                    .build();

            gameLogRepository.save(gameLog);
        } catch (Exception e) {
            log.error("Failed to save game log", e);
        }
    }

    @Transactional
    public void saveGameResult(String roomId, String gameType, GameResultData resultData) {
        try {
            User winner = null;
            if (resultData.getWinnerId() != null) {
                winner = userRepository.findByFirebaseUid(resultData.getWinnerId()).orElse(null);
            }

            List<Map<String, Object>> players = resultData.getPlayerResults().stream()
                    .map(pr -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("userId", pr.getPlayerId());
                        map.put("nickname", pr.getNickname());
                        map.put("score", pr.getScore());
                        map.put("rank", pr.getRank());
                        return map;
                    })
                    .collect(Collectors.toList());

            GameResult gameResult = GameResult.builder()
                    .roomId(roomId)
                    .gameType(gameType)
                    .winner(winner)
                    .players(players)
                    .build();

            gameResultRepository.save(gameResult);
        } catch (Exception e) {
            log.error("Failed to save game result", e);
        }
    }
}
