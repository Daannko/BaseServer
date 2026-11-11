#!/usr/bin/env bash
# Runs on the Pi. Adds board.dannko.com next to the existing vhosts, keeping them untouched.
set -euo pipefail
V4=$(curl -fs -m 10 https://www.cloudflare.com/ips-v4)
V6=$(curl -fs -m 10 https://www.cloudflare.com/ips-v6)
[ "$(echo "$V4" | wc -l)" -ge 10 ] && [ "$(echo "$V6" | wc -l)" -ge 5 ]

CONF=$(mktemp)
{
cat <<'EOF'
server {
    listen 443 ssl;
    server_name board.dannko.com;

    ssl_certificate /etc/ssl/cloudflare/home.dannko.com.pem;
    ssl_certificate_key /etc/ssl/cloudflare/home.dannko.com.key;

    # Cloudflare Access guards this site, so nothing may reach it around Cloudflare.
    # Ranges from https://www.cloudflare.com/ips-v4 and /ips-v6.
    allow 127.0.0.1;
EOF
for ip in $V4 $V6; do echo "    allow $ip;"; done
cat <<'EOF'
    deny all;

    # Board images are capped at 10 MB by the backend.
    client_max_body_size 11m;

    location / {
        proxy_pass http://localhost:49191;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        # AI endpoints wait on the DeepSeek API.
        proxy_read_timeout 300s;
    }
}

server {
    listen 80;
    server_name board.dannko.com;
    return 301 https://$host$request_uri;
}
EOF
} > "$CONF"

sudo -n install -m 644 "$CONF" /etc/nginx/sites-available/board
sudo -n ln -sf /etc/nginx/sites-available/board /etc/nginx/sites-enabled/board
if ! sudo -n nginx -t 2>&1 | tail -2; then
    sudo -n rm -f /etc/nginx/sites-enabled/board
    echo "nginx config invalid, vhost removed"; exit 1
fi
sudo -n systemctl reload nginx
cp "$CONF" /home/daniel/baseserver/board.vhost.conf
rm -f "$CONF"

R="--resolve board.dannko.com:443:127.0.0.1"
echo "== through vhost from localhost"
curl -sk $R -m 8 -o /dev/null -w "front %{http_code}\n" https://board.dannko.com/
curl -sk $R -m 8 -o /dev/null -w "api me %{http_code}\n" https://board.dannko.com/api/user/me
curl -sk $R -m 8 -o /tmp/bs.out -w "login POST with https origin %{http_code} " -H "Origin: https://board.dannko.com" -H "Content-Type: application/json" -d '{"email":"nobody@example.test","password":"wrong"}' https://board.dannko.com/api/auth/login; grep -o '"message"[^,]*' /tmp/bs.out || cat /tmp/bs.out; rm -f /tmp/bs.out
echo "== around Cloudflare must be refused"
curl -sk -m 8 --resolve board.dannko.com:443:192.168.1.149 -o /dev/null -w "from LAN address %{http_code}\n" https://board.dannko.com/
echo "== other sites"
for h in racketwatch vault home; do curl -sk -m 8 --resolve $h.dannko.com:443:127.0.0.1 -o /dev/null -w "$h %{http_code}\n" https://$h.dannko.com/; done
