package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

public class CommentMapper {
    private CommentMapper() {
    }

    public static CommentDto toCommentDto(Comment comment) {
        if (comment == null) {
            return null;
        }
        String authorName = comment.getAuthor() != null ? comment.getAuthor().getName() : null;
        return new CommentDto(comment.getId(), comment.getText(), authorName, comment.getCreated());
    }

    public static Comment toComment(CommentDto dto, Item item, User author, LocalDateTime created) {
        return Comment.builder()
                .text(dto.getText())
                .item(item)
                .author(author)
                .created(created)
                .build();
    }
}
