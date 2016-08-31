package io.andromeda.logcollector;

import io.vertx.rxjava.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Action1;

/**
 * Created by sercan on 04.08.2016.
 */

/*On the Fly File Reading*/
public class LocalFileReaderVerticle extends AbstractVerticle {
    private static Logger LOGGER = LoggerFactory.getLogger(LocalFileReaderVerticle.class);

    private Action1<String> publish(String address) {
        return message -> vertx.eventBus().send(address, message);
    }

    @Override
    public void start() throws Exception {
        super.start();
        LOGGER.info("FileServer Verticle starting");
        FileReader readingServer = new LocalFileReader(this.getVertx());
        new DirectoryPublisher(this.getVertx(), readingServer)
                .publish(config().getString("path"), this::publish);
    }
}
