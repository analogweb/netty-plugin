Analogweb Framework Netty Plugin
===============================================

Analogweb is tiny HTTP oriented framework.

## Example

```java
package org.analogweb.hello;

import java.net.URI;
import org.analogweb.annotation.Route;
import org.analogweb.netty.HttpServer;

@Route("/")
public class Hello {

  public static void main(String... args) {
      HttpServer.run("http://localhost:8080");
  }

  @Route
  public String hello() {
    return "Hello World";
  }

}
```
