package org.l2j.gameserver.network.l2.s2c;

import org.l2j.gameserver.model.Player;
import org.l2j.gameserver.network.l2.GameClient;
import org.l2j.gameserver.network.l2.s2c.updatetype.PartySmallWindowUpdateType;

import java.nio.ByteBuffer;

public class PartySmallWindowUpdatePacket extends L2GameServerPacket
{
	private int obj_id, class_id, level;
	private int curCp, maxCp, curHp, maxHp, curMp, maxMp, vitality;
	private String obj_name;
	private int _flags = 0;

	public PartySmallWindowUpdatePacket(Player member, boolean addAllFlags)
	{
		obj_id = member.getObjectId();
		obj_name = member.getName();
		curCp = (int) member.getCurrentCp();
		maxCp = member.getMaxCp();
		curHp = (int) member.getCurrentHp();
		maxHp = member.getMaxHp();
		curMp = (int) member.getCurrentMp();
		maxMp = member.getMaxMp();
		level = member.getLevel();
		class_id = member.getClassId().getId();

		if(addAllFlags)
		{
			for(PartySmallWindowUpdateType type : PartySmallWindowUpdateType.values())
				addUpdateType(type);
		}
	}

	public PartySmallWindowUpdatePacket(Player member)
	{
		this(member, true);
	}
	
	public void addUpdateType(PartySmallWindowUpdateType type)
	{
		_flags |= type.getMask();
	}

	@Override
	protected final void writeImpl(GameClient client, ByteBuffer buffer)
	{
		buffer.putInt(obj_id);
		buffer.putShort((short) _flags);
		if(containsMask(_flags, PartySmallWindowUpdateType.CURRENT_CP))
			buffer.putInt(curCp); // c4

		if(containsMask(_flags, PartySmallWindowUpdateType.MAX_CP))
			buffer.putInt(maxCp); // c4

		if(containsMask(_flags, PartySmallWindowUpdateType.CURRENT_HP))
			buffer.putInt(curHp);

		if(containsMask(_flags, PartySmallWindowUpdateType.MAX_HP))
			buffer.putInt(maxHp);

		if(containsMask(_flags, PartySmallWindowUpdateType.CURRENT_MP))
			buffer.putInt(curMp);

		if(containsMask(_flags, PartySmallWindowUpdateType.MAX_MP))
			buffer.putInt(maxMp);

		if(containsMask(_flags, PartySmallWindowUpdateType.LEVEL))
			buffer.put((byte)level);

		if(containsMask(_flags, PartySmallWindowUpdateType.CLASS_ID))
			buffer.putShort((short) class_id);

		if(containsMask(_flags, PartySmallWindowUpdateType.VITALITY_POINTS))
			buffer.putInt(0x00);
	}
}