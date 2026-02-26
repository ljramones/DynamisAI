#!/bin/zsh
ROOT=$(pwd)

build_repo() {
    echo "=== Building $1 ==="
    cd "$ROOT/$1" && mvn clean install -DskipTests
    if [ $? -ne 0 ]; then
        echo "FAILED: $1"
        exit 1
    fi
    echo "PASSED: $1"
}

build_repo dynamis-ai-core
build_repo dynamis-ai-lod
build_repo dynamis-ai-perception
build_repo dynamis-ai-cognition
build_repo dynamis-ai-navigation
build_repo dynamis-ai-memory
build_repo dynamis-ai-voice
build_repo dynamis-ai-planning
build_repo dynamis-ai-social
build_repo dynamis-ai-crowd
build_repo dynamis-ai-tools

echo "=== ALL 11 REPOS BUILT SUCCESSFULLY ==="
