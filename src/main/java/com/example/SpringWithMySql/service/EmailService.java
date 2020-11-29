package com.example.SpringWithMySql.service;

import java.util.concurrent.Future;

import com.example.SpringWithMySql.models.Book;

public interface EmailService {

	Boolean send(Book book);
	
	void sendAsync(Book book);
	
	Future<Boolean> sendAsyncWithResult(Book book);
}
