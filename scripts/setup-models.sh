#!/usr/bin/env bash
# DynamisAI - pre-cache Llama 3.2 3B Instruct for Jlama

set -euo pipefail

TOKEN="${HF_TOKEN:-}"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --token) TOKEN="$2"; shift 2 ;;
    *) echo "Unknown argument: $1"; exit 1 ;;
  esac
done

if [[ -z "$TOKEN" ]]; then
  echo "ERROR: HuggingFace token required."
  echo "  export HF_TOKEN=hf_your_token_here"
  echo "  or pass --token hf_your_token_here"
  echo ""
  echo "  Get a token at: https://huggingface.co/settings/tokens"
  echo "  Accept the Meta license at: https://huggingface.co/meta-llama/Llama-3.2-3B-Instruct"
  exit 1
fi

echo "==> Pre-caching meta-llama/Llama-3.2-3B-Instruct via Jlama..."
echo "    Cache directory: ~/.jlama/models/"
echo "    Download size:   ~2.5GB (first run only)"
echo ""

cd "$(dirname "$0")/.."

mvn -pl dynamis-ai-demo exec:java \
  -Dexec.mainClass="org.dynamisai.demo.ModelSetup" \
  -Dexec.args="meta-llama/Llama-3.2-3B-Instruct" \
  -DJLAMA_HF_TOKEN="$TOKEN" \
  -q

echo ""
echo "==> Model cached. Run the showcase with:"
echo "    cd dynamis-ai-demo && mvn exec:java -Dexec.args='--gui'"
