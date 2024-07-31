# Foot-Scaner app 

<img src="Logotype primary.png" width="60%" height="60%" />

[![Build Status](https://travis-ci.org/bauerjj/Android-Simple-Bluetooth-Example.svg?branch=master)](https://travis-ci.org/bauerjj/Android-Simple-Bluetooth-Example)
# Чтобы собрать и запустить - надо просто открыть проект в android studio. И он сам соберётся

# Логика работы 


 - Сначала ищем девайс с названием raspberrypi - если находим, то подключаемся к нему
если нет, то снова и снова запускаем поиск пока не найдём 
 -  Если нашли наш девайс то подключаемся к нему и шлём команду connected? - и ждём ответ 
( ответ и вообще общение в программе, происходит по Broadcast receiver ) если ответ положительный, мы его парсим в callback 
 тогда меняем надпись и можем работать с отправкой данных на устройство и получение данных 

 - Как работает контакт с устройством описано вот тут https://miro.com/app/board/uXjVNu0XjF0=/?moveToWidget=3458764594643337225&cot=14
 - 