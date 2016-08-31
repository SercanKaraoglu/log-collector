package io.andromeda.servicediscovery;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Action1;

import java.util.function.Function;

public abstract class ServicePublisher<MessageType> {
    private static Logger LOGGER = LoggerFactory.getLogger(ServicePublisher.class);
    private Vertx _vertx;
    private ServiceDiscovery _serviceDiscovery;
    private Action1<Throwable> logError = it -> LOGGER.error("{}", it);

    private ServicePublisher(Vertx vertx, ServiceDiscovery serviceDiscovery) {
        this._vertx = vertx;
        this._serviceDiscovery = serviceDiscovery;
    }

    public static <T> ServicePublisher<T> create(Vertx vertx, Function<String, Action1<T>> publish, Observable<T> source) {
        return new ServicePublisher<T>(vertx, ServiceDiscovery.create(vertx)) {
            @Override
            public Action1<T> publish(String address) {
                return publish.apply(address);
            }

            @Override
            public Observable<T> source() {
                return source;
            }
        };
    }

    public abstract Action1<MessageType> publish(String address);

    public abstract Observable<MessageType> source();

    private void send(Action1<MessageType> sender) {
        if (null != sender) {
            LOGGER.debug("PUBLISHED");
            source().subscribe(sender, logError);
        } else {
            LOGGER.error("SENDER NULL");
        }
    }

    public void publish(Record rec) {
        _vertx.eventBus()
                .<JsonObject> consumer(ServiceDiscoveryOptions.DEFAULT_USAGE_ADDRESS)
                .bodyStream()
                .toObservable()
                .filter(service -> service.getJsonObject("record").getString("name").equals(rec.getName()))
                .doOnNext(j -> System.out.println(j.encodePrettily()))
                .filter(it -> "bind".equals(it.getString("type")))
                .map(it -> it.getJsonObject("record").getJsonObject("location").getString("endpoint"))
                .map(this::publish)
                .subscribe(this::send, logError);

        _serviceDiscovery.publishObservable(rec).subscribe(res -> LOGGER.debug("published {}", res.toJson().encodePrettily()), logError);

    }
}
