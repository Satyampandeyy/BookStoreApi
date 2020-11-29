package com.example.SpringWithMySql.batch;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.SpringWithMySql.models.Book;
import com.example.SpringWithMySql.repo.BookRepositry;
@Component
public class BookBatchBean {

	private Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());
	@Autowired
	BookRepositry bookrepo;
	
	
	//@Scheduled(
	//		cron="0,15 * * * * *")
	public void cronJob() {
		logger.info("< cronjob");
		
		//Add scheduled logic here
		Collection<Book> books=bookrepo.findAll();
		logger.info("There are {} books in the data store", books.size());
		
		logger.info("< cronjob");
	}
	

//	@Scheduled(
//			initialDelay=5000,
//			fixedRate=15000)
	public void fixedRateJobWithInitialDelay() {
		logger.info("< fixedRateJobWithInitialDelay");
		
		long pause=5000;
		long start=System.currentTimeMillis();
		do {
			if(start - pause < System.currentTimeMillis()) {
				break;
			}
		} while(true); 
		logger.info("processing time was {} seconds", pause/1000);
		
		logger.info("< fixedRateJobWithInitialDelay");
	}
	@Scheduled(
			initialDelay=5000,
			fixedDelay=15000)
	public void fixedDelayJobWithInitialDelay() {
		logger.info("< fixedRateJobWithInitialDelay");
		
		long pause=5000;
		long start=System.currentTimeMillis();
		do {
			if(start - pause < System.currentTimeMillis()) {
				break;
			}
		} while(true); 
		logger.info("processing time was {} seconds", pause/1000);
		
		logger.info("< fixedRateJobWithInitialDelay");
	}
}
