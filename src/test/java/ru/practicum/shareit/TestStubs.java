package ru.practicum.shareit;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

public final class TestStubs {
    public static final long NON_EXISTING_ID = 9999L;
    public static final String USER_ID_HEADER = "X-Sharer-User-Id";

    public static final String VALID_USER_NAME_1 = "Denis";
    public static final String VALID_USER_NAME_2 = "Lera";
    public static final String VALID_USER_NAME_3 = "Ilnur";
    public static final String UPDATED_USER_NAME = "Updated Denis";

    public static final String VALID_EMAIL_1 = "denis@example.com";
    public static final String VALID_EMAIL_2 = "lera@example.com";
    public static final String VALID_EMAIL_3 = "ilnur@example.com";
    public static final String UPDATED_EMAIL = "updated@example.com";
    public static final String INVALID_EMAIL = "invalid-email";

    public static final String VALID_ITEM_NAME_1 = "Drill";
    public static final String VALID_ITEM_NAME_2 = "Saw";
    public static final String VALID_ITEM_DESCRIPTION_1 = "Powerful drill";
    public static final String VALID_ITEM_DESCRIPTION_2 = "Sharp saw";
    public static final String UPDATED_ITEM_NAME = "Updated drill";
    public static final String UPDATED_ITEM_DESCRIPTION = "Updated description";
    public static final String SEARCH_TEXT_MATCH = "drill";
    public static final String SEARCH_TEXT_BLANK = "   ";
    public static final String VALID_COMMENT_TEXT = "Works well";
    public static final String UPDATED_COMMENT_TEXT = "Still works well";

    public static final UserDto VALID_USER_DTO_1 = new UserDto(null, VALID_USER_NAME_1, VALID_EMAIL_1);
    public static final UserDto VALID_USER_DTO_2 = new UserDto(null, VALID_USER_NAME_2, VALID_EMAIL_2);
    public static final UserDto VALID_USER_DTO_3 = new UserDto(null, VALID_USER_NAME_3, VALID_EMAIL_3);
    public static final UserDto INVALID_USER_DTO_NULL_NAME = new UserDto(null, null, VALID_EMAIL_1);
    public static final UserDto INVALID_USER_DTO_BLANK_NAME = new UserDto(null, "   ", VALID_EMAIL_1);
    public static final UserDto INVALID_USER_DTO_NULL_EMAIL = new UserDto(null, VALID_USER_NAME_1, null);
    public static final UserDto INVALID_USER_DTO_INVALID_EMAIL = new UserDto(null, VALID_USER_NAME_1, INVALID_EMAIL);

    public static final ItemDto VALID_ITEM_DTO_1 = new ItemDto(null, VALID_ITEM_NAME_1, VALID_ITEM_DESCRIPTION_1, true, null);
    public static final ItemDto VALID_ITEM_DTO_2 = new ItemDto(null, VALID_ITEM_NAME_2, VALID_ITEM_DESCRIPTION_2, false, null);
    public static final ItemDto INVALID_ITEM_DTO_NULL_NAME = new ItemDto(null, null, VALID_ITEM_DESCRIPTION_1, true, null);
    public static final ItemDto INVALID_ITEM_DTO_BLANK_NAME = new ItemDto(null, "   ", VALID_ITEM_DESCRIPTION_1, true, null);
    public static final ItemDto INVALID_ITEM_DTO_NULL_DESCRIPTION = new ItemDto(null, VALID_ITEM_NAME_1, null, true, null);
    public static final ItemDto INVALID_ITEM_DTO_BLANK_DESCRIPTION = new ItemDto(null, VALID_ITEM_NAME_1, "   ", true, null);
    public static final ItemDto INVALID_ITEM_DTO_NULL_AVAILABLE = new ItemDto(null, VALID_ITEM_NAME_1, VALID_ITEM_DESCRIPTION_1, null, null);
}
