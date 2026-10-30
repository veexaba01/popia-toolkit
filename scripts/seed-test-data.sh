#!/usr/bin/env bash
# Creates two test organisations, each with an admin, a staff member and four inventory items,
# on a LOCAL instance of the app. For testing only.
set -euo pipefail

BASE="${BASE:-http://localhost:8081}"
if [[ ! "$BASE" =~ ^http://(localhost|127\.0\.0\.1)(:[0-9]+)?/?$ ]]; then
  echo "BASE must be a local HTTP URL (localhost or 127.0.0.1)." >&2
  exit 1
fi
BASE="${BASE%/}"

if ! command -v curl >/dev/null 2>&1; then
  echo "curl is required to seed test data." >&2
  exit 1
fi

read -r -s -p "Test password for all four accounts (12 to 72 characters): " PW
printf '\n'

if [ "${#PW}" -lt 12 ] || [ "${#PW}" -gt 72 ]; then
  echo "The password must be between 12 and 72 characters. Nothing was created." >&2
  exit 1
fi

JAR_A=$(mktemp)
JAR_B=$(mktemp)
trap 'rm -f "$JAR_A" "$JAR_B"' EXIT

token_from() {
  local page
  page=$(curl --noproxy '*' --fail --silent --show-error -b "$1" -c "$1" "$BASE$2")
  grep -o '<input[^>]*name="_csrf"[^>]*>' <<<"$page" \
    | grep -o 'value="[^"]*"' | head -1 | cut -d'"' -f2 || true
}

# post JAR FORM_PATH POST_PATH EXPECTED_REDIRECT field=value ...
post() {
  local jar=$1 form=$2 target=$3 expected=$4
  shift 4

  local token response status redirect
  token=$(token_from "$jar" "$form")
  if [ -z "$token" ]; then
    echo "Could not retrieve a CSRF token from $form." >&2
    return 1
  fi

  local args=()
  for pair in "$@"; do args+=(--data-urlencode "$pair"); done
  response=$(curl --noproxy '*' --silent --show-error -o /dev/null \
    -w '%{http_code}\t%{redirect_url}' -b "$jar" -c "$jar" "${args[@]}" \
    --data-urlencode "_csrf=$token" "$BASE$target")
  status=${response%%$'\t'*}
  redirect=${response#*$'\t'}
  printf '%-14s%s %s\n' "$target" "$status" "$redirect"
  if [[ "$status" != "302" || "$redirect" != "$BASE$expected" ]]; then
    echo "Unexpected response for POST $target; expected 302 $BASE$expected." >&2
    return 1
  fi
}

build_clinic() {
  local jar=$1 clinic=$2 letter=$3
  echo "--- $clinic"
  post "$jar" /register /register /login?registered \
    "organisationName=$clinic" "email=owner-$letter@example.com" "password=$PW"
  post "$jar" /login /login / \
    "username=owner-$letter@example.com" "password=$PW"
  post "$jar" /admin/staff /admin/staff /admin/staff?created \
    "email=staff-$letter@example.com" "password=$PW"

  local upper
  upper=$(echo "$letter" | tr 'a-z' 'A-Z')
  for name in Alpha Beta Gamma Delta; do
    post "$jar" /inventory/new /inventory /inventory?created \
      "name=$upper-$name" "purpose=Test data" "storageLocation=Test cabinet"
  done
}

build_clinic "$JAR_A" "Clinic A" a
build_clinic "$JAR_B" "Clinic B" b
unset PW
echo "Done."
