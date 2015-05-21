Analogweb Framework Netty Plugin
===============================================

Analogweb application running on Netty 4!
Add this plugin to your application classpath them org.analogweb.core.Servers#run enable to boot Netty server.

[![Build Status](https://travis-ci.org/analogweb/netty-plugin.svg)](https://travis-ci.org/analogweb/netty-plugin)

## Quick Start

```java
package org.analogweb.hello;

import org.analogweb.annotation.Route;
import org.analogweb.core.Servers;

@Route("/")
public class Hello {

  public static void main(String... args) {
      Servers.run();
  }

  @Route
  public String hello() {
    // Request GET http://localhost:8080/hello and you should see 'Hello World'
    return "Hello World";
  }

}
```
