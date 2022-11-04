FROM openjdk:13.0.2

ADD *.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]