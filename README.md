# Poke API

> Poke API is a scheduler and an API which have to main goal to trigger agent using Apache Kafka.

## Environment variables

| Name                      | Description                                                                              | Type      |
| ------------------------- | ---------------------------------------------------------------------------------------- | --------- |
| SCHEDULER_ENABLED         | Enable the scheduler in order to send events in kafka                                    | `Boolean` |
| APP_SECRET                | Application secret                                                                       | `String`  |
| MACAROONS_SECRET          | Macaroon secret                                                                          | `String`  |
| JAAS_PATH                 | Path to the `.jaas` file containing kafka credentials                                    | `String`  |
| POSTGRESQL_ADDON_URI      | URI of the PostgreSQL database                                                           | `URI`     |
| POSTGRESQL_ADDON_PORT     | Port of the PostgreSQL database                                                          | `Number`  |
| POSTGRESQL_ADDON_DB       | Name of the PostgreSQL database                                                          | `String`  |
| POSTGRESQL_ADDON_USER     | User to use with the PostgreSQL database                                                 | `String`  |
| POSTGRESQL_ADDON_PASSWORD | Password of the user to use with the PostgreSQL database                                 | `String`  |
| POSTGRESQL_ADDON_HOST     | Host of the PostgreSQL database                                                          | `String`  |
| WARP_PRODUCER             | The UUID of the producer                                                                 | `UUID`    |
| WARP_SIP_HASH_APP         | Hexadecimal key of the Warp 10 instance                                                  | `Hex`     |
| WARP_SIP_HASH_TOKEN       | Hexadecimal key of the Warp 10 instance                                                  | `Hex`     |
| WARP_AES_TOKEN            | Hexadecimal key of the Warp 10 instance                                                  | `Hex`     |
| WARP_ENDPOINT             | URL of the Warp 10 instance                                                              | `URL`     |
| KAFKA_HOST                | Host of the kafka instance with optionnaly a port                                        | `String`  |
| KAFKA_HTTPCHECKS_TOPIC    | Topic of the HTTP checks                                                                 | `String`  |
| KAFKA_HTTPCHECKS_INTERVAL | Interval to treat message batching                                                       | `String`  |
| KAFKA_SSLCHECKS_TOPIC     | Topic of the SSL checks                                                                  | `String`  |
| KAFKA_SSLCHECKS_INTERVAL  | Interval to treat message batching                                                       | `String`  |
| KAFKA_DNSCHECKS_TOPIC     | Topic of the DNS checks                                                                  | `String`  |
| KAFKA_DNSCHECKS_INTERVAL  | Interval to treat message batching                                                       | `String`  |
| KAFKA_ICMPCHECKS_TOPIC    | Topic of the ICMP checks                                                                 | `String`  |
| KAFKA_ICMPCHECKS_INTERVAL | Interval to treat message batching                                                       | `String`  |
| KAFKA_ENDPOINT            | Kafka endpoint                                                                           | `String`  |
| KAFKA_WRITER_USER         | Kafka user with writing permission                                                       | `String`  |
| KAFKA_WRITER_PASSWORD     | Password of the kafka permission                                                         | `String`  |
| KAFKA_SECURITY_PROTOCOL   | Kafka security protocol which can be `PLAINTEXT`, `SASL_PLAINTEXT`, `SASL_SSL` and `SSL` | `String`  |

## Usage

To use the Poke API you should fill the previous environment variables in order to start the application. This means you should have at least as dependencies:

* An instance of PostgreSQL
* An instance of Apache Kafka
* An instance of Warp 10

Once, all dependencies are up and running, you can compile the Poke API using the following command:

```bash
sbt stage
```

Now, you are ready to run it in production using the following command:

```bash
./target/universal/stage/bin/poke-api
```

## Development

To contribute to the project you will need the same dependencies that for the usage. Furthermore, the project

To start the incremental compilation type the following commands:

```bash
$ sbt

[info] Loading settings from plugins.sbt,scaffold.sbt ...
[info] Loading project definition from poke-api/project
[info] Loading settings from build.sbt ...
[info] Loading project definition from .sbt/1.0/staging/49b31237ef85983ed75d/anorm-pg-entity/project
[info] Loading settings from build.sbt ...
[info] Set current project to poke-api (in build poke-api/)
[info] sbt server started at .sbt/1.0/server/e3a928848655bdf089a9/sock
[poke-api] $ ~compile
...
[success] Total time: 2 s, completed Mar 20, 2018 5:14:47 PM
1. Waiting for source changes... (press enter to interrupt)
```

So, now you are ready to contribute :smile:.
