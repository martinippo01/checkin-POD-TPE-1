!/bin/bash

BASE_PATH="../client/target/grpc-com-tpe1-client-2024.1Q"
ADDRESS='localhost:50058'
SECTOR='A'
COUNTERS_ROUND_1='5'
COUNTERS_ROUND_2='5'
COUNTERS_ROUND_3='10'


CASE1_AIRLINE='FlyBondi'
CASE1_FLIGHTS='FO523'
CASE1_COUNTERS='4'

CASE2_AIRLINE='AirCanada'
CASE2_FLIGHTS='AC987|AC988'
CASE2_COUNTERS='7'

CASE3_AIRLINE='AmericanAirlines'
CASE3_FLIGHTS='AA123'
CASE3_COUNTERS='3'

MANIFEST_PATH="./manifest.csv"

# Copy the manifest to the scripts directory
cp "$MANIFEST_PATH" "$BASE_PATH"

if ! pushd "$BASE_PATH"; then
        echo "Base path not found"
        exit 1
fi
# Create sector A
bash adminClient.sh -DserverAddress="$ADDRESS" -Daction=addSector -Dsector="$SECTOR"
# Bookings, flights and airlines
bash adminClient.sh -DserverAddress="$ADDRESS" -Daction=manifest -DinPath="$MANIFEST_PATH"

# Add 5 counters to sector A
bash adminClient.sh -DserverAddress="$ADDRESS" -Daction=addCounters -Dsector="$SECTOR" -Dcounters="$COUNTERS_ROUND_1"

echo "-----------Assign first case: FlyBonfi"
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=assignCounters -Dsector="$SECTOR" -Dflights="$CASE1_FLIGHTS" -Dairline="$CASE1_AIRLINE" -DcounterCount="$CASE1_COUNTERS"
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=assignCounters -Dsector="$SECTOR" -Dflights="$CASE2_FLIGHTS" -Dairline="$CASE2_AIRLINE" -DcounterCount="$CASE2_COUNTERS"
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=assignCounters -Dsector="$SECTOR" -Dflights="$CASE3_FLIGHTS" -Dairline="$CASE3_AIRLINE" -DcounterCount="$CASE3_COUNTERS"

echo "-----------FlyBodi should be assigned, AirCanada and AmericanAirlines should be pending:"
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=listCounters -Dsector="$SECTOR" -DcounterFrom=1 -DcounterTo=30
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=listPendingAssignments -Dsector="$SECTOR"


# Add 5 counters to sector A
bash adminClient.sh -DserverAddress="$ADDRESS" -Daction=addCounters -Dsector="$SECTOR" -Dcounters="$COUNTERS_ROUND_2"

echo "-----------FlyBodi and AmericanAirlines should be assigned, AirCanada should be pending:"
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=listCounters -Dsector="$SECTOR" -DcounterFrom=1 -DcounterTo=30
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=listPendingAssignments -Dsector="$SECTOR"


# Add 10 counters to sector A
bash adminClient.sh -DserverAddress="$ADDRESS" -Daction=addCounters -Dsector="$SECTOR" -Dcounters="$COUNTERS_ROUND_3"

echo "-----------Should be all assigned and no pending:"
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=listCounters -Dsector="$SECTOR" -DcounterFrom=1 -DcounterTo=30
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=listPendingAssignments -Dsector="$SECTOR"

# Remove the manifest that ive copied to the scripts directory
rm "$MANIFEST_PATH"

