package com.example.movietheater.controller;

import com.example.movietheater.entity.*;
import com.example.movietheater.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;
    private final RoomRepository roomRepository;
    private final GenreRepository genreRepository;
    private final BookingRepository bookingRepository;

    // ====================== DASHBOARD ======================

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {

        model.addAttribute("totalMovies", movieRepository.count());
        model.addAttribute("totalShowtimes", showtimeRepository.count());
        model.addAttribute("totalBookings", bookingRepository.count());

        return "admin/dashboard";
    }

    // ====================== QUẢN LÝ PHIM ======================

    @GetMapping("/movies")
    public String listMovies(Model model) {

        model.addAttribute("movies", movieRepository.findAll());

        return "admin/movie-list";
    }

    @GetMapping("/movies/add")
    public String showAddMovieForm(Model model) {

        model.addAttribute("movie", new Movie());
        model.addAttribute("genres", genreRepository.findAll());

        return "admin/movie-form";
    }

    @PostMapping("/movies/delete/{id}")   // ← Đổi thành PostMapping
    public String deleteMovie(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {
            Movie movie = movieRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phim với ID: " + id));

            // Kiểm tra có suất chiếu không
            long showtimeCount = showtimeRepository.countByMovieId(id);

            if (showtimeCount > 0) {
                redirectAttributes.addFlashAttribute("error",
                        "❌ Không thể xóa phim <strong>" + movie.getTitle() + "</strong> " +
                                "vì đang có " + showtimeCount + " suất chiếu.");
                return "redirect:/admin/movies";
            }

            // Được xóa
            movieRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success",
                    "✅ Đã xóa phim <strong>" + movie.getTitle() + "</strong> thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "❌ Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/movies";
    }


    // ====================== QUẢN LÝ SUẤT CHIẾU ======================

    @GetMapping("/showtimes")
    public String listShowtimes(Model model) {

        model.addAttribute("showtimes",
                showtimeRepository.findAll());

        return "admin/showtime-list";
    }

    @GetMapping("/showtimes/add")
    public String showAddShowtimeForm(Model model) {

        model.addAttribute("showtime", new Showtime());

        model.addAttribute("movies",
                movieRepository.findAll());

        model.addAttribute("rooms",
                roomRepository.findAll());

        return "admin/showtime-form";
    }

    @GetMapping("/showtimes/edit/{id}")
    public String editShowtime(
            @PathVariable Long id,
            Model model
    ) {

        Showtime showtime = showtimeRepository
                .findById(id)
                .orElseThrow();

        model.addAttribute("showtime", showtime);

        model.addAttribute("movies",
                movieRepository.findAll());

        model.addAttribute("rooms",
                roomRepository.findAll());

        return "admin/showtime-form";
    }

    // ====================== LƯU SUẤT CHIẾU ======================

    @PostMapping("/showtimes/save")
    public String saveShowtime(
            @ModelAttribute Showtime showtime,
            Model model
    ) {

        // Lấy movie thật từ DB
        Movie movie = movieRepository
                .findById(showtime.getMovie().getId())
                .orElseThrow();

        showtime.setMovie(movie);
        if (!movie.isActive()) {   // hoặc movie.getActive() == false
            model.addAttribute("error",
                    "❌ Không thể tạo suất chiếu cho phim '" + movie.getTitle() + "' vì phim đã ngừng hoạt động.");

            model.addAttribute("showtime", showtime);
            model.addAttribute("movies", movieRepository.findAll());
            model.addAttribute("rooms", roomRepository.findAll());
            return "admin/showtime-form";
        }

        // VALIDATE NULL
        if (showtime.getStartTime() == null) {

            model.addAttribute(
                    "error",
                    "Vui lòng chọn thời gian chiếu"
            );

            model.addAttribute("showtime", showtime);
            model.addAttribute("movies", movieRepository.findAll());
            model.addAttribute("rooms", roomRepository.findAll());

            return "admin/showtime-form";
        }

        // VALIDATE QUÁ KHỨ
        if (showtime.getStartTime()
                .isBefore(LocalDateTime.now())) {

            model.addAttribute(
                    "error",
                    "Không thể tạo suất chiếu trong quá khứ!"
            );

            model.addAttribute("showtime", showtime);
            model.addAttribute("movies", movieRepository.findAll());
            model.addAttribute("rooms", roomRepository.findAll());

            return "admin/showtime-form";
        }

        // TÍNH GIỜ KẾT THÚC
        showtime.setEndTime(
                showtime.getStartTime()
                        .plusMinutes(movie.getDuration())
        );

        // Lấy suất chiếu cùng phòng
        List<Showtime> existingShowtimes =
                showtimeRepository.findByRoomId(
                        showtime.getRoom().getId()
                );

        // CHECK XUNG ĐỘT
        for (Showtime existing : existingShowtimes) {

            // Bỏ qua chính nó khi edit
            if (showtime.getId() != null
                    && existing.getId().equals(showtime.getId())) {

                continue;
            }

            boolean isOverlap =

                    showtime.getStartTime()
                            .isBefore(existing.getEndTime())

                            &&

                            showtime.getEndTime()
                                    .isAfter(existing.getStartTime());

            if (isOverlap) {

                model.addAttribute(
                        "error",
                        "Phòng này đã có suất chiếu trong khoảng thời gian này!"
                );

                model.addAttribute("showtime", showtime);
                model.addAttribute("movies", movieRepository.findAll());
                model.addAttribute("rooms", roomRepository.findAll());

                return "admin/showtime-form";
            }
        }

        showtimeRepository.save(showtime);

        return "redirect:/admin/showtimes";
    }

    @GetMapping("/showtimes/delete/{id}")
    public String deleteShowtime(@PathVariable Long id) {

        showtimeRepository.deleteById(id);

        return "redirect:/admin/showtimes";
    }

    // ====================== QUẢN LÝ ĐẶT VÉ ======================

    @GetMapping("/bookings")
    public String listBookings(Model model) {

        model.addAttribute(
                "bookings",
                bookingRepository.findAll()
        );

        return "admin/booking-list";
    }

    @GetMapping("/bookings/{id}")
    public String bookingDetail(
            @PathVariable Long id,
            Model model
    ) {

        Booking booking = bookingRepository
                .findById(id)
                .orElseThrow();

        model.addAttribute("booking", booking);

        return "admin/booking-detail";
    }

    @GetMapping("/bookings/delete/{id}")
    public String deleteBooking(@PathVariable Long id) {

        bookingRepository.deleteById(id);

        return "redirect:/admin/bookings";
    }
}