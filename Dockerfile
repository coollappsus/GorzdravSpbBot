# Используем официальный образ Maven
FROM maven:3.9-amazoncorretto-21 as build

# Качаем файл pom.xml и весь java-код
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Используем официальный образ OpenJDK как базовый
FROM openjdk:17-jdk-slim-bullseye

# Копируем jar-ку, которая сгенерировалась выше
COPY --from=build /target/*.jar app.jar

# Откроем порт 8080
EXPOSE 8080

# Запустим
ENTRYPOINT ["java", "-jar", "/app.jar"]