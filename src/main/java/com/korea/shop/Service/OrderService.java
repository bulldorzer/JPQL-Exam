package com.korea.shop.Service;

import com.korea.shop.domain.*;
import com.korea.shop.domain.item.Item;
import com.korea.shop.dto.OrderDTO;
import com.korea.shop.repository.ItemRepositoryClass;
import com.korea.shop.repository.MemberRepositoryClass;
import com.korea.shop.repository.OrderItemRepositoryClass;
import com.korea.shop.repository.OrderRepositoryClass;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {   // 시험 랜덤 2개

    private final OrderRepositoryClass orderRepository;
    private final OrderItemRepositoryClass orderItemRepository;
    private final ItemRepositoryClass itemRepository;
    private final MemberRepositoryClass memberRepository;
    private final ModelMapper modelMapper;

    // 주문 생성 (아이템 추가 X)
    public Long createOrder(Long memberId) {
        Member member = memberRepository.findOne(memberId);
        if (member == null) {
            throw new IllegalArgumentException("해당 회원 없음");
        }

        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());
        delivery.setStatus(DeliveryStatus.READY);

        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());

        orderRepository.save(order);
        return order.getId();
    }

    // 주문서에 아이템 추가
    public OrderItem addOrderItem(Long orderId, Long itemId, int qty) {
        Order order = orderRepository.findOne(orderId);
        if (order == null) throw new IllegalArgumentException("해당 주문 없음");

        Item item = itemRepository.findOne(itemId);
        if (item == null) throw new IllegalArgumentException("해당 상품 없음");

        if (qty <= 0) throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");

        OrderItem orderItem = OrderItem.createOrderItem(order, item, item.getPrice(), qty);
        return orderItemRepository.save(orderItem);
    }

    // 전체 주문 조회
    public List<OrderDTO>  getAllOrders() {
        // return orderRepository.findAllByJPQL();
        // 모델 매퍼 사용 : modelMapper.map( 객체, 클래스 )

        // 레포지토리 검색 -> stream 변환 -> 리턴
        // 반환 : modelMapper.map( 객체, 클래스)
        List<OrderDTO> dtoList = orderRepository.findAllByFetch()
                .stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());
        return dtoList;
    }

    // 특정 주문 아이템 삭제 (취소)
    public void cancelOrderItem(Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findOne(orderItemId);
        if (orderItem == null) throw new IllegalArgumentException("해당 주문 아이템 없음");

        orderItem.cancel();
        orderItemRepository.delete(orderItem);
    }

    // 전체 주문 취소
    public void cancelAllOrderItems(Long orderId) {
        Order order = orderRepository.findOne(orderId);
        if (order == null) throw new IllegalArgumentException("해당 주문 없음");

        List<OrderItem> orderItemList = orderItemRepository.findByOrderId(orderId);
        if (orderItemList.isEmpty()) throw new IllegalArgumentException("취소할 주문 아이템 없음");

        order.cancel();

        for (OrderItem orderItem : orderItemList) {
            orderItem.cancel();
            orderItemRepository.delete(orderItem);
        }
    }
}
