FROM amazoncorretto:17-alpine as build

WORKDIR /app
COPY buildSrc buildSrc
COPY gradle gradle
COPY settings.gradle.kts build.gradle.kts
COPY gradle.properties gradle.properties
COPY gradlew gradlew
COPY settings.gradle.kts settings.gradle.kts
COPY danmaku/ani danmaku/ani
COPY utils utils

RUN ./gradlew clean :danmaku:ani:server:installDist

FROM ibm-semeru-runtimes:open-17-jre

ENV PORT=4394

COPY --from=build app/danmaku/ani/server/build/install/server ./server
VOLUME ./server/vol
EXPOSE $PORT

ENTRYPOINT ["/bin/bash", "-c", "./server/bin/server"]