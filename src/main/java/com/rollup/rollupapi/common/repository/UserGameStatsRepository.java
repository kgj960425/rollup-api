package com.rollup.rollupapi.common.repository;

import com.rollup.rollupapi.common.entity.User;
import com.rollup.rollupapi.common.entity.UserGameStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserGameStatsRepository extends JpaRepository<UserGameStats, UUID> {

    List<UserGameStats> findByUser(User user);

    Optional<UserGameStats> findByUserAndGameType(User user, String gameType);

    @Query("SELECT s FROM UserGameStats s WHERE s.gameType = :gameType ORDER BY " +
            "CASE WHEN s.gamesPlayed > 0 THEN CAST(s.gamesWon AS double) / s.gamesPlayed ELSE 0 END DESC")
    List<UserGameStats> findTopByGameTypeOrderByWinRate(@Param("gameType") String gameType);

    @Query("SELECT s FROM UserGameStats s WHERE s.gameType = :gameType AND s.bestScore IS NOT NULL " +
            "ORDER BY s.bestScore DESC")
    List<UserGameStats> findTopByGameTypeOrderByBestScore(@Param("gameType") String gameType);
}
