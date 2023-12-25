package ru.example.springcache.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.example.springcache.model.Book;
import ru.example.springcache.repository.BookRepository;

import java.util.Optional;

@Service
public class BookService {
    private static final Logger LOG = LoggerFactory.getLogger(BookService.class);

    @Autowired
    private BookRepository repository;

    @Cacheable(value = "book", unless = "#result==null")
    //(value = "book" название кэша для группировки данных
    //unless = "#result==null" условие кэширования,в данном случае исключаем null
    public Book findBookById(long id) {
        LOG.info("Calling getBookById...");
        Optional<Book> bookOptional = repository.findById(id);
        return bookOptional.orElse(null);
    }

    @Cacheable(value = "book",key ="#title", unless = "#result==null")
    // key ="#title" явно указали какой параметр будет являться ключом
    public Book findBookByTitleAndAuthor(String title, String author){
        LOG.info("Calling findBookByTitleAndAuthor...");
        Optional<Book> bookOptional = repository.findBookByTitleAndAuthor(title, author);
        return bookOptional.orElse(null);
    }

    // принудительно помещает в кэш
    @CachePut(value = "book", key = "#book.id")
    public Book saveBook(Book book){
        LOG.info("Calling saveBook...");
        return repository.save(book);
    }

    public Book saveBookWithoutCachePut(Book book){
        LOG.info("Calling saveBook...");
        return repository.save(book);
    }

    // очистка кэша при удалении книги из БД
    @CacheEvict(value = "book", key = "#book.id")
    public void deleteBook(Book book){
        LOG.info("Calling deleteBook...");
        repository.delete(book);
    }

    // пример того, как делать нельзя, так как данные остаются в кэше
    public void deleteBookWithoutCacheEvict(Book book){
        LOG.info("Calling deleteBookWithoutCacheEvict...");
        repository.delete(book);
    }
}
