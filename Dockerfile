
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app
COPY . .

RUN ./mvnw clean install -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app
EXPOSE 8080

COPY --from=build /app/target/MirageServer-0.0.1-SNAPSHOT.jar app.jar

ENV PORT=8080
CMD ["java", "-jar", "app.jar"]
