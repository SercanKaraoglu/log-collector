package io.andromeda.servicediscovery;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subjects.BehaviorSubject;

import java.util.function.Function;

public abstract class ServiceConsumer<Service> {
    private static Logger LOGGER = LoggerFactory.getLogger(ServiceConsumer.class);

    private ServiceConsumer() {
    }

    private ServiceConsumer(Vertx vertx, ServiceDiscovery serviceDiscovery) {
        this._vertx = vertx;
        this._serviceDiscovery = serviceDiscovery;
    }

    private Vertx _vertx;
    private ServiceDiscovery _serviceDiscovery;

    private Observable<Service> acquireResource = Observable.defer(() -> {
        BehaviorSubject<Service> subject = BehaviorSubject.create();

        return subject.doOnSubscribe(() -> {
            LOGGER.debug("Getting Service Record");
            _serviceDiscovery.getRecord(this::subscribe, res -> {
                if (res.succeeded()) {
                    Record record = res.result();
                    if (null == record) {
                        LOGGER.debug("Service is not available at this moment");
                    } else {
                        LOGGER.debug("Service Received");
                        subject.onNext(_serviceDiscovery.getReference(record).get());
                    }
                } else {
                }
            });
        });
    });
    private Observable<Service> listenServiceDeparture = Observable.defer(() -> {
        LOGGER.info("{} STATUS->Waiting For Service Arrival", this.getClass().getSimpleName());

        return new io.vertx.rxjava.core.Vertx(_vertx).eventBus().<JsonObject> consumer(ServiceDiscoveryOptions.DEFAULT_ANNOUNCE_ADDRESS)
                .bodyStream()
                .toObservable()
                .map(Record::new)
                .filter(this::subscribe)
                .flatMap(record -> acquireResource);
    });

    public static <T> ServiceConsumer<T> create(Vertx vertx, Function<Record, Boolean> _subscribe) {
        return new ServiceConsumer<T>(vertx, ServiceDiscovery.create(vertx)) {
            @Override
            protected Boolean subscribe(Record metaData) {
                return _subscribe.apply(metaData);
            }
        };
    }

    protected abstract Boolean subscribe(Record metaData);

    public <T> Observable<T> compose(Observable.Transformer<Service, T> transformer) {
        return Observable.merge(acquireResource, listenServiceDeparture).first().compose(transformer);
    }
}