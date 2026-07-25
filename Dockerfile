FROM eclipse-temurin:21-jre-noble

COPY build/libs/gzac-backend-template.war /app.jar

# Import any mounted custom CA certificates before starting the app.
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
