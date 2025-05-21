package com.example.fin.dao;

import com.example.fin.domain.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionCategoryDao extends JpaRepository<TransactionCategory, Long> {

}
