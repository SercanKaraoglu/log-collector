/*     / \____  _    _  ____   ______  / \ ____  __    _______
 *    /  /    \/ \  / \/    \ /  /\__\/  //    \/  \  //  /\__\   JΛVΛSLΛNG
 *  _/  /  /\  \  \/  /  /\  \\__\\  \  //  /\  \ /\\/ \ /__\ \   Copyright 2014-2016 Javaslang, http://javaslang.io
 * /___/\_/  \_/\____/\_/  \_/\__\/__/\__\_/  \_//  \__/\_____/   Licensed under the Apache License, Version 2.0
 */
package io.andromeda.logcollector;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.vertx.core.Vertx;
import io.vertx.rxjava.core.buffer.Buffer;
import rx.Observable;
import rx.Subscriber;

import java.io.IOException;
import java.nio.file.Paths;

import static java.nio.file.Files.list;
import static java.util.stream.Collectors.toList;

public class LocalFileReader implements FileReader {
    private final Vertx _vertx;

    public LocalFileReader(Vertx vertx) {
        this._vertx = vertx;
    }

    @Override
    public Observable<String> listDirectory(String path) {
        return Observable.defer(() -> {
            try {
                return Observable.from(list(Paths.get(path)).map(_path -> _path.toFile().getName()).collect(toList()));
            } catch (IOException e) {
                return Observable.error(e);
            }
        });
    }

    private Observable.Operator<String, Buffer> extractLines() {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(8192);
        return subscriber ->
                new Subscriber<Buffer>() {
                    @Override
                    public void onCompleted() {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onCompleted();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onError(e);
                        }

                    }

                    @Override
                    public void onNext(Buffer buffer) {
                        ByteBuf _buf = ((io.vertx.core.buffer.Buffer) buffer.getDelegate()).getByteBuf();
                        while (_buf.readableBytes() > 0) {
                            byte b = _buf.readByte();
                            if ((b == '\n') || (b == '\r')) {
                                byte[] _bArr = new byte[buf.readableBytes()];
                                buf.readBytes(_bArr);
                                subscriber.onNext(new String(_bArr));
                            } else {
                                buf.writeByte(b);
                            }
                        }
                    }
                };
    }

    @Override
    public Observable<String> source(String path) {
        return new io.vertx.rxjava.core.Vertx(_vertx).fileSystem().readFileObservable(path).lift(extractLines());
    }
}
