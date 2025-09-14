FROM maven:3.9.11-eclipse-temurin-24 AS build

WORKDIR /usr/src/redis-server

COPY pom.xml .
COPY src ./src

RUN mvn package -DskipTests

FROM eclipse-temurin:23-jre-alpine AS run

WORKDIR /redis-server

COPY --from=build /usr/src/redis-server/target/codecrafters-redis.jar .

EXPOSE 6379

CMD ["java", "-jar", "codecrafters-redis.jar"]
