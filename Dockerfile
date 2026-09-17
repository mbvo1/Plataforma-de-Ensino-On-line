# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml files first for better caching
COPY sigea/pom.xml sigea/
COPY sigea/sigea-pai/pom.xml sigea/sigea-pai/
COPY sigea/dominio-principal/pom.xml sigea/dominio-principal/
COPY sigea/aplicacao/pom.xml sigea/aplicacao/
COPY sigea/infraestrutura/pom.xml sigea/infraestrutura/
COPY sigea/apresentacao-backend/pom.xml sigea/apresentacao-backend/

# Download dependencies
RUN cd sigea && mvn dependency:go-offline -B

# Copy source code
COPY sigea sigea

# Build the application
RUN cd sigea && mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/sigea/apresentacao-backend/target/sigea-apresentacao-backend-1.0.0-SNAPSHOT.jar app.jar

# Copy frontend files
COPY --from=build /app/sigea/apresentacao-frontend/public /app/static

# Create uploads directory
RUN mkdir -p /app/uploads

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
