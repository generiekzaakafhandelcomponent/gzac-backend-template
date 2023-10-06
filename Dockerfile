FROM openjdk:17-bullseye

ADD libs/gzac-backend.war /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]