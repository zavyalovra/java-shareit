package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"author"})
    List<Comment> findByItemIdOrderByCreatedAsc(Long id);

    @EntityGraph(attributePaths = {"author"})
    List<Comment> findByItemIdInOrderByCreatedAsc(List<Long> itemIds);
}
