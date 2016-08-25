package us.juggl.vertx.blocking;

import io.vertx.core.Vertx;

/**
 * Created by dphillips on 8/25/16.
 */
public class Main {

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle("us.juggl.vertx.blocking.ExampleVerticle");
    }
}
