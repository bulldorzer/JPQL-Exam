package com.korea.shop.repository;

import com.korea.shop.domain.Member;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryClass {
    // 1) 엔티티 매니저
    private final EntityManager em;
    
    // 1) 신규 생성(저장), 업데이트(수정)
    @Transactional
    public void save(Member member){
        em.persist(member);
    }
    
    // 2) 1개 데이터 찾기 - id기준
    public Member findOne(Long id){
        // em.find( 엔티티클래스, pk)
        return em.find(Member.class, id);
    }
    
    // 3) 여러개 데이터 찾기
    public List<Member> findAll(){
        // em.createQuery("jpql", 엔티티클래스)
        
        return em.createQuery("select m from Member m", Member.class)
                .getResultList(); // 리스트 형 변환 메서드 제공함
    }
    
    // 4) 이름으로 찾기
    // - 기본 JpaRepository에서 제공하지 않는 경우 JPQL이용하여 만듦.
    public List<Member> findByName(String name){
        // pk 검색 외에는 쿼리문 생성해서 실행해야 함. 
        return em.createQuery("select m from Member m where name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }

    // 5) id기준으로 삭제하기
    // 주의 : em.remove()는 영속성 컨텍스트에 존재하는 엔티티만 삭제 가능
    // ㄴ find()를 통해 조회를 해서 영속선 컨텍스트에 가져온 후 --> 삭제 실행
    // createQuery 메서드로 jpql 이용하여 삭제할 경우는 조회할 필요가 없음
    public void deleteById(Long id){
        // em.remove(삭제할 객체);
        Member member = em.find(Member.class, id); // 1) 조회
        if( member != null)
            em.remove(member); // 2) 존재하면 삭제
    }
    
    
}


// jpql 기본 규칙 3가지 
/* 
    1) 테이블 대신 엔티티명
    2) 별칭 반드시 사용
    3) 모든 필드 가져오기 별칭으로 기재
    4) 매개변수값을 조건값으로 지정할때 <:변수명> 형식으로 기재
 */
