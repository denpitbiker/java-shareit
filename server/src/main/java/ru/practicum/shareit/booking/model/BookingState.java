package ru.practicum.shareit.booking.model;

import ru.practicum.shareit.error.BadRequestException;

public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    private static final String UNKNOWN_STATE_ERR_MSG = "Unknown state: ";

    public static BookingState from(String state) {
        if (state == null) {
            throw new BadRequestException(UNKNOWN_STATE_ERR_MSG + null);
        }
        try {
            return BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(UNKNOWN_STATE_ERR_MSG + state);
        }
    }
}
