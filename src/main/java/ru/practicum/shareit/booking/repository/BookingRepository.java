package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBooker_Id(Long bookerId, Sort sort);

    List<Booking> findByBooker_IdAndStartBeforeAndEndAfter(Long bookerId,
                                                           LocalDateTime start,
                                                           LocalDateTime end,
                                                           Sort sort);

    List<Booking> findByBooker_IdAndEndBefore(Long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findByBooker_IdAndStartAfter(Long bookerId, LocalDateTime start, Sort sort);

    List<Booking> findByBooker_IdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    List<Booking> findByItem_Owner_Id(Long ownerId, Sort sort);

    List<Booking> findByItem_Owner_IdAndStartBeforeAndEndAfter(Long ownerId,
                                                               LocalDateTime start,
                                                               LocalDateTime end,
                                                               Sort sort);

    List<Booking> findByItem_Owner_IdAndEndBefore(Long ownerId, LocalDateTime end, Sort sort);

    List<Booking> findByItem_Owner_IdAndStartAfter(Long ownerId, LocalDateTime start, Sort sort);

    List<Booking> findByItem_Owner_IdAndStatus(Long ownerId, BookingStatus status, Sort sort);

    Optional<Booking> findFirstByItem_IdAndStatusAndEndBeforeOrderByEndDesc(Long itemId,
                                                                            BookingStatus status,
                                                                            LocalDateTime end);

    Optional<Booking> findFirstByItem_IdAndStatusAndStartAfterOrderByStartAsc(Long itemId,
                                                                              BookingStatus status,
                                                                              LocalDateTime start);

    boolean existsByItem_IdAndBooker_IdAndStatusAndEndBefore(Long itemId,
                                                             Long bookerId,
                                                             BookingStatus status,
                                                             LocalDateTime end);
}
