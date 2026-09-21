package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query(" select b from Booking b " +
            "where b.booker.id = ?1 " +
            "order by b.start desc")
    List<Booking> findAllByBookerId(Long userId);

    @Query(" select b from Booking b " +
            "where b.booker.id = ?1 " +
            "and b.status = BookingStatus.APPROVED " +
            "and b.start <= ?2 and b.end > ?2 " +
            "order by b.start desc")
    List<Booking> findCurrentByBookerId(Long userId, LocalDateTime now);

    @Query(" select b from Booking b " +
            "where b.booker.id = ?1 " +
            "and b.status = BookingStatus.APPROVED " +
            "and b.end < ?2 " +
            "order by b.start desc")
    List<Booking> findPastByBookerId(Long userId, LocalDateTime now);

    @Query(" select b from Booking b " +
            "where b.booker.id = ?1 " +
            "and b.status = BookingStatus.APPROVED " +
            "and b.start > ?2 " +
            "order by b.start desc")
    List<Booking> findFutureByBookerId(Long userId, LocalDateTime now);

    @Query(" select b from Booking b " +
            "where b.booker.id = ?1 " +
            "and b.status = BookingStatus.WAITING " +
            "order by b.start desc")
    List<Booking> findWaitingByBookerId(Long userId);

    @Query(" select b from Booking b " +
            "where b.booker.id = ?1 " +
            "and b.status = BookingStatus.REJECTED " +
            "order by b.start desc")
    List<Booking> findRejectedByBookerId(Long userId);

    @Query(" select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "order by b.start desc")
    List<Booking> findAllOwnersBookings(Long userId);

    @Query(" select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and b.status = BookingStatus.APPROVED " +
            "and b.start <= ?2 and b.end > ?2 " +
            "order by b.start desc")
    List<Booking> findCurrentOwnersBookings(Long userId, LocalDateTime now);

    @Query(" select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and b.status = BookingStatus.APPROVED " +
            "and b.end < ?2 " +
            "order by b.start desc")
    List<Booking> findPastOwnersBookings(Long userId, LocalDateTime now);

    @Query(" select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and b.status = BookingStatus.APPROVED " +
            "and b.start > ?2 " +
            "order by b.start desc")
    List<Booking> findFutureOwnersBookings(Long userId, LocalDateTime now);

    @Query(" select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and b.status = BookingStatus.WAITING " +
            "order by b.start desc")
    List<Booking> findWaitingOwnersBookings(Long userId);

    @Query(" select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and b.status = BookingStatus.REJECTED " +
            "order by b.start desc")
    List<Booking> findRejectedOwnersBookings(Long userId);

    @Query(" select b from Booking b " +
            "where b.item.id in ?1 " +
            "and b.status = BookingStatus.APPROVED " +
            "and b.start < ?2 " +
            "order by b.start desc")
    List<Booking> findLastBooking(List<Long> itemIds, LocalDateTime now);

    @Query(" select b from Booking b " +
            "where b.item.id in ?1 " +
            "and b.status = BookingStatus.APPROVED " +
            "and b.start > ?2 " +
            "order by b.start asc")
    List<Booking> findNextBooking(List<Long> itemIds, LocalDateTime now);

    @Query(" select b from Booking b " +
            "where b.item.id = ?1 " +
            "and b.booker.id = ?2 " +
            "and b.status = BookingStatus.APPROVED " +
            "and b.end < ?3 " +
            "order by b.end desc")
    List<Booking> findCompletedBookings(Long itemId, Long bookerId, LocalDateTime now);
}
