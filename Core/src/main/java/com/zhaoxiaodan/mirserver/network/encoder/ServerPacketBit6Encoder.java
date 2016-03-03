package com.zhaoxiaodan.mirserver.network.encoder;

import com.zhaoxiaodan.mirserver.network.Bit6Coder;
import com.zhaoxiaodan.mirserver.network.Protocol;
import com.zhaoxiaodan.mirserver.network.packets.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class ServerPacketBit6Encoder extends MessageToMessageEncoder<ByteBuf> {


	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {



		ByteBuf buf = Unpooled.buffer();

		buf.writeByte('#');     // 加上 头分割符

		if('+' == in.getByte(0)){
			buf.writeBytes(in);
		}else{

			short protocolId = in.getByte(4);

			byte[] header = new byte[Packet.DEFAULT_HEADER_SIZE];
			in.readBytes(header);
			buf.writeBytes(Bit6Coder.encoder6BitBuf(header));

			if(protocolId == Protocol.SM_TURN.id){
				byte[] body = new byte[8];
				in.readBytes(body);
				buf.writeBytes(Bit6Coder.encoder6BitBuf(body));
			}
			byte[] body = new byte[in.readableBytes()];
			in.readBytes(body);
			buf.writeBytes(Bit6Coder.encoder6BitBuf(body));
		}


		buf.writeByte('!');     // 加上 尾分割符

		out.add(buf);
	}
}
