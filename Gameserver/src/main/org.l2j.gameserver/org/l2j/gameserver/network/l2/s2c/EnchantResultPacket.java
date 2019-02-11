package org.l2j.gameserver.network.l2.s2c;

import io.github.joealisson.mmocore.StaticPacket;
import org.l2j.gameserver.network.l2.GameClient;

import java.nio.ByteBuffer;

@StaticPacket
public class EnchantResultPacket extends L2GameServerPacket
{
	private final int _resultId, _crystalId;
	private final long _count;
	private final int _enchantLevel;

	//public static final EnchantResultPacket SUCESS = new EnchantResultPacket(0, 0, 0, 0); // вещь заточилась, в статичном виде не используется
	//public static final EnchantResultPacket FAILED = new EnchantResultPacket(1, 0, 0); // вещь разбилась, требует указания получившихся кристаллов, в статичном виде не используется
	public static final EnchantResultPacket CANCEL = new EnchantResultPacket(2, 0, 0, 0); // заточка невозможна
	public static final EnchantResultPacket BLESSED_FAILED = new EnchantResultPacket(3, 0, 0, 0); // заточка не удалась, уровень заточки сброшен на 0
	public static final EnchantResultPacket FAILED_NO_CRYSTALS = new EnchantResultPacket(4, 0, 0, 0); // вещь разбилась, но кристаллов не получилось (видимо для эвента, сейчас использовать невозможно, там заглушка)
	public static final EnchantResultPacket ANCIENT_FAILED = new EnchantResultPacket(5, 0, 0, 0); // заточка не удалась, уровень заточки не изменен (для Ancient Enchant Crystal из итем молла)

	public EnchantResultPacket(int resultId, int crystalId, long count, int enchantLevel)
	{
		_resultId = resultId;
		_crystalId = crystalId;
		_count = count;
		_enchantLevel = enchantLevel;
	}

	@Override
	protected final void writeImpl(GameClient client, ByteBuffer buffer)
	{
		buffer.putInt(_resultId);
		buffer.putInt(_crystalId); // item id кристаллов
		buffer.putLong(_count); // количество кристаллов
		buffer.putInt(_enchantLevel); // уровень заточки
		buffer.putShort((short) 0x00); // uNK
		buffer.putShort((short) 0x00); // uNK
		buffer.putShort((short) 0x00); // uNK
	}
}