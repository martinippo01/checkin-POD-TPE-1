# Testing scripts for clients

This directory contains the following tests (and the associated script)
- **Run all**: Test every command. That the syntax and output are correct
    - `run-all.sh`
- **Basic setup**: 
  - `basicSetUp.sh`
- **Check In Status**: Test the different states of check-in for a passenger
  - `checkInStatus.sh`
- **Notifications 1**: Test that an airline can see notifications for events like, counters assigned and check-in events
For this test, it is necessary to run the following scripts in order:
  - `notificationsTest.sh` 
  - `notificationsTest_parallelActions.sh`
- **Notifications 2**: Test that an airline can se notifications for events like, pending counter assignment, updates on pending status, counters assigned and counters freed. 
For this test, it is necessary to run the following scripts in order: 
    - `notificationsTest.sh`
    - `notificationsTest_parallelActions_multiplePendings.sh`

- **Pendings**: Test that check the order in which airlines are assigned (when pending). Also it test that the pending request are made in order, plus skipping the ones that are not possible.
  - `pendingTest.sh`
