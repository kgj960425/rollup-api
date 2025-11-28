package com.rollup.rollupapi.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user_game_stats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "game_type"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGameStats {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "game_type", nullable = false)
    private String gameType;

    @Column(name = "games_played")
    @Builder.Default
    private int gamesPlayed = 0;

    @Column(name = "games_won")
    @Builder.Default
    private int gamesWon = 0;

    @Column(name = "total_score")
    @Builder.Default
    private long totalScore = 0L;

    @Column(name = "best_score")
    private Integer bestScore;
}
