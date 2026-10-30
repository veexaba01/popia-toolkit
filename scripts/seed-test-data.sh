#!/usr/bin/env bash
# Creates two test organisations, each with an admin, a staff member and four inventory items,
# on a LOCAL instance of the app. For testing only.
set -uo pipefail

BASE="${BASE:-http://localhost:8081}"
read -s -p "Test password for all four accounts (12 to 72 characters): " PW; echo

if [ "${#PW}" -lt 12 ] || [ "${#PW}" -gt 72 ]; then
  echo "The password must be between 12 and 72 characters. Nothing was created."
  exit 1
fi

JAR_A=$(mktemp); JAR_B=$(mktemp)
trap 'rm -f "$JAR_A" "$JAR_B"' EXIT

token_from() {
  curl -s -b "$1" -c "$1" "$BASE$2" | grep -o '<input[^>]*name="_csrf"[^>]*>' \
    | grep -o 'value="[^"]*"' | head -1 | cut -d'"' -f2 || true
}

# post JAR FORM_PATH POST_PATH field=value ...
post() {
  local jar=$1 form=$2 target=$3; shift 3
  local token; token=$(token_from "$jar" "$form")
  local args=()
  for pair in "$@"; do args+=(--data-urlencode "$pair"); done
  printf '%-14s' "$target"
  curl -s -o /dev/null -w "%{http_code} %{redirect_url}\n" -b "$jar" -c "$jar" "${args[@]}" \
    --data-urlencode "_csrf=$token" "$BASE$target"
}

build_clinic() {
  local jar=$1 clinic=$2 letter=$3
  echo "--- $clinic"
  post "$jar" /register /register "organisationName=$clinic" "email=owner-$letter@example.com" "password=$PW"
  post "$jar" /login /login "username=owner-$letter@example.com" "password=$PW"
  post "$jar" /admin/staff /admin/staff "email=staff-$letter@example.com" "password=$PW"
  local upper; upper=$(echo "$letter" | tr 'a-z' 'A-Z')
  for name in Alpha Beta Gamma Delta; do
    post "$jar" /inventory/new /inventory "name=$upper-$name" "purpose=Test data" "storageLocation=Test cabinet"
  done
}

build_clinic "$JAR_A" "Clinic A" a
build_clinic "$JAR_B" "Clinic B" b
unset PW
echo "Done."
