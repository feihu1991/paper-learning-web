FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV DB_HOST=localhost
ENV DB_PORT=3306
ENV DB_NAME=paper_learning
ENV DB_PASSWORD=

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
