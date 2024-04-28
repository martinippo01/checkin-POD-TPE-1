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

# Assign counters to Flybondi, list the counters
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=assignCounters -Dsector="$SECTOR" -Dflights="$FLIGHT" -Dairline="$AIRLINE" -DcounterCount="$COUNTERS"
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=listSectors

# Query counter
echo "Querying counters"
bash queryClient.sh -DserverAddress="$ADDRESS" -Daction=queryCounters -DoutPath=./counters.txt


# Remove the manifest
rm "$MANIFEST_PATH"