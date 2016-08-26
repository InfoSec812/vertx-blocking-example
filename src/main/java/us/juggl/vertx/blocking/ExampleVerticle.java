package us.juggl.vertx.blocking;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.java.Log;

import java.util.logging.Level;

import static io.netty.handler.codec.http.HttpResponseStatus.*;

/**
 * Created by dphillips on 8/25/16.
 */
@Log
public class ExampleVerticle extends AbstractVerticle {

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String JSON = "application/json";
    public static final String STATUS = "status";
    private JsonObject status = new JsonObject();

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);
        router.post("/nonblocking*").handler(this::nonBlockingHandler);
        router.post("/blockingA").blockingHandler(this::blockingHandlerA, false);
        router.post("/blockingB").blockingHandler(this::blockingHandlerB, false);
        router.post("/blockingC").blockingHandler(this::blockingHandlerC, false);
        router.post("/blockingPA").blockingHandler(this::blockingHandlerA);
        router.post("/blockingPB").blockingHandler(this::blockingHandlerB);
        router.post("/blockingPC").blockingHandler(this::blockingHandlerC);

        vertx.createHttpServer().requestHandler(router::accept).listen(1080);
    }

    void nonBlockingHandler(RoutingContext ctx) {
        ctx.request().exceptionHandler(this::execptionHandler);
        ctx.response().exceptionHandler(this::execptionHandler);
        ctx.request().setExpectMultipart(true);
        ctx.request().endHandler(v -> {
            vertx.executeBlocking(future -> {
                try {
                    aBlockingMethod();
                    String email = ctx.request().formAttributes().get("email");
                    future.complete(email);
                } catch (InterruptedException ie) {
                    LOG.log(Level.WARNING, ie.getLocalizedMessage(), ie);
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
                                .put("email", result.result())
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
                    ctx.response().close();
                }
            });
        });
    }

    void execptionHandler(Throwable t) {
        LOG.log(Level.WARNING, "My error handler", t);
    }

    void blockingHandlerA(RoutingContext ctx) {
        ctx.request().setExpectMultipart(true);
        ctx.request().exceptionHandler(this::execptionHandler);
        ctx.response().exceptionHandler(this::execptionHandler);
        process(ctx);
    }

    void blockingHandlerB(RoutingContext ctx) {
        ctx.request().exceptionHandler(this::execptionHandler);
        ctx.response().exceptionHandler(this::execptionHandler);
        ctx.request().setExpectMultipart(true);
        ctx.request().endHandler(v -> {
            // Calling from within the endHandler causes the event loop to be blocked.
            process(ctx);
        });
    }

    void blockingHandlerC(RoutingContext ctx) {
        ctx.request().exceptionHandler(this::execptionHandler);
        ctx.response().exceptionHandler(this::execptionHandler);
        ctx.request().setExpectMultipart(true);
        while (!ctx.request().isEnded()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ie) {
                LOG.log(Level.WARNING, ie.getLocalizedMessage(), ie);
            }
        }
        ctx.request().endHandler(v -> {
            // Calling from within the endHandler causes the event loop to be blocked.
            process(ctx);
        });
    }

    void aBlockingMethod() throws InterruptedException {
        Thread.sleep(3000);
    }

    private void process(RoutingContext ctx) {
        try {
            aBlockingMethod();
            String email = ctx.request().formAttributes().get("email");
            ctx.request().response().putHeader(CONTENT_TYPE, JSON)
                .setStatusMessage(OK.reasonPhrase())
                .setStatusCode(OK.code())
                .end(status
                        .put(STATUS, OK.reasonPhrase())
                        .put("type", "blocking")
                        .put("email", email)
                        .encodePrettily());
        } catch (InterruptedException ie) {
            LOG.log(Level.WARNING, ie.getLocalizedMessage(), ie);
            ctx.request().response()
                .putHeader(CONTENT_TYPE, JSON)
                .setStatusCode(INTERNAL_SERVER_ERROR.code())
                .setStatusMessage(INTERNAL_SERVER_ERROR.reasonPhrase())
                .end(status
                        .put(STATUS, INTERNAL_SERVER_ERROR.reasonPhrase())
                        .put("type", "blocking")
                        .encodePrettily());
        }
        ctx.response().close();
    }
}
