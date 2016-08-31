/*     / \____  _    _  ____   ______  / \ ____  __    _______
 *    /  /    \/ \  / \/    \ /  /\__\/  //    \/  \  //  /\__\   JΛVΛSLΛNG
 *  _/  /  /\  \  \/  /  /\  \\__\\  \  //  /\  \ /\\/ \ /__\ \   Copyright 2014-2016 Javaslang, http://javaslang.io
 * /___/\_/  \_/\____/\_/  \_/\__\/__/\__\_/  \_//  \__/\_____/   Licensed under the Apache License, Version 2.0
 */
package io.andromeda.logconsumer;

import io.andromeda.servicediscovery.MessageConsumer;
import io.vertx.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Action1;

public class ConsoleWriter extends AbstractVerticle {
    private static Logger LOGGER = LoggerFactory.getLogger(ConsoleWriter.class);


    private Action1<Throwable> logError = it -> LOGGER.error("{}", it);

    @Override
    public void start() throws Exception {
        LOGGER.info("ConsoleWriter Verticle starting");
        Observable.from(config().getJsonArray("files"))
                  .map(el -> (String) el)
                  .flatMap(path -> MessageConsumer.<String> create(vertx, rec -> rec.getName().equals(path)))
                  .subscribe(System.out::println, logError);
    }
}
