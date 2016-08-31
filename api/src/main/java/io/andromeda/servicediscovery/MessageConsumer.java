/*     / \____  _    _  ____   ______  / \ ____  __    _______
 *    /  /    \/ \  / \/    \ /  /\__\/  //    \/  \  //  /\__\   JΛVΛSLΛNG
 *  _/  /  /\  \  \/  /  /\  \\__\\  \  //  /\  \ /\\/ \ /__\ \   Copyright 2014-2016 Javaslang, http://javaslang.io
 * /___/\_/  \_/\____/\_/  \_/\__\/__/\__\_/  \_//  \__/\_____/   Licensed under the Apache License, Version 2.0
 */
package io.andromeda.servicediscovery;

import io.vertx.core.Vertx;
import io.vertx.rxjava.core.streams.ReadStream;
import io.vertx.servicediscovery.Record;
import rx.Observable;

import java.util.function.Function;

public class MessageConsumer {
    private static <T> Observable.Transformer<io.vertx.core.eventbus.MessageConsumer<T>, T> extractMessageConsumer() {
        return observable -> observable.map(io.vertx.rxjava.core.eventbus.MessageConsumer<T>::new)
                                       .map(io.vertx.rxjava.core.eventbus.MessageConsumer::bodyStream)
                                       .flatMap(ReadStream::toObservable);
    }

    public static <T> Observable<T> create(Vertx vertx, Function<Record, Boolean> _subscribe) {
        return ServiceConsumer.<io.vertx.core.eventbus.MessageConsumer<T>> create(vertx, _subscribe).compose(extractMessageConsumer());
    }
}
