package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;
    @NotBlank(message = "Item name must not be blank")
    private String name;
    @NotBlank(message = "Item description must not be blank")
    private String description;
    @NotNull(message = "Item availability must be specified")
    private Boolean available;
    private Long requestId;
    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;
    private List<CommentDto> comments = new ArrayList<>();

    public ItemDto(Long id, String name, String description, Boolean available, Long requestId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
        this.requestId = requestId;
        this.comments = new ArrayList<>();
    }
}
