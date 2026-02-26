#!/usr/bin/env bash
# DynamisAI — download all-MiniLM-L6-v2 ONNX encoder
#
# Usage:
#   ./scripts/download-encoder.sh
#
# Downloads to: ~/.dynamisai/encoders/all-MiniLM-L6-v2/

set -euo pipefail

BASE_URL="https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main"
CACHE_DIR="${HOME}/.dynamisai/encoders/all-MiniLM-L6-v2"

mkdir -p "$CACHE_DIR"

FILES=(
  "onnx/model.onnx"
  "tokenizer.json"
  "tokenizer_config.json"
  "vocab.txt"
  "special_tokens_map.json"
)

echo "==> Downloading all-MiniLM-L6-v2 to ${CACHE_DIR}"
echo "    Licence: Apache 2.0 — no token required"
echo ""

for FILE in "${FILES[@]}"; do
  DEST="${CACHE_DIR}/${FILE##*/}"
  if [[ -f "$DEST" ]]; then
    echo "    [skip] ${FILE##*/} already cached"
    continue
  fi
  echo "    [get]  ${FILE}"
  curl -fsSL --create-dirs \
    -H "Accept: application/octet-stream" \
    "${BASE_URL}/${FILE}" \
    -o "$DEST"
done

echo ""
echo "==> Encoder cached at: ${CACHE_DIR}"
echo "    Run the showcase with live semantic memory:"
echo "    cd dynamis-ai-demo && mvn exec:java -Dexec.args='--gui'"
