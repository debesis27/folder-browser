package com.example.test1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.test1.entity.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>{
  
}
