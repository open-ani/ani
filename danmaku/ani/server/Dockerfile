FROM ani-danmaku-server-build as build

FROM ibm-semeru-runtimes:open-17-jre

ENV PORT=4394

COPY --from=build app/danmaku/ani/server/build/install/server ./server
VOLUME ./server/vol
EXPOSE $PORT

ENTRYPOINT ["/bin/bash", "-c", "./server/bin/server"]
