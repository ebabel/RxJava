/**
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.CyclicBarrier;

import org.junit.*;
import org.reactivestreams.Publisher;

import io.reactivex.exceptions.TestException;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

public class XFlatMapTest {

    @Rule
    public Retry retry = new Retry(3, 1000, true);

    static final int SLEEP_AFTER_CANCEL = 500;

    final CyclicBarrier cb = new CyclicBarrier(2);

    void sleep() throws Exception {
        cb.await();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            // ignored here
        }
    }


    @Test
    public void observerSingle() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Integer> ts = Observable.just(1)
            .subscribeOn(Schedulers.io())
            .flatMapSingle(new Function<Integer, Single<Integer>>() {
                @Override
                public Single<Integer> apply(Integer v) throws Exception {
                    sleep();
                    return Single.<Integer>error(new TestException());
                }
            })
            .test();

            cb.await();

            Thread.sleep(50);

            ts.cancel();

            Thread.sleep(SLEEP_AFTER_CANCEL);

            ts.assertEmpty();

            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void singleSingle() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Integer> ts = Single.just(1)
            .subscribeOn(Schedulers.io())
            .flatMap(new Function<Integer, Single<Integer>>() {
                @Override
                public Single<Integer> apply(Integer v) throws Exception {
                    sleep();
                    return Single.<Integer>error(new TestException());
                }
            })
            .test();

            cb.await();

            Thread.sleep(50);

            ts.cancel();

            Thread.sleep(SLEEP_AFTER_CANCEL);

            ts.assertEmpty();

            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }
}
