package org.l2j.gameserver.network.l2.s2c;

import io.github.joealisson.mmocore.StaticPacket;
import org.l2j.gameserver.network.l2.GameClient;

import java.nio.ByteBuffer;

/**
 * @author VISTALL
 * @date 11:33/03.07.2011
 */
@StaticPacket
public class ExGoodsInventoryChangedNotify extends L2GameServerPacket {
	public static final L2GameServerPacket STATIC = new ExGoodsInventoryChangedNotify();

	private ExGoodsInventoryChangedNotify() { }

	@Override
	protected void writeImpl(GameClient client, ByteBuffer buffer) {  }

	@Override
	protected int size(GameClient client) {
		return 5;
	}
}
