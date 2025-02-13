package com.korea.shop.service;

import com.korea.shop.batch.OrderStatusScheduler;
import com.korea.shop.domain.Delivery;
import com.korea.shop.domain.DeliveryStatus;
import com.korea.shop.domain.Order;
import com.korea.shop.domain.OrderStatus;
import com.korea.shop.repository.OrderRepositoryClass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
public class OrderStatusSchedulerTest {

    @Autowired private OrderRepositoryClass orderRepository;

    @Autowired private OrderStatusScheduler orderStatusScheduler;

    @Test
    void 배치테스트() {
        // Given: 테스트용 주문 생성 및 저장
        LocalDateTime now = LocalDateTime.now();

        // 주문 1 (배송 정보 포함)
        Delivery delivery1 = new Delivery();
        delivery1.setStatus(DeliveryStatus.READY); // 배송 준비 중

        Order order1 = new Order();
        order1.setStatus(OrderStatus.PENDING);
        order1.setOrderDate(now.minusDays(2));
        order1.setDelivery(delivery1); // 🚀 필수 설정
        orderRepository.save(order1);

        // 주문 2 (배송 정보 포함)
        Delivery delivery2 = new Delivery();
        delivery2.setStatus(DeliveryStatus.READY);

        Order order2 = new Order();
        order2.setStatus(OrderStatus.PENDING);
        order2.setOrderDate(now.minusDays(3));
        order2.setDelivery(delivery2);
        orderRepository.save(order2);

        // <여기 밑에는 모두 시험 문제>

        // When: 배치 스케줄러 실행
        orderStatusScheduler.cancelUnpaidOrders();

        // Then: 주문 상태가 CANCEL로 변경되었는지 확인
        List<Order> canceledOrders = orderRepository.findByStatusAndBeforeDate(OrderStatus.CANCEL, now.minusDays(1));

        // 🚀 검증: 2건의 주문이 취소되었는지 확인
        assertEquals(2, canceledOrders.size(), "취소된 주문 개수가 올바르지 않음");
        assertTrue(canceledOrders.stream().allMatch(o -> o.getStatus() == OrderStatus.CANCEL), "주문이 올바르게 취소되지 않음");
    }
}
