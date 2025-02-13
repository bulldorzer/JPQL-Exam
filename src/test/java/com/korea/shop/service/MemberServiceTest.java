package com.korea.shop.service;

import com.korea.shop.Service.MemberService;
import com.korea.shop.domain.Member;
import com.korea.shop.repository.MemberRepositoryClass;
import jakarta.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class) // JUnit5 버전으로 테스트
@SpringBootTest
@Transactional
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepositoryClass memberRepository;
    @Autowired EntityManager em;

    @Test
    public void 회원가입() throws Exception{
        // 1. given - 객체생성(값설정)
        Member member = new Member();
        member.setName("Lee");
        member.setPw("1111");
        member.setEmail("user100@aaa.com");

        // 2. when - ~을 때
        Long savedId = memberService.join(member); // db에 회원정보 저장

        // 3. then - 결과적으로..
        Member savedMember = memberRepository.findOne(savedId);
        System.out.println("저장된 id : " + savedId);
        System.out.println("저장된 객체 : " + savedMember);
        // 정상이면 테스트 통과하고 콘솔창에 출력x - 정상이 됨
        assertEquals(member, memberRepository.findOne(savedId));
        System.out.println("-------출력--------- ");
    }

    // 중복검사 테스트 - 이름
    @Test
    public void  중복회원검사() throws Exception{
        // 1. given - 값 생성
        Member mem1 = new Member();
        mem1.setName("Lee");

        Member mem2 = new Member();
        mem2.setName("Lee");

        // 2. when - 동작
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, ()->{
            memberService.join(mem1);
            memberService.join(mem2); // 예외가 발생할 예정 - IllegalArgumentException
        });

        // 3. then - 결과확인
        // 예외 메시지가 기대하는 메시지와 같냐?
        assertEquals("이미 존재하는 회원", exception.getMessage());
    }

}
