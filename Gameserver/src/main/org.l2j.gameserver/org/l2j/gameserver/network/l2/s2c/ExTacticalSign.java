package org.l2j.gameserver.network.l2.s2c;

import org.l2j.gameserver.network.l2.GameClient;

import java.nio.ByteBuffer;

public class ExTacticalSign extends L2GameServerPacket
{
	public static final int STAR = 1;
	public static final int HEART = 2;
	public static final int MOON = 3;
	public static final int CROSS = 4;

	private int _targetId;
	private int _signId;

	public ExTacticalSign(int target, int sign)
	{
		_targetId = target;
		_signId = sign;
	}

	@Override
	protected final void writeImpl(GameClient client, ByteBuffer buffer)
	{
		buffer.putInt(_targetId);
		buffer.putInt(_signId);
	}
}
