FROM openjdk:13.0.2

ADD build/*.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]