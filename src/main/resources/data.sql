CREATE TABLE book
(
    id              INT         NOT NULL,
    title           VARCHAR(50) NOT NULL,
    author          VARCHAR(50) NOT NULL
);

INSERT INTO book (id, title, author) VALUES (1, 'Тихий дон', 'М. А. Шолохов');
INSERT INTO book (id, title, author) VALUES (2, 'Как закалялась сталь', 'Н. А. Островский');
INSERT INTO book (id, title, author) VALUES (3, 'Старуха Изергиль', 'М. Горький');