package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {"booker", "item"})
    List<Booking> findByBookerIdOrderByStartDesc(Long userId);

    @EntityGraph(attributePaths = {"booker", "item"})
    @Query(" select b from Booking b " +
            "where b.booker.id = ?1 " +
            "and b.status = BookingStatus.APPROVED " +
            "and b.start <= ?2 and b.end > ?2 " +
            "order by b.start desc")
    List<Booking> findCurrentByBookerId(Long userId, LocalDateTime now);

    @EntityGraph(attributePaths = {"booker", "item"})
    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long userId, LocalDateTime now);

    @EntityGraph(attributePaths = {"booker", "item"})
    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime now);

    @EntityGraph(attributePaths = {"booker", "item"})
    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long userId, BookingStatus status);

    @EntityGraph(attributePaths = {"booker", "item"})
    List<Booking> findByItemOwnerIdOrderByStartDesc(Long userId);

    @EntityGraph(attributePaths = {"booker", "item"})
    @Query(" select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and b.status = BookingStatus.APPROVED " +
            "and b.start <= ?2 and b.end > ?2 " +
            "order by b.start desc")
    List<Booking> findCurrentOwnersBookings(Long userId, LocalDateTime now);

    @EntityGraph(attributePaths = {"booker", "item"})
    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(Long userId, LocalDateTime now);

    @EntityGraph(attributePaths = {"booker", "item"})
    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime now);

    @EntityGraph(attributePaths = {"booker", "item"})
    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long userId, BookingStatus status);

    @EntityGraph(attributePaths = {"item"})
    @Query(" select b from Booking b " +
            "where b.item.id in ?1 " +
            "and b.status = BookingStatus.APPROVED " +
            "and b.start < ?2 " +
            "order by b.start desc")
    List<Booking> findLastBooking(List<Long> itemIds, LocalDateTime now);

    @EntityGraph(attributePaths = {"item"})
    @Query(" select b from Booking b " +
            "where b.item.id in ?1 " +
            "and b.status = BookingStatus.APPROVED " +
            "and b.start > ?2 " +
            "order by b.start asc")
    List<Booking> findNextBooking(List<Long> itemIds, LocalDateTime now);

    boolean existsByItemIdAndBookerIdAndStatusAndEndBefore(Long itemId, Long userId, BookingStatus status, LocalDateTime now);
}
