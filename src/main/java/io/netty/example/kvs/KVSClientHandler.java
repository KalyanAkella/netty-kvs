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

import java.util.Map;

/**
 * Handler implementation for the object echo client.  It initiates the
 * ping-pong traffic between the object echo client and server by sending the
 * first message to the server.
 */
public final class KVSClientHandler extends SimpleChannelInboundHandler<KVSResponse> {

    private final Map<Long, KVSResult> latches;

    public KVSClientHandler(Map<Long, KVSResult> latches) {
        this.latches = latches;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, KVSResponse kvsResponse) throws Exception {
        Long reqId = kvsResponse.getReqId();
        KVSResult kvsResult = latches.get(reqId);
        kvsResult.setKvsResponse(kvsResponse);
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
