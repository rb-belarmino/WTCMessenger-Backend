# ─────────────────────────────────────────────────────────────
# Stage 1: BUILD
# Usa Maven + JDK 21 para compilar e empacotar o JAR
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copia apenas os arquivos de dependência primeiro (cache de camadas)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Baixa dependências sem compilar o código (cache eficiente)
RUN ./mvnw dependency:go-offline -B

# Copia o código fonte e faz o build
COPY src ./src
RUN ./mvnw package -DskipTests -B

# ─────────────────────────────────────────────────────────────
# Stage 2: RUNTIME
# Usa apenas JRE (imagem menor ~200MB vs ~600MB do JDK)
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Usuário não-root por segurança
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copia apenas o JAR final do stage de build
COPY --from=builder /app/target/*.jar app.jar

# Porta exposta pelo Tomcat
EXPOSE 8082

# Variáveis de ambiente obrigatórias (devem ser fornecidas no compose/k8s)
ENV MONGODB_URI=""
ENV KAFKA_BOOTSTRAP_SERVERS="kafka:9092"
ENV JWT_SECRET=""
ENV FIREBASE_PROJECT_ID=""
ENV FIREBASE_CREDENTIALS_PATH="classpath:firebase-service-account.json"

# Inicia a aplicação com configurações otimizadas para container
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
