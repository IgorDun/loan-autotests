# language: ru
@api @dummyjson
Функционал: REST API демо-сервиса dummyjson.com

  @smoke
  Сценарий: Авторизация и получение профиля по токену
    * dummyjson > Авторизуюсь, сохраняю токен как 'token' и ответ как 'login':
      | username      | emilys     |
      | password      | emilyspass |
      | expiresInMins | 5          |
    * Проверяю в json 'login' поля:
      | $.username    | emilys  |
      | $.accessToken | [\w.-]+ |
    * dummyjson > Получаю профиль пользователя по токену '${token}' и сохраняю как 'profile'
    * Проверяю в json 'profile' поля:
      | $.username | emilys |
      | $.id       | \d+    |

  Сценарий: Поиск товаров с параметрами запроса
    * dummyjson > Ищу товары и сохраняю результат как 'search':
      | q     | phone |
      | limit | 5     |
    * Проверяю в json 'search' поля:
      | $.limit          | 5   |
      | $.products[0].id | \d+ |

  Сценарий: Создание товара по шаблону
    * Сохраняю значения в переменную:
      | title | Ноутбук-@{RandomUtils.randomInt(1000, 9999)} |
    * dummyjson > Создаю товар и сохраняю как 'created':
      | title | ${title} |
    * Проверяю в json 'created' поля:
      | $.id       | \d+      |
      | $.title    | ${title} |
      | $.category | laptops  |

  Сценарий: Удаление товара
    * dummyjson > Удаляю товар '1' и сохраняю ответ как 'deleted'
    * Проверяю в json 'deleted' поля:
      | $.isDeleted | true |

  Сценарий: Несуществующий товар не найден
    * dummyjson > Проверяю, что товар '0' не найден
