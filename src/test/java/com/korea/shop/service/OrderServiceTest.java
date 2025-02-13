package com.korea.shop.service;

import com.korea.shop.Service.OrderService;
import com.korea.shop.domain.*;
import com.korea.shop.domain.item.Book;
import com.korea.shop.exception.NotEnoughStockException;
import com.korea.shop.repository.OrderItemRepositoryClass;
import com.korea.shop.repository.OrderRepositoryClass;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class) // JUnit5 버전으로 테스트
@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired EntityManager em;
    @Autowired OrderService orderService;
    @Autowired OrderRepositoryClass orderRepository;
    @Autowired OrderItemRepositoryClass orderItemRepository;

    // 주문서 생성 = 상품 주문
    @Test
    public void 상품주문() throws Exception{
        // given - 주문 위한 데이터 준비

        Member member = createMember();
        Book item = createBook("JAVA Spring", 10000, 10);
        int orderQty = 3; // 주문수량

        // when - 주문 생성 및 상품추가
        Long orderId = orderService.createOrder(member.getId()); // 상품주문서 생성
        OrderItem orderItem = orderService.addOrderItem(orderId, item.getId(), orderQty); // 주문상품 추가 -  1개
        List<OrderItem> orderItemList = orderItemRepository.findByOrderId(orderId); // 주문 상품 리스트 조회
        Order getOrder = orderRepository.findOne(orderId); // 추가된 주문서 조회

        // then - 검증 단계

        int totalPrice = orderItemList.stream().mapToInt(OrderItem::getTotalPrice).sum(); // 총 주문금액 계산

        assertEquals(OrderStatus.ORDER, getOrder.getStatus(), "상품 주문시 상태는 ORDER");
        assertEquals(7, item.getStockQuantity(), "재고조회");
        assertEquals(1, orderItemList.size(), "주문상품 갯수");
        assertEquals(item.getPrice() * orderQty, totalPrice, "주문가격");

        System.out.println("주문상태 : " + getOrder.getStatus());
        System.out.println("재고조회 : " + item.getStockQuantity());
        System.out.println("주문상품갯수 : " + orderItemList.size());
        System.out.println("주문가격 : " + totalPrice);

    }
    
    // 재고수량이 초과, 예외발생해야 함
    @Test
    public void 상품주문_재고수량초과() throws Exception{
        // given
        Member member = createMember();
        Book item = createBook("JAVA Spring", 10000, 10);
        int orderQty = 15; // 주문수량

        // when
        Long orderId = orderService.createOrder(member.getId());

        NotEnoughStockException exception = assertThrows(NotEnoughStockException.class, ()->{
            OrderItem orderItem = orderService.addOrderItem(orderId, item.getId(), orderQty);
        });
        // then - 예외 발생하면 정상
        assertEquals("need more stock", exception.getMessage(), "재고 부족");
        System.out.println("재고 부족! " + exception.getMessage());
    }


    
    // 주문 취소
    @Test
    public void 주문취소() throws Exception{
        // given
        Member member = createMember();
        Book item = createBook("JAVA Spring", 10000, 10);
        int orderQty = 2; // 주문수량
        Long orderId = orderService.createOrder(member.getId());
        OrderItem orderItem = orderService.addOrderItem(orderId, item.getId(), orderQty);

        // when
        // 주문서 생성 및 추가
        orderService.cancelAllOrderItems(orderId);

        // then
        Order getOrder = orderRepository.findOne(orderId);

        // 1) 재고수량 - 증가? - 10개인지 체크
        // 2) 상태 체크 -  OrderState가 cancel상태 체크

        assertEquals(OrderStatus.CANCEL, getOrder.getStatus(), "주문 상태 확인");
        assertEquals(10, item.getStockQuantity(), "아이템 갯수 확인");

        System.out.println("주문상태 : " + getOrder.getStatus());
        System.out.println("아이템 갯수 : " + item.getStockQuantity());

    }

    // 부분 취소
    // 부분취소 했을때 변화?
    // - Order의 상품 갯수가 달라짐
    // - 재고가 복원
    @Test
    public void 부분취소() throws Exception{
        // given
        // 아이템 2개 이상 추가
        Member member = createMember();
        Book item1 = createBook("JAVA Spring", 10000, 10);
        Book item2 = createBook("파이썬", 12000, 10);
        int orderQty1 = 3;
        int orderQty2 = 2;

        // 주문서 생성
        Long orderId = orderService.createOrder(member.getId());
        // 주문 상품 추가 (2개)
        OrderItem orderItem1 = orderService.addOrderItem(orderId, item1.getId(),  orderQty1);
        OrderItem orderItem2 = orderService.addOrderItem(orderId, item2.getId(),  orderQty2);

        // when
        orderService.cancelOrderItem(orderItem1.getId());

        // then
        Order getOrder = orderRepository.findOne(orderId); // 주문서 다시 검색
        List<OrderItem> orderItemList = orderItemRepository.findByOrderId(orderId); // 아이템 리스트
        int totalPrice = orderItemList.stream().mapToInt(OrderItem::getTotalPrice).sum();

        // 주문 아이템 갯수
        assertEquals(1, orderItemList.size());
        // 삭제된 아이템의 재고가 복구 되었는가?
        assertEquals(10, item1.getStockQuantity()); // 3개 주문
        // 두번째 아이템 재고는 그대로
        assertEquals(8, item2.getStockQuantity()); // 2개 주문

        System.out.println("현재 주문서 아이템 갯수 : " + orderItemList.size());
        System.out.println("전체금액 : " + totalPrice);
        System.out.println("아이템1 재고 : " + item1.getStockQuantity());
        System.out.println("아이템2 재고 : " + item2.getStockQuantity());
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setEmail("user100@aaa.com");
        member.setPw("1111");
        em.persist(member); // 영속성 컨텍스트에 저장
        return member;
    }

    private Book createBook(String name, int price, int stockQty){
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQty);
        em.persist(book); // 영속성 컨텍스트에 저장
        return book;
    }

}
