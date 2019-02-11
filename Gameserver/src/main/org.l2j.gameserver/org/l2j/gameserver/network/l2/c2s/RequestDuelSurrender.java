package org.l2j.gameserver.network.l2.c2s;

import org.l2j.gameserver.model.Player;
import org.l2j.gameserver.model.entity.events.impl.DuelEvent;

import java.nio.ByteBuffer;

public class RequestDuelSurrender extends L2GameClientPacket
{
	@Override
	protected void readImpl(ByteBuffer buffer)
	{}

	@Override
	protected void runImpl()
	{
		Player player = client.getActiveChar();
		if(player == null)
			return;

		DuelEvent duelEvent = player.getEvent(DuelEvent.class);
		if(duelEvent == null)
			return;

		duelEvent.packetSurrender(player);
	}
}