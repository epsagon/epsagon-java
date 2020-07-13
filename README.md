<p align="center">
  <a href="https://epsagon.com" target="_blank" align="center">
    <img src="https://cdn2.hubspot.net/hubfs/4636301/Positive%20RGB_Logo%20Horizontal%20-01.svg" width="300">
  </a>
  <br />
</p>

[![Maven Central](https://img.shields.io/maven-central/v/com.epsagon/epsagon.svg)](https://img.shields.io/maven-central/v/com.epsagon/epsagon.svg)
[![Build Status](https://travis-ci.com/epsagon/serverless-plugin-epsagon.svg?branch=master)](https://travis-ci.com/epsagon/serverless-plugin-epsagon)
[![semantic-release](https://img.shields.io/badge/%20%20%F0%9F%93%A6%F0%9F%9A%80-semantic--release-e10079.svg)](https://github.com/semantic-release/semantic-release)

# Epsagon Instrumentation for Java

This package provides tracing to Java applications for the collection of distributed tracing and performance metrics in [Epsagon](https://app.epsagon.com/?utm_source=github).

# Where To Get Packages
* For [Maven](https://maven.apache.org) projects, use:
```xml
<dependency>
  <groupId>com.epsagon</groupId>
  <artifactId>epsagon</artifactId>
  <version>{Epsagon version}</version>
</dependency>
``` 
The version will be in the format `n.n.n`, latest maven-central version is specified at the top as a
badge.

# Getting Started
## Quick Start
The easiest way to get started is as following:
* set the entry point to your Lambdas as `com.epsagon.EpsagonRequestHandler`
* set the following environment variables:
    * `EPSAGON_ENTRY_POINT` - The real entry point to your code, the one you had
                              previously configured (should be something like
                              `com.yourcompany.YourHandler::handlerMethod`)
    * `EPSAGON_TOKEN` - Epsagon's token, can be found at the 
                        [Dashboard](https://app.epsagon.com/settings)
    * `EPSAGON_APP_NAME` - A name for the application of this function, optional.

And that's it! Your function is ready for invocation.

## Alternative (No Environment Variables)
If you do not want to configure environment variables, please use this alternative:

First, create a simple class that extends `com.epsagon.EpsagonRequestHandler` like so:
```java
import com.epsagon.EpsagonRequestHandler;

public class EpsagonWrapper extends EpsagonRequestHandler {
    static {
        try {
            init("com.yourcompany.YourHandler::yourHandlerMethod")
                    .setToken("<your token>")
                    .setAppName("<your application name>");
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

Finally, set this class as the entry point of your Lambda (instead of your original handler). This
class will automatically load your original handler and execute it.

# Copyright
Provided under the MIT license. See LICENSE for details.

Copyright 2020, Epsagon
