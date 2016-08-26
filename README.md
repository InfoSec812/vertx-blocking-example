# Demonstration of an issue using [Vert.x Web](https://vertx.io/) blockingHandler with multipart form processing

## Compile
```
./gradlew clean build fatJar
```

## Run
```
java -jar ./build/libs/example-all-1.0-SNAPSHOT.jar
```

## Test
```
export CURL_OPTS='-v -X POST -d "email=john.doe%40comecompany.com" -H "Content-Type: application/x-www-form-urlencoded"'

## This causes event loop to block
$ curl $CURL_OPTS http://localhost:1080/blockingB
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 1080 (#0)
> GET /blocking HTTP/1.1
> Host: localhost:1080
> User-Agent: curl/7.47.0
> Accept: */*
> 
< HTTP/1.1 500 Internal Server Error
< Content-Length: 21
< 
* Connection #0 to host localhost left intact

## This throws IllegalStateException (intermittently)
$ curl $CURL_OPTS http://localhost:1080/blockingA
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 1080 (#0)
> GET /blocking HTTP/1.1
> Host: localhost:1080
> User-Agent: curl/7.47.0
> Accept: */*
> 
< HTTP/1.1 500 Internal Server Error
< Content-Length: 21
< 
* Connection #0 to host localhost left intact


## This does not cause any problems
$ curl $CURL_OPTS http://localhost:1080/nonblocking
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 1080 (#0)
> GET /nonblocking HTTP/1.1
> Host: localhost:1080
> User-Agent: curl/7.47.0
> Accept: */*
> 
< HTTP/1.1 200 OK
< Content-Type: application/json
< Content-Length: 47
< 
{
  "status" : "OK",
  "type" : "nonblocking"
* Connection #0 to host localhost left intact
```

And the console output from Vert.x:

```
Aug 25, 2016 3:38:27 PM io.vertx.ext.web.impl.RoutingContextImplBase
SEVERE: Unexpected exception in route
java.lang.IllegalStateException: Request has already been read
        at io.vertx.core.http.impl.HttpServerRequestImpl.checkEnded(HttpServerRequestImpl.java:426)
        at io.vertx.core.http.impl.HttpServerRequestImpl.endHandler(HttpServerRequestImpl.java:239)
        at io.vertx.ext.web.impl.HttpServerRequestWrapper.endHandler(HttpServerRequestWrapper.java:53)
        at us.juggl.vertx.blocking.ExampleVerticle.blockingHandler(ExampleVerticle.java:62)
        at io.vertx.ext.web.impl.BlockingHandlerDecorator.lambda$handle$0(BlockingHandlerDecorator.java:48)
        at io.vertx.core.impl.ContextImpl.lambda$executeBlocking$2(ContextImpl.java:303)
        at io.vertx.core.impl.OrderedExecutorFactory$OrderedExecutor.lambda$new$0(OrderedExecutorFactory.java:94)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
        at java.lang.Thread.run(Thread.java:745)
```