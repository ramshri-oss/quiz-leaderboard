# Quiz Leaderboard System

This project is part of the Bajaj Finserv Health Internship Assignment.

## Problem

The task was to:
- Poll a quiz API 10 times
- Handle duplicate responses
- Aggregate participant scores
- Generate a leaderboard
- Submit the final result

## Approach

- Called the API 10 times with a 5-second delay between each call
- Used a HashSet to remove duplicate entries based on (roundId + participant)
- Used a HashMap to store and update total scores for each participant
- Sorted the leaderboard in descending order of scores
- Submitted the final leaderboard to the API

## Output

Leaderboard:
- George: 795
- Hannah: 750
- Ivan: 745

Total Score: 2290

## Tech Used

- Java 17
- Java HttpClient
- Jackson (for JSON parsing)

## Notes

- Duplicate API responses were handled correctly
- Final total matched expected result
- Multiple runs resulted in idempotent response from server
