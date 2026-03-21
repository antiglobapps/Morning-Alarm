FROM gradle:8.13-jdk17 AS build

WORKDIR /workspace

COPY settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY shared ./shared
COPY server ./server

RUN gradle :server:installDist --no-daemon

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends bash curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /workspace/server/build/install/server ./server

EXPOSE 8080

ENTRYPOINT ["./server/bin/server"]
