INSERT INTO users (name, login, email, birthday) VALUES
('Иван Иванов', 'ivan123', 'ivan@example.com', '1990-01-01'),
('Мария Петрова', 'masha_p', 'maria@example.com', '1992-02-02'),
('Алексей Смирнов', 'lexa_s', 'alex@example.com', '1988-03-03');

INSERT INTO friendship (user_id, friend_id, status) VALUES
(1, 2, 'CONFIRMED'),
(2, 1, 'CONFIRMED');

INSERT INTO friendship (user_id, friend_id, status) VALUES
(1, 3, 'CONFIRMED'),
(3, 1, 'CONFIRMED');

INSERT INTO friendship (user_id, friend_id, status) VALUES
(2, 3, 'CONFIRMED'),
(3, 2, 'CONFIRMED');

INSERT INTO genre (name) VALUES ('Комедия');
INSERT INTO genre (name) VALUES ('Драма');
INSERT INTO genre (name) VALUES ('Мультфильм');
INSERT INTO genre (name) VALUES ('Триллер');
INSERT INTO genre (name) VALUES ('Документальный');
INSERT INTO genre (name) VALUES ('Боевик');

INSERT INTO rating (rating_id, rating_name) VALUES (1, 'G');
INSERT INTO rating (rating_id, rating_name) VALUES (2, 'PG');
INSERT INTO rating (rating_id, rating_name) VALUES (3, 'PG-13');
INSERT INTO rating (rating_id, rating_name) VALUES (4, 'R');
INSERT INTO rating (rating_id, rating_name) VALUES (5, 'NC-17');