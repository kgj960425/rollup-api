package com.rollup.rollupapi.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "game_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameResult {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "room_id", nullable = false)
    private String roomId;

    @Column(name = "game_type", nullable = false)
    private String gameType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "players", columnDefinition = "jsonb")
    private List<Map<String, Object>> players;  // [{userId, nickname, score, rank}, ...]

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @CreationTimestamp
    @Column(name = "finished_at", updatable = false)
    private LocalDateTime finishedAt;
}
