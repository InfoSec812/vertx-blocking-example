# Demonstration of how to successfully use [Vert.x Web](https://vertx.io/) blockingHandler with multipart form processing


## The main point
When using multipart form data (either x-www-form-urlencoded or multipart/form-data),
you need to call `setExpectMultipart(true)` BEFORE the call to blockingHandler. 
This means that you need a route handler BEFORE blockingHandler which makes the call
to `setExpectMultipart(true)` and which subsequently calls `RoutingContext.next()`
as shown below:

```
	Router router = Router.router(vertx);
	router.post("/my/path").handler(ctx -> {
		ctx.request().setExpectMultipart(true);
		ctx.request().exceptionHandler(this::execptionHandler);
		ctx.response().exceptionHandler(this::execptionHandler);
		ctx.next();
	});
	router.post("/my/path").blockingHandler(this::doSomething);
```


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
$ curl $CURL_OPTS http://localhost:1080/blockingA
$ curl $CURL_OPTS http://localhost:1080/nonblocking
```