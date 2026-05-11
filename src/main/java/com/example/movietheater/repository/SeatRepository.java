package com.example.movietheater.repository;

import com.example.movietheater.entity.Seat;
import com.example.movietheater.eenum.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository
        extends JpaRepository<Seat, Long> {

    // Ghế theo phòng
    List<Seat> findByRoomId(Long roomId);

    // Ghế theo trạng thái
    List<Seat> findByRoomIdAndStatus(
            Long roomId,
            SeatStatus status
    );

    // LOCK ghế khi đặt vé
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Seat> findWithLockingById(Long id);
}