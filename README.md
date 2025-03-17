# Реализация базы данных

![Untitled](https://github.com/user-attachments/assets/c2391d77-842f-4897-b035-2af42220021b)

Запрос на получение всех фильмов:
SELECT *
FROM film;
Запрос на получение топа фильмов:
SELECT f.*,COUNT(l.user_id) AS likes_film
FROM films AS f 
LEFT JOIN likes AS l f.film_id = l.film_id
GROUP BY f.film_id
ORDER BY likes_film DESC
LIMIT 10;
Запрос на получение всех пользователей:
SELECT *
FROM user;
Запрос на получение 
