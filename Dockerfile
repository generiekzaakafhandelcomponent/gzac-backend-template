FROM openjdk:17-bullseye

ADD build/libs/gzac-backend.war /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]