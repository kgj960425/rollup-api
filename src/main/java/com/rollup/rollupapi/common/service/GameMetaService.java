package com.rollup.rollupapi.common.service;

import com.rollup.rollupapi.common.dto.response.GameTypeResponse;
import com.rollup.rollupapi.common.entity.GameType;
import com.rollup.rollupapi.common.exception.ResourceNotFoundException;
import com.rollup.rollupapi.common.repository.GameTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameMetaService {

    private final GameTypeRepository gameTypeRepository;

    @Transactional(readOnly = true)
    public List<GameTypeResponse> getActiveGames() {
        return gameTypeRepository.findByIsActiveTrue().stream()
                .map(GameTypeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public GameTypeResponse getGameByType(String gameType) {
        GameType game = gameTypeRepository.findById(gameType)
                .orElseThrow(() -> new ResourceNotFoundException("게임을 찾을 수 없습니다: " + gameType));
        return GameTypeResponse.from(game);
    }

    @Transactional(readOnly = true)
    public boolean isValidGameType(String gameType) {
        return gameTypeRepository.existsById(gameType);
    }
}
