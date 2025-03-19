# Схема базы данных
![БД.png](images/%D0%91%D0%94.png)
# Примеры запросов по фильмам
 Получение всех фильмов
```sql
SELECT * 
FROM film;
```
Получение топа фильмов по лайкам
```sql
SELECT f.name,COUNT(l.user_id) AS likes_film
FROM film AS f
LEFT JOIN likes AS l ON f.film_id=l.film_id
GROUP BY f.film_id
ORDER BY likes_film DESC;
```
Получение рейтинга фильма
```sql
SELECT f.name, r.name
FROM film AS f
JOIN rating AS r ON f.rating_id = r.rating_id;
```
# Примеры запросов по пользователям 
Получение всех пользователей
```sql
SELECT * 
FROM user;
```
Получение общих друзей двух пользователей
```sql
SELECT u.name
FROM user AS u
WHERE u.user_id IN (
SELECT f1.friend_id
FROM friendship AS f1
JOIN friendship AS f2 ON f1.friend_id = f2.friend_id
WHERE f1.user_id = :id
AND f2.user_id = :id
AND f1.status = 'confirmed'
AND f2.status = 'confirmed');
```