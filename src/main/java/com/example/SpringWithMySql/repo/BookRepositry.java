package com.example.SpringWithMySql.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SpringWithMySql.models.Book;

public interface BookRepositry extends JpaRepository<Book, Long> {

	public List<Book> findBybookName(String name);
	public Book findOneBybookName(String name);
}
