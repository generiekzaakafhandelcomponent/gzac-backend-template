FROM openjdk:13.0.2

ADD build/libs/gzac-backend.war /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]