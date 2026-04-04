#!/usr/bin/env bash

set -euo pipefail

count="${1:-30}"
base_url="${BASE_URL:-http://localhost:8081/random-character.pdf?zeroLevel=true}"
output_dir="${OUTPUT_DIR:-./generated-characters}"

mkdir -p "$output_dir"

cleanup_files() {
  rm -f "${tmp_pdf:-}" "${tmp_txt:-}"
}

sanitize_filename() {
  printf '%s' "$1" \
    | tr -d '\r' \
    | sed -E 's/[\\/:*?"<>|]//g; s/[[:space:]]+/ /g; s/^ +| +$//g'
}

extract_name_from_text() {
  local text_file="$1"

  awk '
    function trim(s) {
      sub(/^[[:space:]]+/, "", s)
      sub(/[[:space:]]+$/, "", s)
      return s
    }
    function collapse(s) {
      gsub(/[[:space:]]+/, " ", s)
      return trim(s)
    }
    function is_title_case_name(s, n, parts, i) {
      if (s !~ /^[A-Za-z'\''-]+( [A-Za-z'\''-]+)+$/) return 0
      n = split(s, parts, " ")
      if (n < 2 || n > 4) return 0
      for (i = 1; i <= n; i++) {
        if (parts[i] !~ /^[A-Z][A-Za-z'\''-]*$/) return 0
      }
      return 1
    }
    {
      line = collapse($0)
      if (line == "") next
      if (line ~ /^ShadowDark( |$)/) next
      if (line ~ /^(NAME|TALENTS \/ SPELLS|ANCESTRY|CLASS|LEVEL|XP|TITLE|GEAR|FREE TO CARRY|ALIGNMENT|ATTACKS|BACKGROUND|DEITY|HP|AC)$/) next
      if (line ~ /^[[:digit:][:punct:] ]+$/) next
      if (is_title_case_name(line)) {
        print line
        exit
      }
    }
  ' "$text_file"
}

unique_destination() {
  local directory="$1"
  local base_name="$2"
  local candidate="$directory/$base_name.pdf"
  local suffix=2

  while [[ -e "$candidate" ]]; do
    candidate="$directory/$base_name (${suffix}).pdf"
    ((suffix++))
  done

  printf '%s' "$candidate"
}

for ((i = 1; i <= count; i++)); do
  tmp_pdf="$(mktemp "${TMPDIR:-/tmp}/shadowdark-character-XXXXXX.pdf")"
  tmp_txt="$(mktemp "${TMPDIR:-/tmp}/shadowdark-character-XXXXXX.txt")"
  trap cleanup_files EXIT

  curl --fail --silent --show-error --output "$tmp_pdf" "$base_url"
  pdftotext -layout "$tmp_pdf" "$tmp_txt"

  raw_name="$(extract_name_from_text "$tmp_txt" || true)"
  safe_name="$(sanitize_filename "$raw_name")"

  if [[ -z "$safe_name" ]]; then
    safe_name="random-character-${i}"
  fi

  destination="$(unique_destination "$output_dir" "$safe_name")"
  mv "$tmp_pdf" "$destination"
  rm -f "$tmp_txt"
  unset tmp_pdf tmp_txt
  trap - EXIT

  printf 'Saved %s\n' "$destination"
done
