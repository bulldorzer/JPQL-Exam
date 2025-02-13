package com.korea.shop.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.korea.shop.domain.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {

    private Long orderId;
    private String name; // member대신 주문자 이름

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime orderDate; //주문시간

    private OrderStatus status; // [ORDER, CANCEL]
    private Address address; // 주문서에 - 배송지 주소

    // 엔티티 전체 조회 -> DTO변환
    // 장점 : 재상용성 좋음
    // 단점 : 불필요한 데이터 조회가 될 수 있음
    public OrderDTO(Order order){
        this.orderId = order.getId();
        this.name = order.getMember().getName();
        this.orderDate = order.getOrderDate();
        this.status = order.getStatus();
        this.address = order.getDelivery().getAddress();
    }

}
