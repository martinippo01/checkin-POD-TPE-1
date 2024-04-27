#!/bin/bash
#
#
# Ignore SC2155: Declare and assign separately to avoid masking return values.
# shellcheck disable=SC2155
#

# Enter script directory before sourcing anything

CLIENT_COMPILED_DIR="grpc-com-tpe1-client-2024.1Q"
CLIENT_COMPILED_TAR="grpc-com-tpe1-client-2024.1Q-bin.tar.gz"

SERVER_COMPILED_DIR="grpc-com-tpe1-server-2024.1Q"
SERVER_COMPILED_TAR="grpc-com-tpe1-server-2024.1Q-bin.tar.gz"

SCRIPT_DIR="$(dirname "${BASH_SOURCE[0]}")"
if ! pushd "${SCRIPT_DIR}" &> /dev/null; then
    >&2 echo "Script directory not found (??): '$SCRIPT_DIR'"
    exit 1
fi

compile() {
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
}

extract_clients() {
    if ! pushd "client/target" &> /dev/null; then
        >&2 echo "Client target directory not found."
        exit 1
    fi

    if [ ! -f "$CLIENT_COMPILED_TAR" ]; then
        >&2 echo "tar with compiled binaries not found"
    fi

    tar xf "$CLIENT_COMPILED_TAR"

    echo "Client scripts in '$(pwd)/$CLIENT_COMPILED_DIR/'"

    popd &> /dev/null || return 1
}

extract_server() {
    if ! pushd "server/target" &> /dev/null; then
        >&2 echo "Server target directory not found."
        exit 1
    fi

    if [ ! -f "$SERVER_COMPILED_TAR" ]; then
        >&2 echo "tar with compiled binaries not found"
    fi

    tar xf "$SERVER_COMPILED_TAR"

    echo "Server scripts in '$(pwd)/$SERVER_COMPILED_DIR/'"
    echo "Run the server calling 'bash $(pwd)/$SERVER_COMPILED_DIR/run-server.sh'"

    popd &> /dev/null || return 1
}

compile
extract_clients
extract_server

echo "Success!"

# Exit script directory
popd &> /dev/null || exit 0
