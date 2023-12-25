package ru.example.springcache.service;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import ru.example.springcache.model.Book;
import ru.example.springcache.repository.BookRepository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@SpringBootTest
// перезагрузка контекста чтобы новый тест был с чистым кэшом
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BookServiceTest {
    private static final Logger LOG =  LoggerFactory.getLogger(BookServiceTest.class);

    //позволит понять как хранятся кэшированные данные
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private BookService service;

    @SpyBean// шпионим за бином
    @Autowired
    private BookRepository repository;

    @Test
    void testFindBookById(){
        final long bookId = 1L;
        //первое обращение к сервису, получение данных из БД и кэширование
        Book book =service.findBookById(bookId);
        assertNotNull(book, "Book is not found");
        LOG.info("Book: " + book.getTitle());

        //второе обращение к сервису, получение данных из кэша
        Book cachedBook = service.findBookById(bookId);
        assertNotNull(cachedBook, "Book is not found");
        LOG.info("Book: " + cachedBook.getTitle());

        // данные из БД извлекаются только 1 раз при первом обращении
        verify(repository, times(1)).findById(bookId);

        //debug -> Evaluate Expression...-> Evaluate
        applicationContext.getBean("cacheManager");
    }

    @Test
    void testFindBookByIdNullResult() {
        final long bookId = 10L;

        // первое обращение к сервису, в ответ возвращается null
        Book book1 = service.findBookById(bookId);
        assertNull(book1, "Book is found");

        // второе обращение к сервису, в ответ возвращается null
        Book book2 = service.findBookById(bookId);
        assertNull(book2, "Book is found");

        // так как значение null не кэшируется, то к БД будет 2 обращения
        verify(repository, times(2)).findById(bookId);
    }

    @Test
    void testFindBookByTitleAndAuthor() {
        // первое обращение к сервису, получение данных из БД и кэширование
        Book book = service.findBookByTitleAndAuthor("Тихий дон", "М. А. Шолохов");
        assertNotNull(book, "Book is not found");
        LOG.info("Book: " + book.getTitle());

        // второе обращение к сервису, получение данных из кэша
        Book cachedBook = service.findBookByTitleAndAuthor("Тихий дон", "М. А. Шолохов");
        assertNotNull(cachedBook, "Book is not found");
        LOG.info("Book: " + cachedBook.getTitle());

        // данные из БД извлекаются только 1 раз при первом обращении
        verify(repository, times(1)).findBookByTitleAndAuthor("Тихий дон", "М. А. Шолохов");

        //debug -> Evaluate Expression...-> Evaluate
        applicationContext.getBean("cacheManager");
    }

    @Test
    void testSaveBookAndPutCache() {
        final long bookId = 4L;

        Book book = new Book(bookId, "Капитанская дочка", "А.С. Пушкин");

        // сохранение и кэширование
        Book savedBook = service.saveBook(book);

        // получение данных из кэша
        Book foundedBook = service.findBookById(savedBook.getId());
        assertNotNull(foundedBook, "Book is not found");
        LOG.info("Book: " + foundedBook.getTitle());

        // данные закэшированы при сохранении и при запросе из БД не извлекаются
        verify(repository, never()).findById(bookId);
    }

    @Test
    void testSaveBookWithoutPutCache() {
        final long bookId = 5L;

        Book book = new Book(bookId, "Война и мир", "Л.Н. Толстой");

        // сохранение без кэширования
        Book savedBook = service.saveBookWithoutCachePut(book);

        // первое обращение к сервису, получение данных из БД и кэширование
        Book foundedBook = service.findBookById(savedBook.getId());
        assertNotNull(foundedBook, "Book is not found");
        LOG.info("Book: " + foundedBook.getTitle());

        // второе обращение к сервису, получение данных из кэша
        foundedBook = service.findBookById(savedBook.getId());
        assertNotNull(foundedBook, "Book is not found");
        LOG.info("Book: " + foundedBook.getTitle());

        // данные из БД извлекаются только 1 раз при первом обращении
        verify(repository, times(1)).findById(bookId);
    }

    @Test
    void testDeleteBookAndCacheEvict() {
        final long bookId = 2L;

        //получение данных из БД и кэширование
        Book foundedBook = service.findBookById(bookId);

        // удаление данных из БД и кэшa
        service.deleteBook(foundedBook);

        foundedBook = service.findBookById(bookId);
        assertNull(foundedBook, "Book is not found");
    }
    @Test// Проверка того, что приложение работает не правильно т.к. данные из БД удалены, а из кэша нет
    void testDeleteBookWithoutCacheEvict() {
        final long bookId = 3L;

        // первый вызов findBookById, получение данных из БД и кэширование
        Book foundedBook = service.findBookById(bookId);

        // удаление данных из БД
        service.deleteBookWithoutCacheEvict(foundedBook);

        // второй вызов findBookById, получение закэшированных данных
        foundedBook = service.findBookById(bookId);
        assertNotNull(foundedBook, "Book is not found");
        LOG.info("Book: " + foundedBook.getTitle());

        // данные из БД извлекаются только 1 раз при первом обращении
        verify(repository, times(1)).findById(bookId);
    }

    /**
     * Пример того как можно получить экземпляр бина cacheManager для анализа
     */
    private void cacheManagerBean() {
        applicationContext.getBean("cacheManager");
    }

}
