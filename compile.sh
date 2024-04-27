#!/usr/bin/env bash
#
#
# Ignore SC2155: Declare and assign separately to avoid masking return values.
# shellcheck disable=SC2155
#

# Enter script directory before sourcing anything

readonly COMPILED_DIR="grpc-com-tpe1-client-2024.1Q"
readonly COMPILED_TAR="grpc-com-tpe1-client-2024.1Q-bin.tar.gz"

readonly SCRIPT_DIR="$(dirname "${BASH_SOURCE[0]}")"
if ! pushd "${SCRIPT_DIR}" &> /dev/null; then
    >&2 echo "Script directory not found (??): '$SCRIPT_DIR'"
    exit 1
fi

if ! mvn clean; then
    >&2 echo "Error while executing Maven clean command. Aborted."
    exit 1
fi

if ! mvn compile -Dmaven.test.skip; then
    >&2 echo "Error while executing Maven compile command. Aborted."
    exit 1
fi

if ! mvn package -Dmaven.test.skip; then
    >&2 echo "Error while executing Maven package command. Aborted."
    exit 1
fi

if ! pushd "client/target" &> /dev/null; then
    >&2 echo "Client target directory not found."
    exit 1
fi

if [ ! -f "$COMPILED_TAR" ]; then
    >&2 echo "tar with compiled binaries not found"
fi

tar xf "$COMPILED_TAR"

echo "Success!"
echo "Client scripts in '$(pwd)/$COMPILED_DIR/'"

# Exit script directory
popd &> /dev/null || exit 0
