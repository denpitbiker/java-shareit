package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

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
}
