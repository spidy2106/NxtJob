FROM eclipse-temurin:17-jdk

WORKDIR /app

# Ensure there is NO slash before target
COPY target/*.jar app.jar

EXPOSE 8080

# The "-Dspring.profiles.active=docker" part is the magic switch
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]