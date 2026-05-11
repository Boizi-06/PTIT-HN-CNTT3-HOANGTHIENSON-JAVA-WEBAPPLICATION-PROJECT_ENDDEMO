package com.example.movietheater.repository;

import com.example.movietheater.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    List<Showtime> findByActiveTrueAndStartTimeAfter(LocalDateTime now);

    // Kiểm tra xung đột phòng
    boolean existsByRoomIdAndStartTimeBetween(Long roomId, LocalDateTime start, LocalDateTime end);
    long countByMovieId(Long movieId);
    List<Showtime> findByMovieId(Long movieId);
    List<Showtime> findByRoomId(Long roomId);
}