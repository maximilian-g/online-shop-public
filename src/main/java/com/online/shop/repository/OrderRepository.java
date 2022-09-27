package com.online.shop.repository;

import com.online.shop.entity.Order;
import com.online.shop.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> getOrdersByUserId(Long id, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.endDate IS NULL ORDER BY o.id DESC")
    Page<Order> getAllNotCompletedOrderByIdDesc(Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.endDate IS NULL")
    long getAllNotCompletedCount();

    @Query("SELECT o FROM Order o WHERE o.endDate IS NULL AND o.status = :status ORDER BY o.id DESC")
    Page<Order> getAllNotCompletedWithStatusOrderByIdDesc(OrderStatus status, Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.endDate IS NULL AND o.status = :status")
    long getAllNotCompletedWithStatusCount(OrderStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> getOrderByIdForUpdate(Long id);

    @Query("SELECT o FROM Order o WHERE o.user.username = :username ORDER BY o.id DESC")
    Collection<Order> getOrdersByUsernameOrderByIdDesc(String username);

    @Query("SELECT o FROM Order o WHERE o.paid = FALSE AND o.startDate < :date AND o.status NOT IN :statuses ORDER BY o.id DESC")
    Collection<Order> getNotPaidOrdersOlderThanAndNotInStatuses(Date date, List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o " +
            "WHERE o.paid = TRUE " +
            "AND o.startDate >= :startDate " +
            "AND o.startDate <= :endDate " +
            "ORDER BY o.id DESC")
    Collection<Order> getPaidOrdersForPeriodOrderByIdDesc(Date startDate, Date endDate);

}
