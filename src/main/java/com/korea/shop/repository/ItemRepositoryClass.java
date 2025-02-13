package com.korea.shop.repository;

import com.korea.shop.domain.item.Item;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryClass {
    public final EntityManager em;

    public void save(Item item){
        if (item.getId() == null) {
            em.persist(item); // id 자동부여
        } else {
            em.merge(item);
        }
    }

    public Item findOne(Long id){
        return em.find(Item.class, id);
    }

    public List<Item> findAll(){
        String sql = "select i from Item i";
        return em.createQuery(sql, Item.class).getResultList();
    }
}
