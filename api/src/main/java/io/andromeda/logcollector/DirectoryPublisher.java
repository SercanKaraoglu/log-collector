/*     / \____  _    _  ____   ______  / \ ____  __    _______
 *    /  /    \/ \  / \/    \ /  /\__\/  //    \/  \  //  /\__\   JΛVΛSLΛNG
 *  _/  /  /\  \  \/  /  /\  \\__\\  \  //  /\  \ /\\/ \ /__\ \   Copyright 2014-2016 Javaslang, http://javaslang.io
 * /___/\_/  \_/\____/\_/  \_/\__\/__/\__\_/  \_//  \__/\_____/   Licensed under the Apache License, Version 2.0
 */
package io.andromeda.logcollector;

import io.andromeda.servicediscovery.ServiceProvider;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.servicediscovery.types.MessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Action1;

import java.util.function.Function;

public class DirectoryPublisher {
    private static Logger LOGGER = LoggerFactory.getLogger(DirectoryPublisher.class);
    Vertx _vertx;
    FileReader _server;

    public DirectoryPublisher(io.vertx.core.Vertx vertx, FileReader server) {
        this._vertx = new Vertx(vertx);
        this._server = server;
    }

    public <T> void publish(String name, Function<String, Action1<T>> method) {
        this._server.listDirectory(name)
                    .subscribe(path -> ServiceProvider.create(_vertx, method, this._server.source(name + "/" + path))
                                                      .register(MessageSource.createRecord(path, name + "/" + path, String.class.getName())),
                               error -> LOGGER.error("{}", error));
    }
}
