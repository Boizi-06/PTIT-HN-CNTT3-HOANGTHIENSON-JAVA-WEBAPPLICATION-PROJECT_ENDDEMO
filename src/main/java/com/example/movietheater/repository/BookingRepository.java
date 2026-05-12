package com.example.movietheater.repository;

import com.example.movietheater.entity.Booking;
import com.example.movietheater.eenum.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByUserIdOrderByBookingTimeDesc(Long userId);


    @Query("""
        SELECT DISTINCT b 
        FROM Booking b 
        LEFT JOIN FETCH b.tickets t 
        LEFT JOIN FETCH t.seat 
        LEFT JOIN FETCH b.showtime s 
        LEFT JOIN FETCH s.movie 
        LEFT JOIN FETCH s.room 
        WHERE b.user.id = :userId 
        ORDER BY b.bookingTime DESC
        """)
    List<Booking> findByUserIdWithTickets(Long userId);


    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.bookingTime >= :startTime AND b.status = 'CONFIRMED'")
    Double calculateRevenueThisMonth(@Param("startTime") LocalDateTime startTime);
}