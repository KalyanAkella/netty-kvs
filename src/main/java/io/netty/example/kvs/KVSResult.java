/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.example.kvs;

import java.util.concurrent.CountDownLatch;

public final class KVSResult {
    private final CountDownLatch latch;

    private KVSResponse kvsResponse;

    public KVSResult() {
        this.latch = new CountDownLatch(1);
    }

    public KVSResponse getKvsResponse() throws InterruptedException {
        this.latch.await();
        return kvsResponse;
    }

    public void setKvsResponse(KVSResponse kvsResponse) {
        this.kvsResponse = kvsResponse;
        this.latch.countDown();
    }
}
