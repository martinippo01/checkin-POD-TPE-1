#!/bin/bash
#
#
# Ignore SC2155: Declare and assign separately to avoid masking return values.
# shellcheck disable=SC2155
#

# Enter script directory before sourcing anything
SERVER_COMPILED_DIR="grpc-com-tpe1-server-2024.1Q"
SERVER_PATH="$(pwd)/server/target/$SERVER_COMPILED_DIR"

SCRIPT_DIR="$(dirname "${BASH_SOURCE[0]}")"
if ! pushd "${SCRIPT_DIR}" &> /dev/null; then
    >&2 echo "Script directory not found (??): '$SCRIPT_DIR'"
    exit 1
fi

if [ ! -d "$SERVER_PATH" ]; then
    >&2 echo "Server script not found in '$SERVER_PATH'"
    >&2 echo "Have you run compile script?"
    exit 1
fi


bash "$SERVER_PATH/run-server.sh"
