# ---------- Build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# ---------- Runtime ----------
FROM eclipse-temurin:17-jre
# Tesseract nativo (o tess4j usa a libtesseract do sistema no Linux)
RUN apt-get update \
 && apt-get install -y --no-install-recommends tesseract-ocr libtesseract-dev fontconfig \
 && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY tessdata ./tessdata
ENV KAPTU_OCR_TESSDATA=/app/tessdata \
    KAPTU_PASTA_ARQUIVOS=/tmp/arquivos \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -Djava.awt.headless=true"
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
