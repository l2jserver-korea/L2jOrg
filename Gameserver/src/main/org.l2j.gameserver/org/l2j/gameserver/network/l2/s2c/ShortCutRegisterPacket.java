package org.l2j.gameserver.network.l2.s2c;

import org.l2j.gameserver.model.Player;
import org.l2j.gameserver.model.actor.instances.player.ShortCut;
import org.l2j.gameserver.network.l2.GameClient;

import java.nio.ByteBuffer;

public class ShortCutRegisterPacket extends ShortCutPacket
{
	private ShortcutInfo _shortcutInfo;

	public ShortCutRegisterPacket(Player player, ShortCut sc)
	{
		_shortcutInfo = convert(player, sc);
	}

	@Override
	protected final void writeImpl(GameClient client, ByteBuffer buffer)
	{
		_shortcutInfo.write(buffer,this);
	}
}