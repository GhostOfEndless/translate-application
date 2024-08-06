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
## Примеры тестовых запросов

Для демонстрации используем утилиту командной строки *curl*,
но запросы можно также делать из любого другого http клиента.
Подразумевается, что приложение запущено на локальной машине,
с которой делаются запросы (*localhost*).

* **Запрос на перевод**
  * URI: *http://localhost:8080/api/v1/translate*
  * Метод: *POST*
  * Тело:
    ```
    {
      "sourceLanguageCode": "<код исходного языка>",
      "targetLanguageCode": "<код целевого языка>",
      "text": "<текст>"
    }
    ```
  * **Пример запроса:**
    ```
    curl --request POST --json '{
         "sourceLanguageCode": "en",
         "targetLanguageCode": "ru",
         "text": "Hello world"
      }' http://localhost:8080/api/v1/translate
    ```
  * **Пример ответа:**
    ```
    {
      "text": "Здравствуйте мир"
    }
    ```
  * **Обработка ошибок.** Все пользовательские данные валидируются и при ошибках выводятся соответствующие сообщения:
    ```
    {
      "type": "about:blank",
      "title": "Bad Request",
      "status": 400,
      "detail": "Ошибка 400: некорректный запрос",
      "instance": "/api/v1/translate",
      "errors": [
                  "Текст должен быть указан",
                  "Исходный язык должен быть указан",
                  "Целевой язык должен быть указан"
                ]
    }
    ```

* **Постраничное отображение истории переводов**
   * URI: *http://localhost:8080/api/v1/translate/page/{pageNumber}?size={pageSize}*
   * Метод: *GET*
   * **Пример запроса:**
     ```
     curl --request GET http://localhost:8080/api/v1/translate/page/0
     ```
   * **Пример ответа:**
     ```
     {
       "content": [
                    {
                      "id": 1, 
                      "clientIP": "172.18.0.1",
                      "sourceLanguageCode": "en",
                      "targetLanguageCode": "ru",
                      "sourceText": "Hello world",
                      "translatedText": "Здравствуйте мир",
                      "requestTimestamp": "2024-08-06 16:08:17.715",
                      "responseTimestamp": "2024-08-06 16:08:17.943"
                    }
                  ],
       "page": {
                 "size": 5, 
                 "number": 0,
                 "totalElements": 1,
                 "totalPages": 1
               }
     }
     ```

* **Получение перевода по id**
   * URI: *http://localhost:8080/api/v1/translate/{translationId}*
   * Метод: *GET*
   * **Пример запроса:**
     ```
     curl --request GET http://localhost:8080/api/v1/translate/1
     ```
   * **Пример ответа:**
     ```
     {
       "id": 1,
       "clientIP": "172.18.0.1",
       "sourceLanguageCode": "en",
       "targetLanguageCode": "ru",
       "sourceText": "Hello world",
       "translatedText": "Здравствуйте мир",
       "requestTimestamp": "2024-08-06 16:08:17.715",
       "responseTimestamp": "2024-08-06 16:08:17.943"
     }
     ```
   * **Обработка ошибок.** Если пользователь вводит id несуществующего перевода, 
     API возвращает соответствующую ошибку
     ```
     {
       "type": "about:blank",
       "title": "Not Found",
       "status": 404,
       "detail": "Перевод не найден",
       "instance": "/api/v1/translate/2"
     }
     ```