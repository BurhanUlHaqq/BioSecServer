package com.idea.builders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

public class JoinRequest extends ByteToMessageDecoder {

//    @Override
//    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
//            throws Exception {
//        out.add(in.readBytes(in.readableBytes()));
//    }
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
       // System.out.println("yes joining");
        out.add(in.readBytes(in.readableBytes()).retain());
    }
}
