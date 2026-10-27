# language: ru
@api @dummyjson
Функционал: REST API демо-сервиса dummyjson.com

  @smoke
  Сценарий: Авторизация и получение профиля по токену
    * dummyjson > Отправляю POST запрос на '/auth/login' с телом и сохраняю ответ как 'login':
      """
      {"username": "emilys", "password": "emilyspass", "expiresInMins": 5}
      """
    * Проверяю, что код ответа 'login' равен 200
    * Сохраняю объект из json 'login' по пути:
      | token | $.accessToken |
    * Проверяю в json 'login' поля:
      | $.username    | emilys    |
      | $.accessToken | [\w.-]+   |

  Сценарий: Поиск товаров с параметрами запроса
    * dummyjson > Отправляю GET запрос на '/products/search' с параметрами и сохраняю ответ как 'search':
      | q     | phone |
      | limit | 5     |
    * Проверяю, что код ответа 'search' равен 200
    * Проверяю в json 'search' поля:
      | $.limit          | 5      |
      | $.products[0].id | \d+    |

  Сценарий: Создание товара из таблицы полей
    * Сохраняю значения в переменную:
      | title | Ноутбук-@{RandomUtils.randomInt(1000, 9999)} |
    * dummyjson > Отправляю POST запрос на '/products/add' с полями и сохраняю ответ как 'created':
      | title    | ${title} |
      | category | laptops  |
    * Проверяю, что код ответа 'created' равен 201
    * Проверяю в json 'created' поля:
      | $.id       | \d+      |
      | $.title    | ${title} |
      | $.category | laptops  |

  Сценарий: Удаление товара
    * dummyjson > Отправляю DELETE запрос на '/products/1' и сохраняю ответ как 'deleted'
    * Проверяю, что код ответа 'deleted' равен 200
    * Проверяю в json 'deleted' поля:
      | $.isDeleted | true |

  Сценарий: Несуществующий товар возвращает 404
    * dummyjson > Отправляю GET запрос на '/products/0' и сохраняю ответ как 'missing'
    * Проверяю, что код ответа 'missing' равен 404
