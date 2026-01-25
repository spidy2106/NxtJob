FROM eclipse-temurin:17-jdk

WORKDIR /app

# Ensure there is NO slash before target
COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]