# Getting started
## Prerequisites
- Java 13
- A running Keycloak instance
- A running MySQL server

## Configuration
- Add a properties file to the project root. This file should have the name ".env.properties"
  - The .env.properties.example file can be used as starting point
- Run the following command from a terminal in the project root: ```./gradlew bootrun```
- After the GZAC backend application has finished starting up, the service is available at http://localhost:8080
  - The application uptime can be verified by calling [this API endpoint](http://localhost:8080/api/ping)

## Supporting containers
The GZAC backend application requires a Keycloak instance and a database server. When running the application locally, running Keycloak and the database locally as well is recommended. In [this repository](https://github.com/generiekzaakafhandelcomponent/gzac-docker-compose), Docker compose files are available to support the GZAC application: 
- [Keycloak and MySQL](https://github.com/generiekzaakafhandelcomponent/gzac-docker-compose/blob/main/keycloak-and-mysql.yml)

# Adding implementation code
