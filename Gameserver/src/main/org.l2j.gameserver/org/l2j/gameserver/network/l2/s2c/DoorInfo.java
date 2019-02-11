package org.l2j.gameserver.network.l2.s2c;

import org.l2j.gameserver.model.instances.DoorInstance;
import org.l2j.gameserver.network.l2.GameClient;

import java.nio.ByteBuffer;

/**
 * 60
 * d6 6d c0 4b		door id
 * 8f 14 00 00 		x
 * b7 f1 00 00 		y
 * 60 f2 ff ff 		z
 * 00 00 00 00 		??
 *
 * format  dddd    rev 377  ID:%d X:%d Y:%d Z:%d
 *         ddddd   rev 419
 */
public class DoorInfo extends L2GameServerPacket
{
	private int obj_id, door_id, view_hp;

	//@Deprecated
	public DoorInfo(DoorInstance door)
	{
		obj_id = door.getObjectId();
		door_id = door.getDoorId();
		view_hp = door.isHPVisible() ? 1 : 0;
	}

	@Override
	protected final void writeImpl(GameClient client, ByteBuffer buffer)
	{
		buffer.putInt(obj_id);
		buffer.putInt(door_id);
		buffer.putInt(view_hp); // отображать ли хп у двери или стены
	}
}