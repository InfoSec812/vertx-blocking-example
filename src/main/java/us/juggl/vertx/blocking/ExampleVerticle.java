package us.juggl.vertx.blocking;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import static io.netty.handler.codec.http.HttpResponseStatus.*;

/**
 * Created by dphillips on 8/25/16.
 */
public class ExampleVerticle extends AbstractVerticle {

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String JSON = "application/json";
    public static final String STATUS = "status";
    private JsonObject status = new JsonObject();

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);
        router.route("/nonblocking*").handler(this::nonBlockingHandler);
        router.route("/blocking*").blockingHandler(this::blockingHandler);

        vertx.createHttpServer().requestHandler(router::accept).listen(1080);
    }

    void nonBlockingHandler(RoutingContext ctx) {
        ctx.request().endHandler(v -> {
            vertx.executeBlocking(future -> {
                try {
                    aBlockingMethod();
                    future.complete();
                } catch (InterruptedException ie) {
                    future.fail(ie);
                }
            }, result -> {
                if (result.succeeded()) {
                    ctx.request().response().putHeader(CONTENT_TYPE, JSON)
                        .setStatusMessage(OK.reasonPhrase())
                        .setStatusCode(OK.code())
                        .end(status
                                .put(STATUS, OK.reasonPhrase())
                                .put("type", "nonblocking")
                                .encodePrettily());
                } else {
                    ctx.request().response()
                        .putHeader(CONTENT_TYPE, JSON)
                        .setStatusCode(INTERNAL_SERVER_ERROR.code())
                        .setStatusMessage(INTERNAL_SERVER_ERROR.reasonPhrase())
                        .end(status
                                .put(STATUS, INTERNAL_SERVER_ERROR.reasonPhrase())
                                .put("type", "nonblocking")
                                .encodePrettily());
                }
            });
        }).setExpectMultipart(true);
    }

    void blockingHandler(RoutingContext ctx) {
        ctx.request().endHandler(v -> {
            try {
                aBlockingMethod();
                ctx.request().response().putHeader(CONTENT_TYPE, JSON)
                    .setStatusMessage(OK.reasonPhrase())
                    .setStatusCode(OK.code())
                    .end(status
                            .put(STATUS, OK.reasonPhrase())
                            .put("type", "blocking")
                            .encodePrettily());
            } catch (InterruptedException ie) {
                ctx.request().response()
                    .putHeader(CONTENT_TYPE, JSON)
                    .setStatusCode(INTERNAL_SERVER_ERROR.code())
                    .setStatusMessage(INTERNAL_SERVER_ERROR.reasonPhrase())
                    .end(status
                            .put(STATUS, INTERNAL_SERVER_ERROR.reasonPhrase())
                            .put("type", "blocking")
                            .encodePrettily());
            }
        }).setExpectMultipart(true);
    }

    void aBlockingMethod() throws InterruptedException {
        Thread.sleep(5000);
    }
}
