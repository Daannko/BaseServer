#!/usr/bin/env bash
# Sets an account's password. Run it on the Pi, in the deployment folder:
#   ./reset-password.sh                 prompts, for user@user.com
#   ./reset-password.sh me@example.com  prompts, for that account
# Also ends the account's open sessions.
set -euo pipefail
cd "$(dirname "$0")"

EMAIL="${1:-user@user.com}"
if [ -t 0 ]; then
    read -rsp "New password for $EMAIL: " PASSWORD; echo
    read -rsp "Repeat: " REPEAT; echo
    [ "$PASSWORD" = "$REPEAT" ] || { echo "Passwords differ" >&2; exit 1; }
else
    read -r PASSWORD
fi
[ "${#PASSWORD}" -ge 12 ] || { echo "Use at least 12 characters" >&2; exit 1; }

# Hex-encoded and sent on stdin, so the password never appears in a process list.
HEX=$(printf %s "$PASSWORD" | od -An -tx1 | tr -d ' \n')
DB_PASSWORD=$(grep POSTGRES_PASSWORD .env | cut -d= -f2)
UPDATED=$(docker compose exec -T -e PGPASSWORD="$DB_PASSWORD" postgres psql -U myuser -d mydatabase -qAt -v ON_ERROR_STOP=1 <<SQL
create extension if not exists pgcrypto;
delete from refresh_token where user_id in (select id from users where email = '$EMAIL');
with changed as (
    update users set password = crypt(convert_from(decode('$HEX', 'hex'), 'UTF8'), gen_salt('bf', 10))
    where email = '$EMAIL' returning 1
) select count(*) from changed;
drop extension pgcrypto;
SQL
)
[ "$UPDATED" = "1" ] || { echo "No account $EMAIL" >&2; exit 1; }
echo "Password changed for $EMAIL"
