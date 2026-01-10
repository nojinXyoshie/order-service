package com.example.order_service.repository;

import com.example.order_service.domain.Notification;
import com.example.order_service.domain.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, String> {

    boolean existsByOrderIdAndStatus(String orderId, NotificationStatus status);
}
