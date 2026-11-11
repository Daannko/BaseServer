#!/usr/bin/env bash
# Uploads the already built frontend to the Raspberry Pi and restarts only nginx.
# Backend and databases keep running. The previous build stays in front.prev.
# Build first: npx ng build --configuration lan --optimization=true --output-hashing=all --source-map=false
set -euo pipefail

PI=daniel@10.10.10.1
REMOTE_DIR=/home/daniel/baseserver
HERE="$(cd "$(dirname "$0")" && pwd)"
DIST="$HERE/../../../BaseServer_front/dist/base-server-app/browser"

[ -f "$DIST/index.html" ] || { echo "No build in $DIST" >&2; exit 1; }

ssh "$PI" "rm -rf $REMOTE_DIR/front.new && mkdir $REMOTE_DIR/front.new"
tar -C "$DIST" -cf - . | ssh "$PI" "tar -C $REMOTE_DIR/front.new -xf -"

# front is bind mounted, so web must be recreated to pick up the new folder.
ssh "$PI" "cd $REMOTE_DIR && test -f front.new/index.html && test -d front && \
    rm -rf front.prev && mv front front.prev && mv front.new front && \
    docker compose up -d --no-deps --force-recreate web && \
    sleep 2 && docker compose ps web && \
    curl -s http://127.0.0.1:49191/ | grep -oE 'main-[A-Z0-9]+\.js' | sort -u"
