#!/bin/bash

BASE_PATH="../client/target/grpc-com-tpe1-client-2024.1Q"
ADDRESS='localhost:50058'
SECTOR='A'
COUNTERS='10'
COUNTER_FROM='1'
AIRLINE='FlyBondi'
BOOKING='FLY213'
FLIGHT='FO523'

MANIFEST_PATH="./manifest.csv"

# Take the manifest to the scripts directory
cp "$MANIFEST_PATH" "$BASE_PATH"

if ! pushd "$BASE_PATH"; then
        echo "Base path not found"
        exit 1
fi

# Add sector, counters and bookings
bash adminClient.sh -DserverAddress="$ADDRESS" -Daction=addSector -Dsector="$SECTOR"
bash adminClient.sh -DserverAddress="$ADDRESS" -Daction=addCounters -Dsector="$SECTOR" -Dcounters="$COUNTERS"
bash adminClient.sh -DserverAddress="$ADDRESS" -Daction=manifest -DinPath="$MANIFEST_PATH"

# Register to recieve notifications and wait
echo "-------------------Register and wait to recieve notifications: "
bash eventsClient.sh -DserverAddress="$ADDRESS" -Daction=register -Dairline="$AIRLINE"

# Remove the manifest
rm "$MANIFEST_PATH"

