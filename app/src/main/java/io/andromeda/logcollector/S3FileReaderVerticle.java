/*     / \____  _    _  ____   ______  / \ ____  __    _______
 *    /  /    \/ \  / \/    \ /  /\__\/  //    \/  \  //  /\__\   JΛVΛSLΛNG
 *  _/  /  /\  \  \/  /  /\  \\__\\  \  //  /\  \ /\\/ \ /__\ \   Copyright 2014-2016 Javaslang, http://javaslang.io
 * /___/\_/  \_/\____/\_/  \_/\__\/__/\__\_/  \_//  \__/\_____/   Licensed under the Apache License, Version 2.0
 */
package io.andromeda.logcollector;

import io.vertx.rxjava.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Action1;

public class S3FileReaderVerticle extends AbstractVerticle {
    private static Logger LOGGER = LoggerFactory.getLogger(LocalFileReaderVerticle.class);

    private Action1<String> publish(String address) {
        return message ->
                vertx.eventBus().send(address, message);
    }

    @Override
    public void start() throws Exception {
        LOGGER.info("FileServer Verticle starting");
        FileReader readingServer = new S3FileReader(config().getString("profile"), config().getString("region"));
        new DirectoryPublisher(this.getVertx(), readingServer)
                .publish(config().getString("path"), this::publish);
    }
}
