package com.rollup.rollupapi.common.repository;

import com.rollup.rollupapi.common.entity.GameLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameLogRepository extends JpaRepository<GameLog, Long> {

    List<GameLog> findByRoomIdOrderByCreatedAtAsc(String roomId);

    List<GameLog> findByRoomIdAndGameType(String roomId, String gameType);
}
