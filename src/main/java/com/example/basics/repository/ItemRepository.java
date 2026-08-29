package com.example.basics.repository;

import com.example.basics.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    boolean existsBySku(String sku);
    Page<Item> findByCategoryId(Long categoryId, Pageable pageable);
}
