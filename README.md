# Sample Akka Http #

[![Build Status](https://travis-ci.org/fpeyron/sample-akka-http.svg?branch=master)](https://travis-ci.org/fpeyron/sample-akka-http)[![Coverage Status](https://coveralls.io/repos/github/fpeyron/sample-akka-http/badge.svg?branch=master)](https://coveralls.io/github/fpeyron/sample-akka-http?branch=master)

Application demo using Akka, Akka-http, Circe with Swagger description.


## About the Project
#### Technology ####

* root language  : [Scala 2.12](http://scala-lang.org/)
* backbone architecture framework : [Akka Framework](https://doc.akka.io/docs/akka/current/)
* http provider framework [Akka-http](https://doc.akka.io/docs/akka-http/current/)
* http serializer Json : [Circe](https://circe.github.io/circe/)
* http documentation : [swagger-akka-Http](https://github.com/swagger-akka-http/swagger-akka-http) which is built using [swagger.io](http://swagger.io/) front (embedded)

#### Architecture ####

* Http service :
    * Service info   : url to read info about build (/info)
    * Service health : url to check health (deployment needs) /health)
    * Swagger ui     : url of Swagger application (/swagger)
    * Service api    : api of service (/api) 
* Actor :
    * WorkerActor : service List and statistic

#### Limitations ####

* Application could not deploy as Concurrently (multi node) 
  > Statistic state could now be share between node.
* Statistics are lost after restart of the actor 
  > No persistence of statistics are implemented
* No memory limitation
  > Memory could be grown with several type of parameters because statistics. There is no limitation on the number of state locally persisted. 
 
#### To be Improved ####
 
 1. Create a dedicated Actor for statistics to improve performance
 2. Implement **Akka cluster** to be share statistic with other node
     > Actor Statistic should be define as singleton in the cluster 
 3. Implement **Akka persistence** to persist statistic state after restart
     > implementation with external service like *Kafta* could be a good solution 
  
## Getting Started

###### Requirements :
* Java [OpenJDK](https://openjdk.java.net/) (version >= 8)
* [Sbt](http://www.scala-sbt.org/) (version >= 1.0.0)

> **NOTE** This project is compatible Java 11 **without regression**.


#### Run locally
```bash
sbt run
```
Application is running by default on the port `8080`. It should change in resource file (`application.conf`)

#### Build as Fat Jar and run
```bash
sbt assembly
java -jar ./target/scala-2.12/sample-akka-http-assembly-1.0-SNAPSHOT.jar
```
Application is running on the port `8080` by default. It should change with Jvm parameter :
```bash
java -Dapi.http.port=8083 -jar ./target/scala-2.12/sample-akka-http-assembly-1.0-SNAPSHOT.jar
```
 


#### Testing and coverage

```bash
sbt clean coverage test coverageReport
```
The complete coverage report is generated as http page : `./target/scala-2.12/scoverage-report/index.html`.   

> **NOTE** To display the test coverage next to build results in GitLab you have to add the following regex in your project's settings (under **CI/CD Pipelines** > **Test coverage parsing**): `Coverage was \[\d+.\d+\%\]`.


#### Check style

```bash
sbt scalastyle
```
Results of checks appear in console.   

#### Docker publishing


Build dockerfile and prepare build directory :
```bash
sbt docker:stage
```

Build docker image and publish locally :
```bash
sbt docker:publishLocal
```

Build docker image and publish :
```bash
sbt docker:publish
```

Run instance of docker image locally :
````bash
 docker run --name sample-akka-http -p 8082:8080 sample-akka-http
````
(with docker port forward 8082)

> **NOTE** for run publishing and tags, `Docker` environment should be started locally and configured for publishing.



#### Test Integration (IT) ####

> **TODO**

First open sbt command 
```bash
sbt
```
List all tasks
```sbtshell
tasks gatling -v
```
Run all simulations
```sbtshell
gatling:test
```



## License ##

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
