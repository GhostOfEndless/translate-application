# Тестовое задание на Т‑Банк Финтех Java-разработчик
## Описание решения
Стек технологий:
* Spring Boot (Web, JDBC, Validation, Test, Testcontainers)
* Flyway
* PostgreSQL
* Gradle
* Lombok
* Testcontainers
* JUnit
* Docker/Docker Compose

В каталоге `src/main/java/com/example` лежат пакеты, реализующие основную логику работы приложения:
* `controller` - здесь находится `TranslationRestController`, отвечающий за взаимодействие с пользователем
  посредством HTTP-запросов. `BadRequestControllerAdvice` обрабатывает ошибки ввода пользователя и выводит
  локализованное сообщение об ошибке. В пакете `payload` лежит record `TranslationRequestPayload` - это объект, 
  который получает
  сервер от пользователя при отправке запроса на перевод.
* `entity` - содержит единственный entity-класс `Translation`. Объекты этого класса сохраняются в базу данных и
  возвращаются пользователю в `TranslationRestController`.
* `repository` - тут располагается интерфейс `TranslationRepository`, который работает с БД.
* `exceptions` - этот пакет содержит некоторые исключения, которые могут возникать во время работы приложения.
* `config` - содержит конфигурационный класс `ApplicationConfig`, который создаёт бины из свойств, описанных в `application.yaml` и 
   объект RestTemplate для выполнения запросов к API Yandex.Cloud
* `client` - здесь располагаются интерфейс `YandexCloudRestClient` и его реализация `YandexCloudRestClientImpl` 
   для взаимодействия с API Yandex.Cloud посредством HTTP-запросов. Запросы выполняются при помощи RestTemplate. 
   Пакет `payload` содержит record-классы, объекты которых `YandexCloudRestClientImpl` получает от Yandex.Cloud
* `service` - в этом пакете находится единственный интерфейс `TranslationService` и реализующий его класс
  `TranslationServiceImpl`. Этот сервис обрабатывает входную строку для перевода, запускает параллельные потоки для 
   HTTP-запросов к YandexCloud и взаимодействует с `TranslationRepository`.

В `src/main/resources/db/migration` находится файл миграции БД `V1__Basic_schema.sql`,
создающий в новой БД схему и таблицу.

Директория `src/test/java/com/example` содержит абстрактный класс `BaseTest`, создающий тестовую базу данных, 
а в пакете `controller` располагается класс тестов `TranslationRestControllerTest`, покрывающий основные
случаи при использовании API приложения.
## Инструкция по запуску
**Требования: в системе должен быть установлен docker и docker-compose**

1. Скачайте архив с репозиторием в удобное место у себя на компьютере:
    ```
    git clone https://github.com/GhostOfEndless/translate-application.git
    ```
2. Далее перейдите в директорию с файлом *docker-compose.yml*:
    ```
    cd translate-application
    ```
3. Теперь необходимо изменить API-ключ для доступа к сервисам Yandex.Cloud:
   * С помощью любого удобного редактора откройте файл `.env`, находящийся в корне проекта 
   и отредактируйте следующую переменную:
     ```
     ...
     YANDEX_CLOUD_API_KEY=<API-KEY>
     ...
     ```
     Вместо `<API-KEY>` необходимо вставить предоставленный API-ключ.
4. Теперь можно запустить приложение:
    * Для Linux систем:
      ```
      docker compose up
      ```
    * Для Windows систем:
      ```
      docker-compose up
      ```
