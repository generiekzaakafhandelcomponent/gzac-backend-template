FROM eclipse-temurin:17-jre-noble

ADD build/libs/gzac-backend-template.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]