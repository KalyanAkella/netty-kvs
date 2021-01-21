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

import java.io.Serializable;

public final class KVSRequest implements Serializable {
    public enum Type {
        READ, WRITE;
    }

    private final Long reqId;
    private final Type type;
    private final String key;
    private final String value;

    private KVSRequest(Long reqId, Type type, String key, String value) {
        this.reqId = reqId;
        this.type = type;
        this.key = key;
        this.value = value;
    }

    public static KVSRequest newReadRequest(String key) {
        return new KVSRequest(newRequestId(), Type.READ, key, null);
    }

    public static KVSRequest newWriteRequest(String key, String value) {
        return new KVSRequest(newRequestId(), Type.WRITE, key, value);
    }

    private static long newRequestId() {
        return System.nanoTime();
    }

    public Long getReqId() {
        return reqId;
    }

    public Type getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
