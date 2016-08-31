/*     / \____  _    _  ____   ______  / \ ____  __    _______
 *    /  /    \/ \  / \/    \ /  /\__\/  //    \/  \  //  /\__\   JΛVΛSLΛNG
 *  _/  /  /\  \  \/  /  /\  \\__\\  \  //  /\  \ /\\/ \ /__\ \   Copyright 2014-2016 Javaslang, http://javaslang.io
 * /___/\_/  \_/\____/\_/  \_/\__\/__/\__\_/  \_//  \__/\_____/   Licensed under the Apache License, Version 2.0
 */
package io.andromeda.logconsumer;

import io.andromeda.servicediscovery.MessageConsumer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.mongo.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Action1;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MongoWriter extends AbstractVerticle {
    private static Logger LOGGER = LoggerFactory.getLogger(ConsoleWriter.class);


    private Action1<Throwable> logError = it -> LOGGER.error("{}", it);
    private Action1<String> logSuccess = it -> LOGGER.info("SUCCESS", it);
    private MongoClient mongo;
    String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    private JsonObject counterQuery = new JsonObject().put("findAndModify", "counters")
                                                      .put("_id", "LOG_COUNTER")
                                                      .put("update", new JsonObject().put("$inc", new JsonObject().put("seq", 1L)))
                                                      .put("upsert", true)
                                                      .put("new", true);

    private Observable<Long> nextId() {
        return mongo.runCommandObservable("findAndModify", counterQuery).map(it -> it.getJsonObject("value").getLong("seq"));
    }

    @Override
    public void start() throws Exception {
        LOGGER.info("ConsoleWriter Verticle starting");
        mongo = MongoClient.createShared(new Vertx(vertx), config().getJsonObject("mongoConfig"));
        Observable.from(config().getJsonArray("files"))
                  .map(el -> (String) el)
                  .flatMap(path -> MessageConsumer.<String> create(vertx, rec -> rec.getName().equals(path)))
                  .flatMap(el -> nextId().map(_id -> new JsonObject().put("_id", _id).put("i", el)))
                  .flatMap(el -> mongo.saveObservable(today, el))
                  .subscribe(logSuccess, logError);
    }
}

