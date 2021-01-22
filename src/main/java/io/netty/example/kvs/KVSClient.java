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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;

public final class KVSClient {

    static final boolean SSL = System.getProperty("ssl") != null;
    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));

    public static void main(String[] args) throws Exception {
        // Configure SSL.
        final SslContext sslCtx;
        if (SSL) {
            sslCtx = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            final Map<Long, KVSResult> signals = new ConcurrentHashMap<Long, KVSResult>();
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    if (sslCtx != null) {
                        p.addLast(sslCtx.newHandler(ch.alloc(), HOST, PORT));
                    }
                    p.addLast(
                            new ObjectEncoder(),
                            new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                            new KVSClientHandler(signals));
                }
             });

            // Start the connection attempt.
            Channel ch = b.connect(HOST, PORT).sync().channel();

            for (int i = 1; i <= 100; i++) {
                KVSRequest kvsRequest = KVSRequest.newWriteRequest(format("key_%d", i), format("val_%d", i));
                KVSResult kvsResult = new KVSResult();
                signals.put(kvsRequest.getReqId(), kvsResult);
                ch.writeAndFlush(kvsRequest).sync();
                System.out.println("Waiting for response of write request...");
                String response = kvsResult.getKvsResponse().getResponse();
                System.out.println("Response received: " + response);
                signals.remove(kvsRequest.getReqId());
            }

            for (int i = 1; i <= 100; i++) {
                String key = format("key_%d", i);
                KVSRequest kvsRequest = KVSRequest.newReadRequest(key);
                KVSResult kvsResult = new KVSResult();
                signals.put(kvsRequest.getReqId(), kvsResult);
                ch.writeAndFlush(kvsRequest).sync();
                System.out.println("Waiting for response of read request...");
                String response = kvsResult.getKvsResponse().getResponse();
                System.out.println("Response received: " + response);
                int value = Integer.parseInt(response.substring(response.indexOf('_') + 1).trim());
                if (value != i) {
                    throw new IllegalStateException(format("mismatch for key: %s, got value: %s", key, value));
                }
                signals.remove(kvsRequest.getReqId());
            }

            ch.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
