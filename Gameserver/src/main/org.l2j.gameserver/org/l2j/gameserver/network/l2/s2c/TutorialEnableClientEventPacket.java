package org.l2j.gameserver.network.l2.s2c;

import org.l2j.gameserver.network.l2.GameClient;

import java.nio.ByteBuffer;

public class TutorialEnableClientEventPacket extends L2GameServerPacket
{
	private int _event = 0;

	public TutorialEnableClientEventPacket(int event)
	{
		_event = event;
	}

	@Override
	protected final void writeImpl(GameClient client, ByteBuffer buffer)
	{
		buffer.putInt(_event);
	}
}