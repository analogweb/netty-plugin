Analogweb Framework Netty Plugin
===============================================

Analogweb application running on Netty 4!

## Quick Start

```java
package org.analogweb.hello;

import org.analogweb.annotation.Route;
import org.analogweb.netty.HttpServers;

@Route("/")
public class Hello {

  public static void main(String... args) {
      HttpServers.create("http://localhost:8080").run();
  }

  @Route
  public String hello() {
    // Request GET http://localhost:8080/hello and you should see 'Hello World'
    return "Hello!";
  }

}
```
