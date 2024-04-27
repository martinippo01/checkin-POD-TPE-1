#!/usr/bin/env bash
#
#
# Ignore SC2155: Declare and assign separately to avoid masking return values.
# shellcheck disable=SC2155
#

# Enter script directory before sourcing anything

readonly SCRIPT_DIR="$(dirname "${BASH_SOURCE[0]}")"
if ! pushd "${SCRIPT_DIR}" &> /dev/null; then
        >&2 echo "Script directory not found (??): '$SCRIPT_DIR'"
        exit 1
fi

readonly PATH_TO_CODE_BASE="$(pwd)"
readonly JAVA_OPTS="-Djava.rmi.server.codebase=file://$PATH_TO_CODE_BASE/lib/jars/grpc-com-tpe1-client-2024.1Q.jar"
readonly MAIN_CLASS="ar.edu.itba.pod.tpe1.client.query.CounterQueryClient"

java "$JAVA_OPTS" -cp 'lib/jars/*' "$MAIN_CLASS" "$@"

# Exit script directory
popd &> /dev/null || exit 0