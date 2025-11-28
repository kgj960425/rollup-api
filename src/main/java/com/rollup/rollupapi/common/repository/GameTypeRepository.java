package com.rollup.rollupapi.common.repository;

import com.rollup.rollupapi.common.entity.GameType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameTypeRepository extends JpaRepository<GameType, String> {

    List<GameType> findByIsActiveTrue();
}
