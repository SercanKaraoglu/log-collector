/*     / \____  _    _  ____   ______  / \ ____  __    _______
 *    /  /    \/ \  / \/    \ /  /\__\/  //    \/  \  //  /\__\   JΛVΛSLΛNG
 *  _/  /  /\  \  \/  /  /\  \\__\\  \  //  /\  \ /\\/ \ /__\ \   Copyright 2014-2016 Javaslang, http://javaslang.io
 * /___/\_/  \_/\____/\_/  \_/\__\/__/\__\_/  \_//  \__/\_____/   Licensed under the Apache License, Version 2.0
 */
package io.andromeda.logcollector;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import rx.Observable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;

public class S3FileReader implements FileReader {
    private AmazonS3 s3;

    public S3FileReader(String profile, String region) {
        this.s3 = new AmazonS3Client(new ProfileCredentialsProvider(profile));
        s3.setRegion(com.amazonaws.regions.Region.getRegion(Regions.fromName(region)));
    }

    @Override
    public Observable<String> listDirectory(String path) {
        return Observable.defer(() -> {
            try {
                ParsePath parsePath = new ParsePath(path).invoke();
                if (parsePath.isAvailable()) return Observable.empty();
                ObjectListing objectListing = s3.listObjects(new ListObjectsRequest().withBucketName(parsePath.getBucket()).withPrefix(parsePath.getKey()));
                return Observable.from(objectListing.getObjectSummaries()).filter(objectSummary -> !Pattern.matches(".*\\/$", objectSummary.getKey())).map(S3ObjectSummary::getKey);
            } catch (AmazonClientException e) {
                return Observable.error(e);
            }

        });
    }

    @Override
    public Observable<String> source(String path) {
        ParsePath parsePath = new ParsePath(path).invoke();
        if (parsePath.isAvailable()) return Observable.empty();
        String bucket = parsePath.getBucket();
        String key = parsePath.getKey();
        return Observable.create(subscriber -> {
            try {
                S3ObjectInputStream s3ObjectInputStream = s3.getObject(new GetObjectRequest(bucket, key)).getObjectContent();
                InputStream inputStream;
                if (path.endsWith(".gz")) {
                    inputStream = new GZIPInputStream(s3ObjectInputStream);
                } else {
                    inputStream = s3ObjectInputStream;
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                while (!subscriber.isUnsubscribed()) {
                    String line = reader.readLine();
                    if (line == null) {
                        subscriber.onCompleted();
                        break;
                    }
                    subscriber.onNext(line);
                }
            } catch (IOException e) {
                subscriber.onError(e);
            }
        });
    }

    private class ParsePath {
        private boolean myResult;
        private String path;
        private String bucket;
        private String key;

        public ParsePath(String path) {this.path = path;}

        boolean isAvailable() {return myResult;}

        public String getBucket() {
            return bucket;
        }

        public String getKey() {
            return key;
        }

        public ParsePath invoke() {
            String[] paths = path.split("/");
            if (paths.length < 2) {
                myResult = true;
                return this;
            }
            bucket = paths[0];
            key = IntStream.rangeClosed(1, paths.length - 1).mapToObj(i -> paths[i]).collect(Collectors.joining("/"));
            myResult = false;
            return this;
        }
    }
}
