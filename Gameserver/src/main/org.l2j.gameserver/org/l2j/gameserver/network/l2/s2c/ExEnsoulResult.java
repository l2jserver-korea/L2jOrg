package org.l2j.gameserver.network.l2.s2c;

import org.l2j.gameserver.network.l2.GameClient;
import org.l2j.gameserver.templates.item.support.Ensoul;

import java.nio.ByteBuffer;

/**
 * @author Bonux
 **/
public class ExEnsoulResult extends L2GameServerPacket
{
	public static final L2GameServerPacket FAIL = new ExEnsoulResult();

	private final boolean _success;
	private final Ensoul[] _normalEnsouls;
	private final Ensoul[] _specialEnsouls;

	private ExEnsoulResult()
	{
		_success = false;
		_normalEnsouls = null;
		_specialEnsouls = null;
	}

	public ExEnsoulResult(Ensoul[] normalEnsouls, Ensoul[] specialEnsouls)
	{
		_success = true;
		_normalEnsouls = normalEnsouls;
		_specialEnsouls = specialEnsouls;
	}

	@Override
	protected final void writeImpl(GameClient client, ByteBuffer buffer)
	{
		buffer.put((byte) (_success ? 1 : 0));
		if(_success)
		{
			buffer.put((byte)_normalEnsouls.length);
			for(Ensoul ensoul : _normalEnsouls)
				buffer.putInt(ensoul.getId());

			buffer.put((byte)_specialEnsouls.length);
			for(Ensoul ensoul : _specialEnsouls)
				buffer.putInt(ensoul.getId());
		}
	}
}