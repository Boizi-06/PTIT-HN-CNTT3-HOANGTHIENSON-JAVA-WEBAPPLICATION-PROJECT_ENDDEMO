package com.example.movietheater.config;

import com.example.movietheater.entity.*;
import com.example.movietheater.eenum.*;           // ← SỬA: enum (không phải eenum)
        import com.example.movietheater.repository.*;
        import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final GenreRepository genreRepository;
    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final MovieRepository movieRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        if (userRepository.count() > 0) {
            System.out.println("✅ Dữ liệu đã được seed trước đó.");
            return;
        }

        System.out.println("🚀 Đang seed dữ liệu mẫu...");

        // 1. Seed Genres
        List<Genre> genres = new ArrayList<>();
        genres.add(Genre.builder().name("Hành Động").build());
        genres.add(Genre.builder().name("Kinh Dị").build());
        genres.add(Genre.builder().name("Tình Cảm").build());
        genres.add(Genre.builder().name("Khoa Học Viễn Tưởng").build());
        genres.add(Genre.builder().name("Hài").build());
        genreRepository.saveAll(genres);

        // 2. Seed Rooms + Seats
        createRoomWithSeats("Phòng 1");
        createRoomWithSeats("Phòng 2");
        createRoomWithSeats("Phòng 3");

        // 3. Seed Users
        createUserWithProfile("admin", "admin123", Role.ADMIN,
                "Nguyễn Văn Admin", "admin@cinema.com", "0123456789");

        createUserWithProfile("staff", "staff123", Role.STAFF,
                "Trần Thị Staff", "staff@cinema.com", "0987654321");

        createUserWithProfile("user", "user123", Role.USER,
                "Lê Văn User", "user@gmail.com", "0912345678");

        // 4. Seed Movies
        Movie movie1 = Movie.builder()
                .title("Avengers: Endgame")
                .description("Siêu anh hùng cứu vũ trụ")
                .duration(181)
                .posterUrl("https://picsum.photos/id/1015/300/400")
                .releaseDate(LocalDate.of(2025, 5, 1))
                .genre(genres.get(0))
                .active(true)
                .build();

        Movie movie2 = Movie.builder()
                .title("The Conjuring")
                .description("Phim kinh dị nổi tiếng")
                .duration(112)
                .posterUrl("https://picsum.photos/id/201/300/400")
                .releaseDate(LocalDate.of(2025, 4, 15))
                .genre(genres.get(1))
                .active(true)
                .build();

        movieRepository.saveAll(List.of(movie1, movie2));

        System.out.println("✅ Seed dữ liệu thành công!");
        System.out.println("👤 Tài khoản test:");
        System.out.println("Admin  → admin / admin123");
        System.out.println("Staff  → staff / staff123");
        System.out.println("User   → user / user123");
    }

    private void createRoomWithSeats(String roomName) {

        Room room = Room.builder()
                .name(roomName)
                .capacity(50)
                .build();

        roomRepository.save(room);

        List<Seat> seats = new ArrayList<>();

        char[] rows = {'A', 'B', 'C', 'D', 'E'};

        int seatsPerRow = 10;

        for (int r = 0; r < rows.length; r++) {

            char row = rows[r];

            for (int num = 1; num <= seatsPerRow; num++) {

                SeatType seatType;

                // HÀNG E → COUPLE

                if (row == 'E') {

                    seatType = SeatType.COUPLE;

                }

                // HÀNG C + D → VIP

                else if (row == 'C' || row == 'D') {

                    seatType = SeatType.VIP;

                }

                // A + B → STANDARD

                else {

                    seatType = SeatType.STANDARD;
                }

                seats.add(
                        Seat.builder()
                                .seatName(row + String.valueOf(num))
                                .room(room)
                                .status(SeatStatus.AVAILABLE)
                                .seatType(seatType)
                                .build()
                );
            }
        }

        seatRepository.saveAll(seats);
    }
    private void createUserWithProfile(String username, String rawPassword, Role role,
                                       String fullName, String email, String phone) {
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .enabled(true)
                .build();

        UserProfile profile = UserProfile.builder()
                .user(user)
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .build();

        user.setProfile(profile);
        userRepository.save(user);
    }
}