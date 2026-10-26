spring.application.name=gorzdrav_spb_bot
token.bot=${BOT_TOKEN}
gorzdrav.api.url=${GORZDRAV_API_URL}


spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.data.redis.host=${SPRING_DATA_REDIS_HOST}
spring.data.redis.port=${SPRING_DATA_REDIS_PORT}
# Таймауты сети внутри Docker
spring.data.redis.connect-timeout=2000ms
spring.data.redis.timeout=1000ms
# Настройка пула соединений Lettuce
spring.data.redis.lettuce.pool.enabled=true
# Максимальное количество активных соединений под нагрузку
spring.data.redis.lettuce.pool.max-active=32
# Сколько соединений держать открытыми в простое
spring.data.redis.lettuce.pool.max-idle=16
# Минимальное количество всегда живых "горячих" коннектов
spring.data.redis.lettuce.pool.min-idle=4
# Сколько поток бота ждет свободное соединение из пула, прежде чем выдать ошибку
spring.data.redis.lettuce.pool.max-wait=3000ms
# интервал фоновой очистки пула от мертвых сокетов
spring.data.redis.lettuce.pool.time-between-eviction-runs=30s

spring.jpa.generate-ddl=true
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.jdbc.time_zone=Europe/Moscow

spring.jackson.time-zone=Europe/Moscow
spring.jackson.deserialization.adjust-dates-to-context-time-zone=false