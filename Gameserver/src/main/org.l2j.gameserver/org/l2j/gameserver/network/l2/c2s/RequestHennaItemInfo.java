package org.l2j.gameserver.network.l2.c2s;

import org.l2j.gameserver.data.xml.holder.HennaHolder;
import org.l2j.gameserver.model.Player;
import org.l2j.gameserver.network.l2.s2c.HennaItemInfoPacket;
import org.l2j.gameserver.templates.HennaTemplate;

import java.nio.ByteBuffer;

public class RequestHennaItemInfo extends L2GameClientPacket
{
	// format  cd
	private int _symbolId;

	@Override
	protected void readImpl(ByteBuffer buffer)
	{
		_symbolId = buffer.getInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = client.getActiveChar();
		if(player == null)
			return;

		HennaTemplate template = HennaHolder.getInstance().getHenna(_symbolId);
		if(template != null)
			player.sendPacket(new HennaItemInfoPacket(template, player));
	}
}