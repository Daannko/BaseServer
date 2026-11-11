#!/usr/bin/env bash
# Builds backend + frontend on this PC and (re)deploys them to the Raspberry Pi.
# Usage: ./deploy.sh            build everything, upload, restart
#        ./deploy.sh --no-build upload what is already built
set -euo pipefail

PI=daniel@10.10.10.1
REMOTE_DIR=/home/daniel/baseserver
HERE="$(cd "$(dirname "$0")" && pwd)"
BACKEND="$HERE/../.."
FRONT="$BACKEND/../BaseServer_front"

if [ "${1:-}" != "--no-build" ]; then
    # Lombok 1.18.30 generates nothing on JDK 25 (the PC default), so build on 17.
    JDK17="/c/Program Files/Java/jdk-17"
    if [ -d "$JDK17" ]; then export JAVA_HOME="$JDK17" PATH="$JDK17/bin:$PATH"; fi
    (cd "$BACKEND" && ./mvnw.cmd -q -DskipTests package)
    (cd "$FRONT" && npx ng build --configuration lan --optimization=true --output-hashing=all --source-map=false)
fi

ssh "$PI" "mkdir -p $REMOTE_DIR/uploads && rm -rf $REMOTE_DIR/front && mkdir $REMOTE_DIR/front"
scp "$HERE/compose.yaml" "$HERE/nginx.conf" "$HERE/reset-password.sh" "$PI:$REMOTE_DIR/"
# The jar bundles env.properties and the JWT keys, so only its owner may read it.
scp "$BACKEND"/target/BaseServer-*.jar "$PI:$REMOTE_DIR/app.jar"
tar -C "$FRONT/dist/base-server-app/browser" -cf - . | ssh "$PI" "tar -C $REMOTE_DIR/front -xf -"

ssh "$PI" "cd $REMOTE_DIR && chmod 600 app.jar && chmod 700 reset-password.sh && \
    { [ -f .env ] || { umask 077; echo POSTGRES_PASSWORD=\$(openssl rand -hex 24) > .env; }; } && \
    docker compose up -d --force-recreate backend web && docker compose up -d && docker compose ps"
