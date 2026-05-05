package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    private Long id;
    @NotNull(message = "Booking start must be specified")
    @FutureOrPresent
    private LocalDateTime start;
    @NotNull(message = "Booking end must be specified")
    @Future
    private LocalDateTime end;
    @NotNull(message = "Booked item id must be specified")
    private Long itemId;
    private ItemDto item;
    private UserDto booker;
    private BookingStatus status;
}
