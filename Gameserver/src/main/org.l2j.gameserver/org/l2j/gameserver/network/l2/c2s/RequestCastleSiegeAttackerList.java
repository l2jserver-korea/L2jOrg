package org.l2j.gameserver.network.l2.c2s;

import org.l2j.gameserver.data.xml.holder.ResidenceHolder;
import org.l2j.gameserver.model.Player;
import org.l2j.gameserver.model.entity.residence.Residence;
import org.l2j.gameserver.network.l2.s2c.CastleSiegeAttackerListPacket;

import java.nio.ByteBuffer;

public class RequestCastleSiegeAttackerList extends L2GameClientPacket
{
	private int _unitId;

	@Override
	protected void readImpl(ByteBuffer buffer)
	{
		_unitId = buffer.getInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = client.getActiveChar();
		if(player == null)
			return;

		Residence residence = ResidenceHolder.getInstance().getResidence(_unitId);
		if(residence != null)
			sendPacket(new CastleSiegeAttackerListPacket(residence));
	}
}