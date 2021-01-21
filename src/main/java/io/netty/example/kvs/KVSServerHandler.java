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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.ConcurrentMap;

public final class KVSServerHandler extends SimpleChannelInboundHandler<KVSRequest> {

    private final ConcurrentMap<String, String> kvs;

    public KVSServerHandler(ConcurrentMap<String, String> kvs) {
        this.kvs = kvs;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, KVSRequest kvsRequest) throws Exception {
        String key = kvsRequest.getKey();
        switch (kvsRequest.getType()) {
            case READ:
                String value = kvs.get(key);
                ctx.writeAndFlush(new KVSResponse(kvsRequest.getReqId(), value));
                break;
            case WRITE:
                String response = kvs.put(key, kvsRequest.getValue());
                ctx.writeAndFlush(new KVSResponse(kvsRequest.getReqId(), response));
                break;
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
