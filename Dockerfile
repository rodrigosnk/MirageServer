
FROM ubuntu:latest AS build

RUN apt-get update && apt-get install -y openjdk-21-jdk maven

WORKDIR /app
COPY . .

RUN mvn clean install -DskipTests


FROM openjdk:21-jdk-slim

WORKDIR /app
EXPOSE 8080


COPY --from=build /app/target/MirageServer-0.0.1-SNAPSHOT.jar app.jar


ENV PORT=8080
CMD ["java", "-jar", "app.jar"]
