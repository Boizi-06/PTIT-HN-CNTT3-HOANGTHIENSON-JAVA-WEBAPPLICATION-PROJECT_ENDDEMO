package com.example.movietheater.controller;

import com.example.movietheater.entity.Movie;
import com.example.movietheater.entity.Showtime;
import com.example.movietheater.repository.MovieRepository;
import com.example.movietheater.repository.ShowtimeRepository;
import com.example.movietheater.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/movies")
public class MovieController {

    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;
    private final TicketRepository ticketRepository;

    // Danh sách phim
    @GetMapping
    public String movieList(Model model) {

        model.addAttribute(
                "movies",
                movieRepository.findByActiveTrueOrderByReleaseDateDesc()
        );

        return "user/home";
    }

    // Chi tiết phim
    @GetMapping("/{id}")
    public String movieDetail(
            @PathVariable Long id,
            Model model
    ) {

        Movie movie = movieRepository.findById(id)
                .orElseThrow();

        List<Showtime> allShowtimes =
                showtimeRepository.findByMovieId(id);

        // FILTER SUẤT CHIẾU
        List<Showtime> validShowtimes =
                allShowtimes.stream()

                        // ACTIVE
                        .filter(Showtime::isActive)

                        // CHƯA ĐẾN GIỜ CHIẾU
                        .filter(showtime ->
                                showtime.getStartTime()
                                        .isAfter(LocalDateTime.now()))

                        // CHƯA FULL GHẾ
                        .filter(showtime -> {

                            long bookedSeats =
                                    ticketRepository.countByShowtimeId(
                                            showtime.getId()
                                    );

                            int totalSeats =
                                    showtime.getRoom()
                                            .getSeats()
                                            .size();

                            return bookedSeats < totalSeats;
                        })

                        .toList();

        model.addAttribute("movie", movie);

        model.addAttribute(
                "showtimes",
                validShowtimes
        );

        return "user/movie-detail";
    }
}