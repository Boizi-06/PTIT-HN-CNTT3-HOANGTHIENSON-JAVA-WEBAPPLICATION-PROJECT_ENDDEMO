package com.example.movietheater.repository;

import com.example.movietheater.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByBookingId(Long bookingId);

    List<Ticket> findBySeatId(Long seatId);

    boolean existsByShowtimeIdAndSeatId(
            Long showtimeId,
            Long seatId
    );
    @Query("""
        SELECT t.seat.id 
        FROM Ticket t 
        WHERE t.showtime.id = :showtimeId
    """)
    List<Long> findBookedSeatIdsByShowtimeId(@Param("showtimeId") Long showtimeId);
    void deleteByBookingId(Long bookingId);

    long countByShowtimeId(Long showtimeId);
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.booking.bookingTime >= :startTime")
    long countByBookingBookingTimeAfter(@Param("startTime") LocalDateTime startTime);
}