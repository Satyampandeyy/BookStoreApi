package com.example.SpringWithMySql.service;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.SpringWithMySql.models.Book;
import com.example.SpringWithMySql.util.AsyncResponse;
@Service
public class EmailServiceBean implements EmailService {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public Boolean send(Book book) {
		logger.info("> send");
		
		Boolean success = Boolean.FALSE;
		
		// Simulate method execution time
		long pause =5000;
		try {
			Thread.sleep(pause);
		} catch(Exception e) {
			// do nothing
		}
		logger.info("processing time was {} seconds",pause/1000);
		success=Boolean.TRUE;
		
		return success;
	}

	@Async
	@Override
	public void sendAsync(Book book) {
		logger.info("> sendAsync");
		try {
			send(book);
		} catch(Exception e) {
			logger.warn("Exception caught sending asynchronous mail", e);
		}
		logger.info("sendAsync");

	}

	@Async
	@Override
	public Future<Boolean> sendAsyncWithResult(Book book) {
		logger.info("> sendAsyncWithResult");
		
		AsyncResponse<Boolean> response = new AsyncResponse<Boolean>();
		
		try {
			Boolean success = send(book);
			response.complete(success);
			
		} catch (Exception e) {
			logger.warn("Exception caught sending asynchronous mail", e);
			response.completedExceptionally(e);
		}
		logger.info("< sendAsyncWithResult");
		return response;
	}

}
