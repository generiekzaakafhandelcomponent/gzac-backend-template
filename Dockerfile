FROM openjdk:17.0.2-bullseye

ADD build/libs/gzac-backend.war /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]