FROM openjdk:21-bullseye

ADD libs/gzac-backend.war /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]