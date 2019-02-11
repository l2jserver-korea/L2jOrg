package org.l2j.gameserver.network.l2.s2c;

import org.l2j.gameserver.network.l2.GameClient;

import java.nio.ByteBuffer;

/**
 *
 * @author monithly
 */
public class ExDeletePartySubstitute extends L2GameServerPacket
{
	private final int _obj;

	public ExDeletePartySubstitute(final int objectId)
	{
		_obj = objectId;
	}

	@Override
	protected void writeImpl(GameClient client, ByteBuffer buffer)
	{
		buffer.putInt(_obj);
	}
}
