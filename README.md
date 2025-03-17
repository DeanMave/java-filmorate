# Реализация базы данных

![Untitled](https://github.com/user-attachments/assets/c2391d77-842f-4897-b035-2af42220021b)

Запрос на получение всех фильмов:

SELECT *

FROM film;

Запрос на получение топа фильмов с лимитом 10:

SELECT f.*,COUNT(l.user_id) AS likes_film

FROM films AS f 

LEFT JOIN likes AS l f.film_id = l.film_id

GROUP BY f.film_id

ORDER BY likes_film DESC

LIMIT 10;

Запрос на получение всех пользователей:

SELECT *

FROM user;

Запрос на получение общих друзей пользователей с айди 1 и 2:

SELECT u.*

FROM user u

WHERE u.user_id IN (

    SELECT f1.friend_id
    
    FROM friendship f1
    
    JOIN friendship f2 ON f1.friend_id = f2.friend_id
    
    WHERE f1.user_id = 1
    
    AND f2.user_id = 2
    
    AND f1.status = 'confirmed'
    
    AND f2.status = 'confirmed'
)

