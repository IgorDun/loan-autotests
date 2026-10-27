# language: ru
@ui @webdriver @saucedemo
Функционал: Покупка в демо-магазине saucedemo.com

  Предыстория:
    * saucedemo > Перехожу на сайт

  @smoke
  Сценарий: Успешный вход стандартного пользователя
    * saucedemo > Авторизуюсь как 'standard_user' с паролем 'secret_sauce'
    * saucedemo > Вижу каталог товаров

  Сценарий: Заблокированный пользователь не может войти
    * saucedemo > Авторизуюсь как 'locked_out_user' с паролем 'secret_sauce'
    * saucedemo > Вижу ошибку входа, содержащую 'locked out'

  Сценарий: Оформление заказа из двух товаров
    * saucedemo > Авторизуюсь как 'standard_user' с паролем 'secret_sauce'
    * saucedemo > Добавляю в корзину товар 'Sauce Labs Backpack'
    * saucedemo > Добавляю в корзину товар 'Sauce Labs Bike Light'
    * saucedemo > Проверяю, что в корзине 2 товара
    * saucedemo > Оформляю заказ на 'Иван' 'Тестов' с индексом '101000'
    * saucedemo > Вижу подтверждение заказа 'Thank you for your order!'
