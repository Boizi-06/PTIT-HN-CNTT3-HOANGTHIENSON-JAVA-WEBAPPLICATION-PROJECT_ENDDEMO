package com.example.movietheater.service;

import com.example.movietheater.entity.*;
import com.example.movietheater.eenum.BookingStatus;
import com.example.movietheater.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public Booking bookSeats(
            User user,
            Long showtimeId,
            List<Long> seatIds
    ) {

        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy suất chiếu"));


        // CHECK SUẤT CHIẾU HẾT HẠN
        if (!showtime.isActive()
                || showtime.getEndTime().isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "Suất chiếu đã kết thúc hoặc không hoạt động"
            );
        }

        Booking booking = Booking.builder()
                .user(user)
                .showtime(showtime)
                .bookingTime(LocalDateTime.now())
                .status(BookingStatus.CONFIRMED)
                .build();

        booking = bookingRepository.save(booking);

        List<Ticket> tickets = new ArrayList<>();

        double totalAmount = 0;

        for (Long seatId : seatIds) {

            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() ->
                            new RuntimeException("Ghế không tồn tại"));

            // CHECK GHẾ ĐÃ ĐẶT TRONG SUẤT CHIẾU NÀY CHƯA
            boolean alreadyBooked =
                    ticketRepository.existsByShowtimeIdAndSeatId(
                            showtimeId,
                            seatId
                    );

            if (alreadyBooked) {

                throw new RuntimeException(
                        "Ghế " + seat.getSeatName()
                                + " đã được đặt"
                );
            }

            Ticket ticket = Ticket.builder()
                    .booking(booking)
                    .seat(seat)
                    .showtime(showtime)
                    .price(80000)
                    .build();

            tickets.add(ticket);

            totalAmount += 80000;
        }

        ticketRepository.saveAll(tickets);

        booking.setTickets(tickets);
        booking.setTotalAmount(totalAmount);

        Booking savedBooking =
                bookingRepository.save(booking);

        // AUTO HIDE SHOWTIME NẾU HẾT GHẾ
        autoDisableShowtime(showtime);

        return savedBooking;
    }

    // =========================
    // AUTO DISABLE SHOWTIME
    // =========================

    private void autoDisableShowtime(Showtime showtime) {

        int totalSeats =
                showtime.getRoom().getSeats().size();

        int bookedSeats =
                ticketRepository.findAll()
                        .stream()
                        .filter(ticket ->
                                ticket.getShowtime().getId()
                                        .equals(showtime.getId()))
                        .toList()
                        .size();

        // HẾT GHẾ
        if (bookedSeats >= totalSeats) {

            showtime.setActive(false);

            showtimeRepository.save(showtime);
        }

        // QUÁ GIỜ
        if (showtime.getEndTime()
                .isBefore(LocalDateTime.now())) {

            showtime.setActive(false);

            showtimeRepository.save(showtime);
        }
    }
    // Thêm method này
    public List<Long> getBookedSeatIdsForShowtime(Long showtimeId) {
        return ticketRepository.findBookedSeatIdsByShowtimeId(showtimeId);
    }
}