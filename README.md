# Getting started
## Prerequisites
- Java 13
- A running Keycloak instance
- A running MySQL server

## Configuration
- Add a properties file to the project root. This file should have the name ".env.properties"
  - The .env.properties.example file can be used as starting point
  - The .env.properties.example file configuration is based on the Keycloak and MySQL containers defined in [this Docker compose file](https://github.com/generiekzaakafhandelcomponent/gzac-docker-compose/blob/main/keycloak-and-mysql.yml)
- Run the following command from a terminal in the project root: ```./gradlew bootrun```
- After the GZAC backend application has finished starting up, the service is available at http://localhost:8080
  - The application uptime can be verified by calling [this API endpoint](http://localhost:8080/api/ping)
