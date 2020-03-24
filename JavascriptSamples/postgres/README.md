# Postgres client sample usage
## Setup
`docker run --name pg -p 5432:5432 -d postgres:9.6`
## Start
```shell script
node src/singleclient.js
# or
node src/connectionpool.js
```