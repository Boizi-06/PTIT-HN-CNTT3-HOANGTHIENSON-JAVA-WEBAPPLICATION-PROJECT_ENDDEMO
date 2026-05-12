package com.example.movietheater.controller;

import com.example.movietheater.service.MovieRevenueService;
import com.example.movietheater.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/revenue")
public class AdminRevenueController {

    private final MovieRevenueService movieRevenueService;
    private final BookingRepository bookingRepository;

    // =========================
    // PAGE 1: DOANH THU
    // =========================

    @GetMapping
    public String revenuePage(Model model) {

        // Doanh thu theo phim
        model.addAttribute(
                "revenueByMovie",
                movieRevenueService.getRevenueByMovie()
        );

        // Tổng doanh thu hệ thống
        Double totalRevenue =
                bookingRepository.getTotalSystemRevenue();

        // Nếu null -> 0
        if (totalRevenue == null) {

            totalRevenue = 0.0;
        }

        model.addAttribute(
                "totalRevenue",
                totalRevenue
        );

        return "admin/revenue";
    }

    // =========================
    // PAGE 2: CHI TIẾT BOOKING
    // =========================

    @GetMapping("/movie/{movieId}")
    public String movieRevenueDetail(
            @PathVariable Long movieId,
            Model model
    ) {

        model.addAttribute(
                "bookings",
                bookingRepository.findByMovieId(movieId)
        );

        return "admin/revenue-detail";
    }
}