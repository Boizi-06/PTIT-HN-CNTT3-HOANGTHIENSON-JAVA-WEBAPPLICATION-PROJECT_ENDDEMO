package com.example.movietheater.controller;

import com.example.movietheater.eenum.BookingStatus;
import com.example.movietheater.eenum.SeatStatus;
import com.example.movietheater.entity.Booking;
import com.example.movietheater.entity.Seat;
import com.example.movietheater.entity.Showtime;
import com.example.movietheater.entity.User;
import com.example.movietheater.repository.*;
import com.example.movietheater.service.BookingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/booking")
public class BookingController {

    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;




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
            @RequestParam(required = false) List<Long> seatIds,
            Model model
    ) {

        Showtime showtime = showtimeRepository
                .findById(showtimeId)
                .orElseThrow();

        // ❗ FIX LỖI KHÔNG CHỌN GHẾ
        if (seatIds == null || seatIds.isEmpty()) {
            model.addAttribute("showtime", showtime);
            model.addAttribute("seats", seatRepository.findByRoomId(showtime.getRoom().getId()));
            model.addAttribute("error", "Bạn chưa chọn ghế nào!");
            return "user/booking";
        }

        List<Seat> selectedSeats =
                seatRepository.findAllById(seatIds);

        double totalPrice = 0;

        for (Seat seat : selectedSeats) {

            String seatName = seat.getSeatName();

            if (seatName.startsWith("C") || seatName.startsWith("D")) {
                totalPrice += 120000;
            }
            else if (seatName.startsWith("E")) {
                totalPrice += 180000;
            }
            else {
                totalPrice += 80000;
            }
        }

        model.addAttribute("showtime", showtime);
        model.addAttribute("selectedSeats", selectedSeats);
        model.addAttribute("seatIds", seatIds);
        model.addAttribute("totalPrice", totalPrice);

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
    // =========================
// LỊCH SỬ ĐẶT VÉ
// =========================

    @GetMapping("/history")
    public String bookingHistory(
            Authentication authentication,
            Model model
    ) {

        User user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow();

        // Dùng query có fetch join
        List<Booking> bookings = bookingRepository
                .findByUserIdWithTickets(user.getId());

        model.addAttribute("bookings", bookings);

        return "user/booking-history";
    }




    // =========================
// HỦY BOOKING
// =========================

    @PostMapping("/cancel/{bookingId}")
    @Transactional
    public String cancelBooking(
            @PathVariable Long bookingId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {

        User user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow();

        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow();

        // CHECK ĐÚNG USER
        if (!booking.getUser().getId().equals(user.getId())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Bạn không có quyền hủy vé này!"
            );

            return "redirect:/booking/history";
        }

        // CHECK ĐÃ HỦY CHƯA
        if (booking.getStatus() == BookingStatus.CANCELLED) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Vé này đã bị hủy trước đó!"
            );

            return "redirect:/booking/history";
        }

        // CHECK QUÁ 24H
        if (booking.getBookingTime()
                .plusDays(1)
                .isBefore(LocalDateTime.now())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Đã quá thời gian hủy vé!"
            );

            return "redirect:/booking/history";
        }

        // ===== XÓA TICKET =====

        ticketRepository.deleteByBookingId(
                booking.getId()
        );

        // ===== UPDATE STATUS =====

        booking.setStatus(BookingStatus.CANCELLED);

        bookingRepository.save(booking);

        // ===== MỞ LẠI SUẤT CHIẾU =====

        Showtime showtime = booking.getShowtime();

        showtime.setActive(true);

        showtimeRepository.save(showtime);

        redirectAttributes.addFlashAttribute(
                "success",
                "Hủy vé thành công!"
        );

        return "redirect:/booking/history";
    }
}