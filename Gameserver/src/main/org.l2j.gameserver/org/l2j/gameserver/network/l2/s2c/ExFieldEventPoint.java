package org.l2j.gameserver.network.l2.s2c;

import org.l2j.gameserver.network.l2.GameClient;

import java.nio.ByteBuffer;

public class ExFieldEventPoint extends L2GameServerPacket
{
	private final int _points;

	public ExFieldEventPoint(int points)
	{
		_points = points;
	}

	@Override
	protected final void writeImpl(GameClient client, ByteBuffer buffer)
	{
		buffer.putInt(_points);
	}
}