package com.rollup.rollupapi.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameType {

    @Id
    private String id;  // 'yacht', 'lexio', 등

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "min_players", nullable = false)
    private int minPlayers;

    @Column(name = "max_players", nullable = false)
    private int maxPlayers;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
