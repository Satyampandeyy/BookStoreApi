package com.example.SpringWithMySql.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity.BodyBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.SpringWithMySql.exception.NoSuchTeacherFound;
import com.example.SpringWithMySql.models.Book;
import com.example.SpringWithMySql.repo.BookRepositry;
import com.example.SpringWithMySql.service.EmailService;

@Service
@Transactional(
		propagation=Propagation.SUPPORTS,
		readOnly=true)
@CrossOrigin("*")
@RestController
public class BookController {

	@Autowired
	BookRepositry bookRepositry;
	
	@Autowired
	private EmailService emailService;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@GetMapping("/getAllBooks")
	public List<Book> getBook() {
		List<Book> books =(List<Book>) bookRepositry.findAll();
		List<Book> bookUpdated=new ArrayList<>();
		for(Book book : books) {
			book.setPicByte(decompressBytes(book.getPicByte()));
			
		}
		return books;
	}
	
	@PostMapping("/sendBook/{id}/send")
	public ResponseEntity<Book> sendGreeting(@PathVariable("id") Long id, 
			@RequestParam(value="wait",
			defaultValue = "false") boolean waitForAsyncResult) {
		logger.info("< sendBook");
		Book book = null;
		try {
			book = bookRepositry.findById(id).orElseThrow(()->new NoSuchTeacherFound("No such Teacher present"));
			if(book == null) {
				logger.info("< sendBook");
				return new ResponseEntity<Book>(HttpStatus.NOT_FOUND);
			}
			if(waitForAsyncResult) {
				Future<Boolean> asyncResponse = emailService.sendAsyncWithResult(book);
				boolean emailSent = asyncResponse.get();
				logger.info("-book email sent? {}", emailSent);
			} else {
				emailService.sendAsync(book);
			}
		} catch(Exception e) {
			logger.error("A problem sending the book",e);
			return new ResponseEntity<Book>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		logger.info("< sendGreeting");
		return new ResponseEntity<Book>(book, HttpStatus.OK);
	}
	
	@Transactional(
			propagation=Propagation.SUPPORTS,
			readOnly=false)
	@CachePut(value="book",key="#book.bookId") 
	@CrossOrigin("*")
	@PostMapping("/bookSave")
	public ResponseEntity<String> insertBook(@RequestBody Book book) {
		bookRepositry.save(book);
		if(book.getBookId() == 19) {
			bookRepositry.deleteById(book.getBookId());
			throw new RuntimeException("Roll me back");
		}
		return new ResponseEntity("Book added successfully",HttpStatus.OK);
	}
	
	@PostMapping("/upload/{bookName}/{bookAuthor}")
	public ResponseEntity<String> uplaodImage(@RequestParam("imageFile") MultipartFile file,@PathVariable("bookName") String name,
			@PathVariable("bookAuthor") String author) throws IOException {

		System.out.println("Original Image Byte Size - " + file.getBytes().length);
		Book img = new Book();
		img.setPicByte(compressBytes(file.getBytes()));
		img.setBookName(name);
		img.setBookAuthor(author);
		img.setImageName(file.getOriginalFilename());
		
		bookRepositry.save(img);
		return new ResponseEntity("Image uploaded successfully",HttpStatus.OK);
	}
	
	// compress the image bytes before storing it in the database
		public static byte[] compressBytes(byte[] data) {
			Deflater deflater = new Deflater();
			deflater.setInput(data);
			deflater.finish();

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
			byte[] buffer = new byte[1024];
			while (!deflater.finished()) {
				int count = deflater.deflate(buffer);
				outputStream.write(buffer, 0, count);
			}
			try {
				outputStream.close();
			} catch (IOException e) {
			}
			System.out.println("Compressed Image Byte Size - " + outputStream.toByteArray().length);

			return outputStream.toByteArray();
		}
		
		@GetMapping(path = { "/get/{imageName}" })
		public Book getImage(@PathVariable("imageName") String imageName) throws IOException {

			final Optional<Book> retrievedImage = Optional.of(bookRepositry.findOneBybookName(imageName));
			Book img = new Book();
			img.setPicByte(decompressBytes(retrievedImage.get().getPicByte()));
			img.setBookName(retrievedImage.get().getBookName());
			img.setBookId(retrievedImage.get().getBookId());
			img.setImageName(retrievedImage.get().getImageName());
			
			return img;
		}
		public static byte[] decompressBytes(byte[] data) {
			Inflater inflater = new Inflater();
			inflater.setInput(data);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
			byte[] buffer = new byte[1024];
			try {
				while (!inflater.finished()) {
					int count = inflater.inflate(buffer);
					outputStream.write(buffer, 0, count);
				}
				outputStream.close();
			} catch (IOException ioe) {
			} catch (DataFormatException e) {
			}
			return outputStream.toByteArray();
		}
	
	@Transactional(
			propagation=Propagation.SUPPORTS,
			readOnly=false)
	@PostMapping("/multipleBookSave")
	public String insertBook(@RequestBody List<Book> book) {
		bookRepositry.saveAll(book);
		return "Record is saved successfully";
	}
	

	  @GetMapping("/findBybookNames/{name}") 
	  public List<Book> getBookByName(@PathVariable("name") String bookName) {
		  return bookRepositry.findBybookName(bookName); 
		  }
	 
	@Cacheable(value="book",
			key="#id") 
	@GetMapping("/getByBookId/{bookId}")
	public Optional<Book> getBookById(@PathVariable("bookId") Long id) throws InterruptedException {
		Thread.sleep(4000);
		return bookRepositry.findById(id);
	}
	
	@Transactional(
			propagation=Propagation.SUPPORTS,
			readOnly=false)
	@CacheEvict(value="book",key="#Id")
	@DeleteMapping("/deleteBookById/{book}")
	public String deleteBook(@PathVariable("book") Long Id) {
		bookRepositry.deleteById(Id);
		return "Record deleted successfully";
	}
	@PutMapping("/updateBookName/{bookId}")
	@CachePut(value="book",key="#id") 
	public String updateBookNameById(@PathVariable("bookId") Long id,@RequestBody Book updatedbook) {
		Book book = bookRepositry.findById(id).orElse(new Book());
		book.setBookName(updatedbook.getBookName());
		bookRepositry.save(book);
		return "book updated successfully";
	}
	
	@PutMapping("/updateBookNameAuthor")
	public String updateBookNameAndAuthor(@RequestBody Book bookInfo) {
		Book book = bookRepositry.findById(bookInfo.getBookId()).orElse(new Book());
		book.setBookAuthor(bookInfo.getBookAuthor());
		book.setBookName(bookInfo.getBookName());
		bookRepositry.save(book);
		return "book updated successfully";
	}
	@CacheEvict(value="book",allEntries=true)
	@GetMapping("/evict")
	public void evictCache() {
		
	}
}
