package com.zhaoxiaodan.mirserver.gameserver.handler;

import com.zhaoxiaodan.mirserver.gameserver.entities.Config;
import com.zhaoxiaodan.mirserver.gameserver.entities.Player;
import com.zhaoxiaodan.mirserver.gameserver.GameClientPackets;
import com.zhaoxiaodan.mirserver.gameserver.GameServerPackets;
import com.zhaoxiaodan.mirserver.network.packets.ClientPacket;

public class ActionHandler extends PlayerHandler {

	@Override
	public void onPacket(ClientPacket packet, Player player) throws Exception {

		GameClientPackets.Action request = (GameClientPackets.Action) packet;


		if (!player.checkAndIncActionTime(Config.PLAYER_ACTION_INTERVAL_TIME)) {
			session.sendPacket(new GameServerPackets.ActionStatus(GameServerPackets.ActionStatus.Result.Fail));
			return;
		}

		player.direction = request.direction;
		boolean isSucc = false;
		switch (packet.protocol) {
			case CM_WALK:
				isSucc = player.walk(request.direction);
				break;
			case CM_RUN:
				isSucc = player.run(request.direction);
				break;
			case CM_TURN:
				isSucc = player.turn(request.direction);
				player.session.sendPacket(new GameServerPackets.Turn(player));
				break;
			case CM_LONGHIT: // TODO: 16/3/13 外挂的自动刺杀不会"开启刺杀", 只会使用 CM_LONGHIT 的 Hit,
				isSucc = player.hit(request.direction, 12, 2);
				break;
			case CM_FIREHIT: // TODO: 16/3/13 外挂会自动用Spell来"开启烈火", 使用烈火的时候发 CM_FIREHIT 的Hit
				isSucc = player.hit(request.direction, 26);
				break;
			case CM_WIDEHIT:
				isSucc = player.hit(request.direction, 25);
				break;
			case CM_HIT:
			case CM_BIGHIT:
			case CM_HEAVYHIT:
			case CM_POWERHIT:
				isSucc = player.hit(request.direction);
				break;
			default:
				isSucc = true;
				break;
		}

		if (player.currMapPoint.x != request.x || player.currMapPoint.y != request.y) {
			player.session.sendPacket(new GameServerPackets.Turn(player));
		}

		if (isSucc)
			session.sendPacket(new GameServerPackets.ActionStatus(GameServerPackets.ActionStatus.Result.Good));
		else
			session.sendPacket(new GameServerPackets.ActionStatus(GameServerPackets.ActionStatus.Result.Fail));
	}
}
