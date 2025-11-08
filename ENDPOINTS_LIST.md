# Список всех эндпоинтов API

**Общее количество эндпоинтов: 40**

## AuthorizationController (3 эндпоинта)

1. `POST /auth/register` - Регистрация пользователя
2. `POST /auth/login` - Авторизация пользователя
3. `POST /auth/getAllUsers` - Получение всех пользователей

## BookingController (4 эндпоинта)

4. `POST /bookings` - Создание нового бронирования
5. `GET /bookings/{establishmentId}/available` - Получение доступных столов в заведении на указанное время
6. `GET /bookings/user/{userId}` - Получение всех бронирований конкретного пользователя
7. `DELETE /bookings/{bookingId}` - Отмена (удаление) бронирования

## EstablishmentController (10 эндпоинтов)

8. `GET /establishments/user/{userId}` - Получение заведений по ID пользователя
9. `GET /establishments/getAll` - Получение всех заведений
10. `GET /establishments/markers` - Получение облегченных данных для маркеров на карте
11. `GET /establishments/search` - Поиск заведений по названию или адресу
12. `GET /establishments/{id}` - Получение заведения по ID
13. `POST /establishments/create` - Создание нового заведения
14. `PUT /establishments/{id}` - Обновление заведения
15. `DELETE /establishments/{id}` - Удаление заведения
16. `PUT /establishments/{id}/status` - Обновление статуса заведения
17. `GET /establishments/pending` - Получение неодобренных (PENDING) заведений

## MenuController (11 эндпоинтов)

18. `GET /menu/establishment/{establishmentId}` - Получение полного меню заведения
19. `POST /menu/group/food` - Создание новой группы еды
20. `PUT /menu/group/food/{groupId}` - Обновление группы еды
21. `POST /menu/drink/group` - Создание новой группы напитков
22. `PUT /menu/drink/group/{groupId}` - Обновление группы напитков
23. `DELETE /menu/group/{groupId}` - Удаление группы (еды или напитков)
24. `POST /menu/item/food` - Создание нового блюда
25. `PUT /menu/item/food/{itemId}` - Обновление блюда
26. `POST /menu/item/drink` - Создание нового напитка
27. `PUT /menu/item/drink/{itemId}` - Обновление напитка
28. `DELETE /menu/item/{itemId}` - Удаление компонента меню (блюда или напитка)

## ReviewController (3 эндпоинта)

29. `POST /reviews/create` - Создание отзыва
30. `GET /reviews/establishment/{establishmentId}` - Получение отзывов по ID заведения
31. `DELETE /reviews/{id}` - Удаление отзыва по ID

## TableController (3 эндпоинта)

32. `POST /tables/establishment/{establishmentId}/create` - Создание списка столиков для заведения
33. `GET /tables/establishment/{establishmentId}` - Получение всех столиков заведения
34. `DELETE /tables/{id}` - Удаление столика по ID

## TestController (2 эндпоинта)

35. `GET /test/ping` - Тестовый эндпоинт (возвращает "pong")
36. `GET /` - Главная страница (возвращает HTML)

## UserController (4 эндпоинта)

37. `GET /user/me` - Получение данных пользователя по ID
38. `PUT /user/me` - Обновление данных пользователя
39. `PUT /user/me/password` - Обновление пароля пользователя
40. `DELETE /user/me` - Удаление пользователя по ID

