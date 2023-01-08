### Simple Spring Boot Application

To run the tests.

```
mvn clean package
```

To run app by command.

```
SERVER_PORT=9080 mvn spring-boot:run
```

To generate a docker image.

```
mvn clean package spring-boot:build-image
```

To run the docker image

```
docker run -it -p 9080:9080 -e SERVER_PORT=9080 demo-service:0.0.1-SNAPSHOT
```

To create a user

```
curl -v -H "Content-type: application/json" -X POST localhost:9080/v1/api/users -d '{"name": "test", "buildings": [{"name":"Building 1", "location": "Dublin", "elevators":[{"name": "Elevator 1", "status": "STOPPED", "floors":[1, 2, 3]}]}]}'
```

To find a user

```
curl -v localhost:9080/v1/api/users/1
```

To find a building that belongs for a user

```
curl -v localhost:9080/v1/api/users/1/buildings/1
```