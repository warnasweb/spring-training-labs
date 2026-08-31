package com.warnasweb.training.books.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "books")
public class Book {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, unique = true, length = 17)
	private String isbn;
	@Column(nullable = false, length = 200)
	private String title;
	@Column(nullable = false, length = 120)
	private String author;
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;
	@Version
	private long version;

	protected Book() {
	}

	public Book(String isbn, String title, String author, BigDecimal price) {
		this.isbn = isbn;
		this.title = title;
		this.author = author;
		this.price = price;
	}

	public void revise(String isbn, String title, String author, BigDecimal price) {
		this.isbn = isbn;
		this.title = title;
		this.author = author;
		this.price = price;
	}

	public Long getId() {
		return id;
	}

	public String getIsbn() {
		return isbn;
	}

	public String getTitle() {
		return title;
	}

	public String getAuthor() {
		return author;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public long getVersion() {
		return version;
	}
}
