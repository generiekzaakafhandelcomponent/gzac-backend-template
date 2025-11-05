FROM eclipse-temurin:21-jdk-alpine

ADD build/libs/gzac-backend-template.war /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
