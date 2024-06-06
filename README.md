Этот код представляет собой набор компонентов для создания приложения, которое сканирует IP-адреса в заданном диапазоне на наличие SSL-сертификатов и собирает информацию о доменах, указанных в этих сертификатах. Приложение состоит из нескольких модулей:

### 1. **SSLScanner**
- **Основная задача**: Сканирование IP-адресов на наличие SSL-сертификатов.
- **Функциональность**:
  - Создание HTTP-клиента с поддержкой HTTPS через `PoolingHttpClientConnectionManager` и `SSLConnectionSocketFactory`, который позволяет игнорировать проверку подлинности сертификатов (`NoopHostnameVerifier`).
  - Использование многопоточности для параллельного выполнения запросов к каждому IP-адресу в списке.
  - Сбор информации о доменах, указанных в сертификатах, и сохранение этой информации в файле.

### 2. **IPAddressRange**
- **Основная задача**: Расчет списка IP-адресов на основе CIDR-маски.
- **Функциональность**:
  - Принимает строку с CIDR-маской и вычисляет все возможные IP-адреса в этом диапазоне.
  - Возвращает массив IP-адресов, которые затем используются для сканирования.

### 3. **App**
- **Основная задача**: Запуск сервера и обработка входящих запросов от пользователей.
- **Функциональность**:
  - Настройка статических файлов и маршрутов для веб-приложения.
  - Обработка POST-запросов для запуска сканирования по IP-диапазону, полученному от пользователя.
  - Отображение результатов сканирования через HTML-страницы.

### 4. **ScanTask**
- **Основная задача**: Выполнение отдельной задачи сканирования для одного IP-адреса.
- **Функциональность**:
  - Подключение к IP-адресу и извлечение сертификатов SSL.
  - Извлечение информации о домене из сертификатов и добавление её в общий список для последующего сохранения.

### Общие особенности:
- Все компоненты написаны на Java и используют библиотеку Apache HttpClient для работы с HTTP и HTTPS.
- Для упрощения работы с SSL используется стратегия доверия ко всем сертификатам (`TrustSelfSignedStrategy`), что может быть небезопасным в реальных условиях использования.
- Многопоточность обеспечивает возможность одновременного обслуживания нескольких IP-адресов.
- Результаты сканирования сохраняются в текстовый файл, что позволяет легко анализировать результаты после завершения процесса.

Этот проект демонстрирует использование основных паттернов проектирования, таких как Singleton (для логгера), Factory (для создания экземпляров HTTP-клиента), и Observer (через Future для ожидания завершения задач).

Citations: