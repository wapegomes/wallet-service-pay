FROM maven:3.9-eclipse-temurin-21-alpine as build
WORKDIR /workspace/app

# Copiar apenas o pom.xml primeiro para aproveitar o cache de dependências do Docker
COPY pom.xml .
# Baixar todas as dependências (incluindo Redis) para que possam ser cacheadas
RUN mvn dependency:go-offline

# Agora copiar o código fonte e compilar
COPY src src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp

# Adicionar metadados à imagem
LABEL maintainer="Wallet Service Team"
LABEL version="1.0.0"
LABEL description="Wallet Service with Redis Cache, Spring Security, and Resilience4j"

# Configurações para a JVM
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -Dspring.profiles.active=prod"

# Configurações de segurança
ENV WALLET_APP_JWT_SECRET=walletServiceSecretKey123456789012345678901234567890
ENV WALLET_APP_JWT_EXPIRATION_MS=86400000

# Copiar o jar da fase de build
COPY --from=build /workspace/app/target/*.jar app.jar

# Definir o ponto de entrada com configurações otimizadas
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
