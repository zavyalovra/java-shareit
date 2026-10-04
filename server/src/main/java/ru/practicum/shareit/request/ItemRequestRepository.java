package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    @EntityGraph(attributePaths = {"items", "items.owner"})
    List<ItemRequest> findByRequestor_IdOrderByCreatedDesc(Long userId);

    List<ItemRequest> findByRequestor_IdNot(Long userId, Pageable pageable);
}
