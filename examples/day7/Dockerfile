FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -B -DskipTests package
FROM eclipse-temurin:21-jre
ARG SERVICE
WORKDIR /app
COPY --from=build /workspace/${SERVICE}/target/${SERVICE}-1.0.0.jar app.jar
USER 10001
ENTRYPOINT ["java","-XX:MaxRAMPercentage=70","-jar","/app/app.jar"]
