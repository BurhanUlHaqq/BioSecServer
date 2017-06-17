package com.idea.builders;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class Pipeline extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast("decoder", new ByteArrayDecoder());
        ch.pipeline().addLast("encoder", new StringEncoder());
        ch.pipeline().addLast("myHelper", new RequestHandler());
    }

}
