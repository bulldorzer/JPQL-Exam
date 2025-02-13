package com.korea.shop.repository;

import com.korea.shop.domain.OrderStatus;
import com.korea.shop.dto.OrderDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import com.korea.shop.domain.Order;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryClass {

    private final EntityManager em;


    public void save(Order order) {
        if (order.getId() == null){
            em.persist(order); // id 자동부여
        } else {
            em.merge(order);
        }
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    // 전체 데이터 조회 시험
    // 방법1 - fatch join
    public List<Order> findAllByFetch(){
        return em.createQuery(
                        "select o from Order o"
                                + " join fetch o.member m" // 회원 member까지 함께 조회
                                + " join fetch o.delivery d" // 배송 delivery까지 함께 조회
                        , Order.class)
                .getResultList();
    }

    // 방법2 - dto 프로젝션 시험
    public List<OrderDTO> findAllByJPQL(){
        return em.createQuery("select new com.korea.shop.dto.OrderDTO"
                + "(o.id, m.name, o.orderDate, o.status, d.address)"
                + " from Order o"
                + " join o.member m"
                + " join o.delivery d"
                , OrderDTO.class)
                .getResultList();
    }

    // 동적쿼리 1 - str 방식 시험
    public List<Order> findAllByString(OrderSearch orderSearch) {

        StringBuilder jpql = new StringBuilder("select o from Order o join o.member m "); // 가변형 텍스트
        List<String> conditions = new ArrayList<>(); // 조건 str이 저장될 리스트

        // statue 검색조건
        if(orderSearch.getOrderStatus() != null){
            conditions.add("o.status = :status");
        }

        // 회원 이름 검색
        if(StringUtils.hasText(orderSearch.getMemberName())){
            conditions.add("m.name like :name");
        }

        if( !conditions.isEmpty()){ // 조건이 있다면
            jpql.append(" where ").append( String.join(" and ", conditions) );
        }

        // 동적 쿼리 실행
        TypedQuery<Order> query = em.createQuery( jpql.toString() , Order.class)
                .setMaxResults(1000); // 최대 1000개까지만 데이터 반환하도록 제한

        // 동적 파라미터 설정
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", "%" + orderSearch.getMemberName() + "%");
        }
        return query.getResultList();
    }

    // 동적쿼리 2 - Criteria 방식 시험
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {


        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Object, Object> m = o.join("member", JoinType.INNER); // Order, Member 이너 조인

        List<Predicate> criteria = new ArrayList<>(); // 검색 조건을 저장할 리스트

        // 주문 상태 검색
        // cb.equal(테이블.get(필드명), 값)
        // cb.like(테이블.get(필드명), "%" + 값 + "%")
        if (orderSearch.getOrderStatus() != null) {
            // where문 조건 설정
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }

        // 회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name = cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }

        // 조건을 where절에 적용
        cq.where(cb.and(criteria.toArray( new Predicate[criteria.size()] )));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
        return query.getResultList();
    }
    
    // 동적쿼리 - Criteria 방식 -  batch에서 취소할 데이터 조회 나중에 사용할 데이터
    public List<Order> findByStatusAndBeforeDate(OrderStatus status, LocalDateTime beforeDate) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> order = cq.from(Order.class);

        List<Predicate> predicates = new ArrayList<>();
        if (status != null) {
            predicates.add(cb.equal(order.get("status"), status));
        }
        if (beforeDate != null) {
            predicates.add(cb.lessThanOrEqualTo(order.get("orderDate"), beforeDate));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        TypedQuery<Order> query = em.createQuery(cq);
        return query.getResultList();
    }

}

//  Criteria API란?
// - Criteria API는 JPA(Java Persistence API)에서 동적 쿼리를 생성할 수 있도록 지원하는 API임.
// - SQL을 직접 작성하지 않고 객체 지향 방식으로 동적으로 쿼리를 생성할 수 있음.

// 1) 동적 쿼리를 생성하는 도구 - where, join, select 조건 만들 수 있음
// 2) 결과물이 Order 클래스 형식인 쿼리를 만들거야 =  SELECT * FROM orders 와 비슷한 역할
// 3) Root<T> 란? - CriteriaQuery에서 기준이 되는 테이블을 지정
// 4) 조인할 테이블 지정
// 결과 : SELECT * FROM orders o INNER JOIN members m ON o.member_id = m.id


 /*
        N+1문제 :
            데이터를 조회할 때, 관계설정이 되어 있는 테이블들의 데이터를
            같이 조회하려면  N+1 개의 쿼리문의 실행이된다.

        해결방법 : fetch Join
            한번의 SQL로 모든 연관 엔티티 조회하도록 함 jpql 구문
            fetch Join을 사용하게 되면 Lazy 전략이어도 즉시 함께 조회된다.
            @EntityGraph 방법보다 JPQL로 세밀하게 제어 가능

            SELECT o.*, m.*, d.*  FROM orders o
            JOIN member m ON o.member_id = m.id
            JOIN delivery d ON o.delivery_id = d.id;
         */