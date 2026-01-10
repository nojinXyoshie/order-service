package com.example.order_service.service;

import com.example.order_service.domain.Notification;
import com.example.order_service.domain.NotificationStatus;
import com.example.order_service.domain.Order;
import com.example.order_service.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void notifyPaymentSuccess(Order order) {
        if (notificationRepository.existsByOrderIdAndStatus(order.getId(), NotificationStatus.SENT)) {
            log.info("Notification already sent for order {}", order.getId());
            return;
        }

        Notification notification = new Notification(order.getId(), "EMAIL",
                "Pembayaran untuk order %s berhasil".formatted(order.getId()), NotificationStatus.PENDING);
        notification.markSent();
        notificationRepository.save(notification);
        log.info("Notification {} created for order {} with status {}", notification.getId(), order.getId(), notification.getStatus());
    }
}
