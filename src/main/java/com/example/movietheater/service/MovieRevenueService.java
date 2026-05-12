package com.example.movietheater.service;

import com.example.movietheater.dto.MovieRevenueDTO;
import com.example.movietheater.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieRevenueService {

    private final BookingRepository bookingRepository;

    public List<MovieRevenueDTO> getRevenueByMovie() {

        return bookingRepository.getRevenueByMovie()
                .stream()
                .map(o -> new MovieRevenueDTO(
                        ((Number) o[0]).longValue(),
                        (String) o[1],
                        o[2] == null ? 0.0 : ((Number) o[2]).doubleValue()
                ))
                .toList();
    }
}