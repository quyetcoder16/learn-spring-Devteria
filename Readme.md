# Identity service

## Tech stack
* Build tool: maven 3.9.9
* Java: 21
* Framework: Spring boot 3.2.2
* DBMS: MySQL

## Start application
`mvn spring-boot:run`

## Build application
`mvn clean package`

## Docker guideline

### Build image
`docker build -t test-identity:0.0.1 .`

### Create network:
`docker network create quyet-network`

### Check network:
`docker network ls`

### Start MySQL in quyet-network
`docker run --network quyet-network --name mysql_quyet -p 3307:3306 -e MYSQL_ROOT_PASSWORD=root -d mysql:8.0.36-debian`

### Run your application in quyet-network
`docker run --name identity-service --network quyet-network -p 8081:8081 -e DBMS_CONNECTION=jdbc:mysql://mysql_quyet:3306/spring -e DBMS_PASSWORD=root test-identity:0.0.1`

## dockerhub push

### Build docker image
`docker build -t <account>/identity-service:0.9.0 .`

### Push docker image to Docker Hub
`docker image push <account>/identity-service:0.9.0`

### pull image 
`docker pull quyetcoder16/identity-service:0.9.0`

### run docker image 
`docker run --name identity-service --network quyet-network -p 8081:8081 -e DBMS_CONNECTION=jdbc:mysql://mysql_quyet:3306/spring -e DBMS_PASSWORD=root quyetcoder16/identity-service:0.9.0`


