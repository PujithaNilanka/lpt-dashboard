# Stage 1: build
FROM maven:3.9.2-eclipse-temurin-17 as builder
WORKDIR /workspace
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN mvn -q -N dependency:resolve

COPY src ./src
RUN mvn -DskipTests package -Pnative -DskipNative -DskipITs -e -DskipTests=true package

# Stage 2: run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
