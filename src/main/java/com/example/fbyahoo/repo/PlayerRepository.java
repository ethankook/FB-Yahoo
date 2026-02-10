package com.example.fbyahoo.repo;

import com.example.fbyahoo.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, String> {

    Optional<Player> findByPlayerKey(String playerKey);

    List<Player> findByNameFullContainingIgnoreCase(String q);
}