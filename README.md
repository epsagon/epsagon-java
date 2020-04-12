<p align="center">
  <a href="https://epsagon.com" target="_blank" align="center">
    <img src="https://cdn2.hubspot.net/hubfs/4636301/Positive%20RGB_Logo%20Horizontal%20-01.svg" width="300">
  </a>
  <br />
</p>

[![Maven Central](https://img.shields.io/maven-central/v/com.epsagon/epsagon.svg)](https://img.shields.io/maven-central/v/com.epsagon/epsagon.svg)
[![Build Status](https://travis-ci.com/epsagon/serverless-plugin-epsagon.svg?branch=master)](https://travis-ci.com/epsagon/serverless-plugin-epsagon)
[![semantic-release](https://img.shields.io/badge/%20%20%F0%9F%93%A6%F0%9F%9A%80-semantic--release-e10079.svg)](https://github.com/semantic-release/semantic-release)

# Epsagon Tracing for Java

This package provides tracing to Java applications for the collection of distributed tracing and performance metrics in [Epsagon](https://dashboard.epsagon.com/?utm_source=github).


## Contents

- [Installation](#installation)
- [Usage](#usage)
  - [Auto-tracing](#auto-tracing)
  - [Ignore Endpoints](#ignore-endpoints)
- [Frameworks](#frameworks)
- [Integrations](#integrations)
- [Configuration](#configuration)
- [Getting Help](#getting-help)
- [Opening Issues](#opening-issues)
- [License](#license)


## Installation

To install Epsagon on AWS Lambda function:
```xml
<dependency>
  <groupId>com.epsagon</groupId>
  <artifactId>epsagon</artifactId>
  <version>{Epsagon version}</version>
</dependency>
```
The version will be in the format `n.n.n`, latest maven-central version is specified at the top as a badge.

For any other environment, download the tracer using `curl`:
```sh
curl -o epsagon-opentracing-agent-1.0.19.jar https://epsagon-java-opentracing-agent.s3.amazonaws.com/epsagon-opentracing-agent-1.0.19.jar
```

## Usage

### Auto-tracing

The simplest way to get started is to run your Java command with the following environment variable:
```sh
export EPSAGON_TOKEN=<epsagon-token>
export EPSAGON_SERVICE_NAME=<app-name-stage>
java -javaagent:.<path_to_epsagon_agent> -jar <your jar file>
```

For example:
```sh
export EPSAGON_TOKEN=<your-token>
export EPSAGON_SERVICE_NAME=spring-prod
java -javaagent:./epsagon-opentracing-agent-1.0.19.jar -jar app.jar
```

You can see the list of auto-tracing [supported frameworks](#frameworks)

### Ignore Endpoints

You can ignore certain incoming requests by specifying endpoints:
```java
export EPSAGON_IGNORED_ENDPOINTS="[\"/healthcheck\", \"/health\"]"
```

## Frameworks

The following frameworks are supported by Epsagon:

|Framework                               |Supported Version          |Auto-tracing Supported                               |
|----------------------------------------|---------------------------|-----------------------------------------------------|
|[AWS Lambda](#aws-lambda)               |All                        |<ul><li>- [ ] </li></ul> |
|[Spring Boot](#spring-boot)             |All                        |<ul><li>- [x] </li></ul>                             |
|[Kafka Clients](#kafka-clients)         |`>=0.11.0.0`               |<ul><li>- [x] </li></ul>                             |
|[Tomcat](#tomcat)                       |`>=7.0.0`                  |<ul><li>- [x] </li></ul>                             |
|[Generic](#generic)                     |All                        |<ul><li>- [x] </li></ul>                             |


### AWS Lambda

The easiest way to get started is as following:
* Set the entry point to your Lambdas as `com.epsagon.EpsagonRequestHandler`
* Set the following environment variables:
    * `EPSAGON_ENTRY_POINT` - The real entry point to your code, the one you had
                              previously configured (should be something like
                              `com.yourcompany.YourHandler::handlerMethod`)
    * `EPSAGON_TOKEN` - Epsagon's token, can be found at the 
                        [Dashboard](https://dashboard.epsagon.com/settings)
    * `EPSAGON_APP_NAME` - A name for the application of this function, optional.

And that's it! Your function is ready for invocation.

If you do not with to configure environment variables, please use this alternative:

First, create a simple class that extends `com.epsagon.EpsagonRequestHandler` like so:
```java
import com.epsagon.EpsagonRequestHandler;

public class EpsagonWrapper extends EpsagonRequestHandler {
    static {
        try {
            init("com.yourcompany.YourHandler::yourHandlerMethod")
                    .setToken("<epsagon=token>")
                    .setAppName("<app-name-stage>");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
```
The class should have a static initializer that calls the `EpsagonRequestHandler.init()` method,
and gives it your Lambda's actual entry point as a parameter. The return value of this method
is an `EpsagonConfiguration` object. Configure your token and application name using this object,
like the example shows.

### Spring Boot

Tracing can be done using the auto-tracing:

```sh
export EPSAGON_TOKEN=<epsagon-token>
export EPSAGON_SERVICE_NAME=<app-name-stage>
java -javaagent:.<path_to_epsagon_agent> -jar <your jar file>
```

For example:
```sh
export EPSAGON_TOKEN=<your-token>
export EPSAGON_SERVICE_NAME=spring-prod
java -javaagent:./epsagon-opentracing-agent-1.0.19.jar -jar app.jar

### Tomcat

Tracing can be done using the auto-tracing:

```sh
export EPSAGON_TOKEN=<epsagon-token>
export EPSAGON_SERVICE_NAME=<app-name-stage>
java -javaagent:.<path_to_epsagon_agent> -jar <your jar file>
```

For example:
```sh
export EPSAGON_TOKEN=<your-token>
export EPSAGON_SERVICE_NAME=tomcat-prod
java -javaagent:./epsagon-opentracing-agent-1.0.19.jar -jar app.jar

### Kafka Clients

Tracing can be done using the auto-tracing:

```sh
export EPSAGON_TOKEN=<epsagon-token>
export EPSAGON_SERVICE_NAME=<app-name-stage>
java -javaagent:.<path_to_epsagon_agent> -jar <your jar file>
```

For example:
```sh
export EPSAGON_TOKEN=<your-token>
export EPSAGON_SERVICE_NAME=consumer-prod
java -javaagent:./epsagon-opentracing-agent-1.0.19.jar -jar app.jar

### Generic

Tracing can be done using the auto-tracing:

```sh
export EPSAGON_TOKEN=<epsagon-token>
export EPSAGON_SERVICE_NAME=<app-name-stage>
java -javaagent:.<path_to_epsagon_agent> -jar <your jar file>
```

For example:
```sh
export EPSAGON_TOKEN=<your-token>
export EPSAGON_SERVICE_NAME=app-prod
java -javaagent:./epsagon-opentracing-agent-1.0.19.jar -jar app.jar


## Integrations

Epsagon provides out-of-the-box instrumentation (tracing) for many popular frameworks and libraries.

|Library             |Supported Version          |
|--------------------|---------------------------|
|kafka-clients       |`>=0.11.0.0`               |
|aws-java-sdk        |`>=1.11.0`                 |
|software.amazon.awssdk/sdk-core|`>=2.2.0`    |
|Apache HttpClient         |`>=4.0`            |
|Apache HttpAsyncClient         |`>=4.0`            |



## Configuration

Advanced options can be configured as a parameter to the init() method or as environment variables.

|Parameter             |Environment Variable          |Type   |Default      |Description                                                                        |
|----------------------|------------------------------|-------|-------------|-----------------------------------------------------------------------------------|
|                      |EPSAGON_TOKEN                 |String |-            |Epsagon account token                                                              |
|                      |EPSAGON_SERVICE_NAME          |String |`Epsagon Application`|Application name that will be set for traces                                       |
|                      |EPSAGON_COLLECTOR_ENDPOINT    |String |-            |The address of the trace collector to send trace to                                |
|                      |EPSAGON_IGNORED_ENDPOINTS     |Array  |-            |List of endpoints to ignore from tracing (for example `/healthcheck`)              |
|-                     |EPSAGON_REPORTER_LOG_SPANS    |Boolean|`True`       |A flag to disable Epsagon trace sending (if set to `False`            |

To Enable debug prints for troubleshooting add `"-Depsagon.slf4j.simpleLogger.defaultLogLevel=info"` to the `java` command

## Getting Help

If you have any issue around using the library or the product, please don't hesitate to:

* Use the [documentation](https://docs.epsagon.com).
* Use the help widget inside the product.
* Open an issue in GitHub.


## Opening Issues

If you encounter a bug with the Epsagon library for Java, we want to hear about it.

When opening a new issue, please provide as much information about the environment:
* Library version, Java runtime version, dependencies, etc.
* Snippet of the usage.
* A reproducible example can really help.

The GitHub issues are intended for bug reports and feature requests.
For help and questions about Epsagon, use the help widget inside the product.

## License

Provided under the MIT license. See LICENSE for details.

Copyright 2020, Epsagon
