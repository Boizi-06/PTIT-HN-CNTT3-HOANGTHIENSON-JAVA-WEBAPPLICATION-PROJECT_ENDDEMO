package com.example.movietheater.controller;

import com.example.movietheater.entity.*;
import com.example.movietheater.repository.*;
import com.example.movietheater.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
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
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final BookingService bookingService;

    // ====================== DASHBOARD ======================

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {

        // 1. Tổng số phim
        long totalMovies = movieRepository.count();

        // 2. Tổng số người dùng
        long totalUsers = userRepository.findAllNotAdmin().toArray().length;

        // 3. Vé đã bán trong tháng này
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDateTime startOfMonthTime = startOfMonth.atStartOfDay();

        long ticketsThisMonth = ticketRepository.countByBookingBookingTimeAfter(startOfMonthTime);

        // 4. Doanh thu tháng này
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

        // 5. Danh sách phim gần đây (8 phim mới nhất)
        List<Movie> recentMovies = movieRepository.findTop8ByOrderByReleaseDateDesc();



        model.addAttribute("totalMovies", totalMovies);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("ticketsThisMonth", ticketsThisMonth);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("movies", recentMovies);

        return "admin/dashboard";
    }

    // ====================== QUẢN LÝ PHIM ======================

    @GetMapping("/movies")
    public String listMovies(Model model) {

        model.addAttribute(
                "movies",
                movieRepository.findAllByOrderByReleaseDateDesc()
        );

        return "admin/movie-list";
    }

    // ====================== FORM THÊM PHIM ======================

    @GetMapping("/movies/add")
    public String showAddMovieForm(Model model) {

        model.addAttribute("movie", new Movie());
        model.addAttribute("genres", genreRepository.findAll());

        return "admin/movie-form";
    }

    // ====================== FORM SỬA PHIM ======================

    @GetMapping("/movies/edit/{id}")
    public String showEditMovieForm(
            @PathVariable Long id,
            Model model
    ) {

        Movie movie = movieRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy phim"));

        model.addAttribute("movie", movie);

        model.addAttribute(
                "genres",
                genreRepository.findAll()
        );

        return "admin/movie-form";
    }

    // ====================== LƯU PHIM ======================


    @PostMapping("/movies/save")
    public String saveMovie(
            @ModelAttribute Movie movie,
            @RequestParam("posterFile") MultipartFile posterFile,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        // CHECK ADD HAY EDIT
        boolean isNewMovie = (movie.getId() == null);

        // ================= LOAD MOVIE CŨ KHI EDIT =================

        Movie oldMovie = null;

        if (!isNewMovie) {

            oldMovie = movieRepository.findById(movie.getId())
                    .orElseThrow(() ->
                            new RuntimeException("Không tìm thấy phim"));

        }

        // ================= VALIDATE GENRE =================

        if (movie.getGenre() == null
                || movie.getGenre().getId() == null) {

            model.addAttribute(
                    "error",
                    "Vui lòng chọn thể loại phim!"
            );

            model.addAttribute("genres", genreRepository.findAll());

            return "admin/movie-form";
        }

        Genre genre = genreRepository
                .findById(movie.getGenre().getId())
                .orElseThrow();

        movie.setGenre(genre);

        // ================= VALIDATE TITLE =================

        if (movie.getTitle() == null
                || movie.getTitle().trim().isEmpty()) {

            model.addAttribute(
                    "error",
                    "Tên phim không được để trống!"
            );

            model.addAttribute("genres", genreRepository.findAll());

            return "admin/movie-form";
        }

        // ================= VALIDATE DURATION =================

        if (movie.getDuration() <= 0) {

            model.addAttribute(
                    "error",
                    "Thời lượng phim phải lớn hơn 0!"
            );

            model.addAttribute("genres", genreRepository.findAll());

            return "admin/movie-form";
        }

        if (movie.getDuration() > 500) {

            model.addAttribute(
                    "error",
                    "Thời lượng phim không được vượt quá 500 phút!"
            );

            model.addAttribute("genres", genreRepository.findAll());

            return "admin/movie-form";
        }

        // ================= VALIDATE RELEASE DATE =================

        if (movie.getReleaseDate() == null) {

            model.addAttribute(
                    "error",
                    "Vui lòng chọn ngày phát hành!"
            );

            model.addAttribute("genres", genreRepository.findAll());

            return "admin/movie-form";
        }

        if (movie.getReleaseDate()
                .isAfter(LocalDate.now().plusYears(5))) {

            model.addAttribute(
                    "error",
                    "Ngày phát hành không hợp lệ!"
            );

            model.addAttribute("genres", genreRepository.findAll());

            return "admin/movie-form";
        }

        // ================= VALIDATE TRAILER =================

        if (movie.getTrailerUrl() == null
                || movie.getTrailerUrl().trim().isEmpty()) {

            model.addAttribute(
                    "error",
                    "Trailer phim không được để trống!"
            );

            model.addAttribute("genres", genreRepository.findAll());

            return "admin/movie-form";
        }

        if (!movie.getTrailerUrl().startsWith("http://")
                && !movie.getTrailerUrl().startsWith("https://")) {

            model.addAttribute(
                    "error",
                    "Trailer URL phải bắt đầu bằng http:// hoặc https://"
            );

            model.addAttribute("genres", genreRepository.findAll());

            return "admin/movie-form";
        }

        // ================= VALIDATE DESCRIPTION =================

        if (movie.getDescription() == null
                || movie.getDescription().trim().isEmpty()) {

            model.addAttribute(
                    "error",
                    "Mô tả phim không được để trống!"
            );

            model.addAttribute("genres", genreRepository.findAll());

            return "admin/movie-form";
        }

        if (movie.getDescription().trim().length() < 20) {

            model.addAttribute(
                    "error",
                    "Mô tả phim phải tối thiểu 20 ký tự!"
            );

            model.addAttribute("genres", genreRepository.findAll());

            return "admin/movie-form";
        }

        if (movie.getDescription().trim().length() > 5000) {

            model.addAttribute(
                    "error",
                    "Mô tả phim không được vượt quá 5000 ký tự!"
            );

            model.addAttribute("genres", genreRepository.findAll());

            return "admin/movie-form";
        }

        // ================= XỬ LÝ POSTER =================

        try {

            // ===== NẾU UPLOAD FILE =====
            if (!posterFile.isEmpty()) {

                String fileName =
                        System.currentTimeMillis()
                                + "_"
                                + posterFile.getOriginalFilename();

                String uploadDir = "uploads/movies/";

                java.nio.file.Path uploadPath =
                        java.nio.file.Paths.get(uploadDir);

                if (!java.nio.file.Files.exists(uploadPath)) {

                    java.nio.file.Files.createDirectories(uploadPath);
                }

                java.nio.file.Path filePath =
                        uploadPath.resolve(fileName);

                posterFile.transferTo(filePath);

                movie.setPosterUrl(
                        "/uploads/movies/" + fileName
                );

            }

            // ===== KHÔNG UPLOAD FILE =====
            else {

                // EDIT -> GIỮ POSTER CŨ
                if (!isNewMovie && oldMovie != null) {

                    movie.setPosterUrl(
                            oldMovie.getPosterUrl()
                    );

                }

            }

        } catch (Exception e) {

            model.addAttribute(
                    "error",
                    "Lỗi upload ảnh: " + e.getMessage()
            );

            model.addAttribute("genres", genreRepository.findAll());

            return "admin/movie-form";
        }

        // ================= VALIDATE POSTER =================

        if (movie.getPosterUrl() == null
                || movie.getPosterUrl().trim().isEmpty()) {

            model.addAttribute(
                    "error",
                    "Poster phim không được để trống!"
            );

            model.addAttribute("genres", genreRepository.findAll());

            return "admin/movie-form";
        }

        // CHECK URL nếu là LINK ONLINE
        if (movie.getPosterUrl().startsWith("http")
                && !movie.getPosterUrl().startsWith("http://")
                && !movie.getPosterUrl().startsWith("https://")) {

            model.addAttribute(
                    "error",
                    "Poster URL không hợp lệ!"
            );

            model.addAttribute("genres", genreRepository.findAll());

            return "admin/movie-form";
        }

        // ================= SAVE =================

        movieRepository.save(movie);

        // ================= SUCCESS =================

        if (isNewMovie) {

            redirectAttributes.addFlashAttribute(
                    "success",
                    "✅ Thêm phim thành công!"
            );

        } else {

            redirectAttributes.addFlashAttribute(
                    "success",
                    "✅ Cập nhật phim thành công!"
            );
        }

        return "redirect:/admin/movies";
    }
    // ====================== XÓA PHIM ======================

    @PostMapping("/movies/delete/{id}")
    public String deleteMovie(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {

        try {

            Movie movie = movieRepository.findById(id)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Không tìm thấy phim với ID: " + id
                            ));

            // CHECK SUẤT CHIẾU
            long showtimeCount =
                    showtimeRepository.countByMovieId(id);

            if (showtimeCount > 0) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "❌ Không thể xóa phim <strong>"
                                + movie.getTitle()
                                + "</strong> vì đang có "
                                + showtimeCount
                                + " suất chiếu."
                );

                return "redirect:/admin/movies";
            }

            movieRepository.deleteById(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "✅ Đã xóa phim <strong>"
                            + movie.getTitle()
                            + "</strong> thành công!"
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "❌ Có lỗi xảy ra: " + e.getMessage()
            );
        }

        return "redirect:/admin/movies";
    }

    // ====================== QUẢN LÝ SUẤT CHIẾU ======================

    @GetMapping("/showtimes")
    public String listShowtimes(Model model) {

        model.addAttribute(
                "showtimes",
                showtimeRepository.findAll()
        );

        return "admin/showtime-list";
    }

    @GetMapping("/showtimes/add")
    public String showAddShowtimeForm(Model model) {

        model.addAttribute("showtime", new Showtime());

        model.addAttribute(
                "movies",
                movieRepository.findAllByOrderByReleaseDateDesc()
        );

        model.addAttribute(
                "rooms",
                roomRepository.findAll()
        );

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

        model.addAttribute(
                "movies",
                movieRepository.findAll()
        );

        model.addAttribute(
                "rooms",
                roomRepository.findAll()
        );

        return "admin/showtime-form";
    }

    // ====================== LƯU SUẤT CHIẾU ======================

    @PostMapping("/showtimes/save")
    public String saveShowtime(
            @ModelAttribute Showtime showtime,
            Model model
    ) {

        Movie movie = movieRepository
                .findById(showtime.getMovie().getId())
                .orElseThrow();

        showtime.setMovie(movie);

        // ================= VALIDATE START TIME =================

        if (showtime.getStartTime() == null) {

            model.addAttribute(
                    "error",
                    "Vui lòng chọn thời gian chiếu!"
            );

            model.addAttribute("showtime", showtime);
            model.addAttribute("movies", movieRepository.findAll());
            model.addAttribute("rooms", roomRepository.findAll());

            return "admin/showtime-form";
        }

        // ================= CHECK RELEASE DATE =================

        if (movie.getReleaseDate() != null
                && showtime.getStartTime()
                .toLocalDate()
                .isBefore(movie.getReleaseDate())) {

            model.addAttribute(
                    "error",
                    "Không thể tạo suất chiếu trước ngày phát hành của phim!"
            );

            model.addAttribute("showtime", showtime);
            model.addAttribute("movies", movieRepository.findAll());
            model.addAttribute("rooms", roomRepository.findAll());

            return "admin/showtime-form";
        }

        // ================= CHECK ACTIVE =================

        if (!movie.isActive()) {

            model.addAttribute(
                    "error",
                    "❌ Không thể tạo suất chiếu cho phim '"
                            + movie.getTitle()
                            + "' vì phim đã ngừng hoạt động."
            );

            model.addAttribute("showtime", showtime);
            model.addAttribute("movies", movieRepository.findAll());
            model.addAttribute("rooms", roomRepository.findAll());

            return "admin/showtime-form";
        }

        // ================= CHECK PAST =================

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

        // ================= CHECK ROOM =================

        if (showtime.getRoom() == null
                || showtime.getRoom().getId() == null) {

            model.addAttribute(
                    "error",
                    "Vui lòng chọn phòng chiếu!"
            );

            model.addAttribute("showtime", showtime);
            model.addAttribute("movies", movieRepository.findAll());
            model.addAttribute("rooms", roomRepository.findAll());

            return "admin/showtime-form";
        }

        // ================= TÍNH END TIME =================

        showtime.setEndTime(
                showtime.getStartTime()
                        .plusMinutes(movie.getDuration())
        );

        // ================= CHECK OVERLAP =================

        List<Showtime> existingShowtimes =
                showtimeRepository.findByRoomId(
                        showtime.getRoom().getId()
                );

        for (Showtime existing : existingShowtimes) {

            // BỎ QUA CHÍNH NÓ KHI EDIT
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

        // ================= SAVE =================

        showtimeRepository.save(showtime);

        return "redirect:/admin/showtimes";
    }

    // ====================== XÓA SUẤT CHIẾU ======================

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