FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Create non-root user
RUN addgroup --system spring && adduser --system spring --ingroup spring

# Change ownership
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
