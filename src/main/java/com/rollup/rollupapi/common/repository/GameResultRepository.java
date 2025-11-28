package com.rollup.rollupapi.common.repository;

import com.rollup.rollupapi.common.entity.GameResult;
import com.rollup.rollupapi.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GameResultRepository extends JpaRepository<GameResult, UUID> {

    List<GameResult> findByGameTypeOrderByFinishedAtDesc(String gameType);

    List<GameResult> findByWinner(User winner);

    List<GameResult> findByRoomId(String roomId);
}
