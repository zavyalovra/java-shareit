package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    boolean existsByRequestorIdAndDescription(Long requestorId, String description);

    @EntityGraph(attributePaths = {"items", "items.owner"})
    List<ItemRequest> findByRequestor_IdOrderByCreatedDesc(Long userId);

    @EntityGraph(attributePaths = {"items", "items.owner"})
    List<ItemRequest> findByRequestor_IdNotOrderByCreatedDesc(Long userId);
}
