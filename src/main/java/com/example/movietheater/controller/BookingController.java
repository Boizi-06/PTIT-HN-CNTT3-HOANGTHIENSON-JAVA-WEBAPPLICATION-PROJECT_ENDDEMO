package com.example.movietheater.controller;

import com.example.movietheater.eenum.SeatStatus;
import com.example.movietheater.entity.Booking;
import com.example.movietheater.entity.Seat;
import com.example.movietheater.entity.Showtime;
import com.example.movietheater.entity.User;
import com.example.movietheater.repository.SeatRepository;
import com.example.movietheater.repository.ShowtimeRepository;
import com.example.movietheater.repository.UserRepository;
import com.example.movietheater.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/booking")
public class BookingController {

    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final BookingService bookingService;

    // =========================
    // TRANG CHỌN GHẾ
    // =========================

    @GetMapping("/{showtimeId}")
    public String bookingPage(
            @PathVariable Long showtimeId,
            Model model) {

        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy suất chiếu!"));

        // Lấy tất cả ghế của phòng
        List<Seat> seats = seatRepository.findByRoomId(
                showtime.getRoom().getId()
        );

        // LẤY DANH SÁCH GHẾ ĐÃ ĐẶT
        List<Long> bookedSeatIds =
                bookingService.getBookedSeatIdsForShowtime(showtimeId);

        // CẬP NHẬT TRẠNG THÁI GHẾ
        for (Seat seat : seats) {

            if (bookedSeatIds.contains(seat.getId())) {

                seat.setStatus(SeatStatus.BOOKED);

            } else {

                seat.setStatus(SeatStatus.AVAILABLE);
            }
        }

        model.addAttribute("showtime", showtime);
        model.addAttribute("seats", seats);

        return "user/booking";
    }

    // =========================
    // TRANG XÁC NHẬN ĐẶT VÉ
    // =========================

    @PostMapping("/confirm")
    public String confirmBooking(
            @RequestParam Long showtimeId,
            @RequestParam List<Long> seatIds,
            Model model
    ) {

        Showtime showtime = showtimeRepository
                .findById(showtimeId)
                .orElseThrow();

        List<Seat> selectedSeats =
                seatRepository.findAllById(seatIds);

        double totalPrice = 0;

        for (Seat seat : selectedSeats) {

            String seatName = seat.getSeatName();

            // VIP
            if (seatName.startsWith("C")
                    || seatName.startsWith("D")) {

                totalPrice += 120000;

            }

            // COUPLE
            else if (seatName.startsWith("E")) {

                totalPrice += 180000;

            }

            // STANDARD
            else {

                totalPrice += 80000;
            }
        }

        model.addAttribute("showtime", showtime);

        model.addAttribute(
                "selectedSeats",
                selectedSeats
        );

        model.addAttribute(
                "seatIds",
                seatIds
        );

        model.addAttribute(
                "totalPrice",
                totalPrice
        );

        return "user/booking-confirm";
    }

    // =========================
    // THANH TOÁN
    // =========================

    @PostMapping("/payment")
    public String payment(
            @RequestParam Long showtimeId,
            @RequestParam List<Long> seatIds,
            Authentication authentication
    ) {

        User user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow();

        Booking booking = bookingService.bookSeats(
                user,
                showtimeId,
                seatIds
        );

        return "redirect:/booking/success/" + booking.getId();
    }

    // =========================
    // TRANG SUCCESS
    // =========================

    @GetMapping("/success/{bookingId}")
    public String bookingSuccess(
            @PathVariable Long bookingId,
            Model model
    ) {

        model.addAttribute("bookingId", bookingId);

        return "user/booking-success";
    }
}