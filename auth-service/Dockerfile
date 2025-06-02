# auth-service/Dockerfile
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copiar el pom.xml y descargar dependencias primero (para cache)
COPY pom.xml .
COPY .mvn/ .mvn
COPY mvnw .
RUN ./mvnw dependency:go-offline

# Copiar el código fuente
COPY src ./src

# Compilar y empaquetar
RUN ./mvnw clean package -DskipTests

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "target/auth-service-0.0.1-SNAPSHOT.jar"]