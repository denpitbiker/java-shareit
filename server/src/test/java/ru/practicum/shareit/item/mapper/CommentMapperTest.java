package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static ru.practicum.shareit.TestStubs.VALID_COMMENT_TEXT;
import static ru.practicum.shareit.TestStubs.VALID_ITEM_DESCRIPTION_1;
import static ru.practicum.shareit.TestStubs.VALID_ITEM_NAME_1;
import static ru.practicum.shareit.TestStubs.VALID_USER_NAME_1;

public class CommentMapperTest {
    @Test
    @DisplayName("Map null comment to dto")
    public void toCommentDto_nullComment_returnsNull() {
        Assertions.assertNull(CommentMapper.toCommentDto(null), "Expected null dto");
    }

    @Test
    @DisplayName("Map comment without author to dto")
    public void toCommentDto_commentWithoutAuthor_authorNameIsNull() {
        Comment comment = Comment.builder()
                .id(1L)
                .text(VALID_COMMENT_TEXT)
                .item(new Item(1L, VALID_ITEM_NAME_1, VALID_ITEM_DESCRIPTION_1, true, null, null))
                .author(null)
                .created(LocalDateTime.now())
                .build();

        CommentDto commentDto = CommentMapper.toCommentDto(comment);

        Assertions.assertNull(commentDto.getAuthorName(), "Expected null author name");
        Assertions.assertEquals(VALID_COMMENT_TEXT, commentDto.getText(), "Expected comment text");
    }

    @Test
    @DisplayName("Map dto to comment")
    public void toComment_dto_fieldsPreserved() {
        CommentDto dto = new CommentDto(null, VALID_COMMENT_TEXT, null, null);
        Item item = new Item(1L, VALID_ITEM_NAME_1, VALID_ITEM_DESCRIPTION_1, true, null, null);
        User author = new User(1L, VALID_USER_NAME_1, "denis@example.com");
        LocalDateTime created = LocalDateTime.now();

        Comment comment = CommentMapper.toComment(dto, item, author, created);

        Assertions.assertEquals(VALID_COMMENT_TEXT, comment.getText(), "Expected text");
        Assertions.assertEquals(author, comment.getAuthor(), "Expected author");
        Assertions.assertEquals(created, comment.getCreated(), "Expected created timestamp");
    }
}
