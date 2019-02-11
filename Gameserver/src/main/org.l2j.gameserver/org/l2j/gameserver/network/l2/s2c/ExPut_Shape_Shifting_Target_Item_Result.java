package org.l2j.gameserver.network.l2.s2c;

import org.l2j.gameserver.network.l2.GameClient;

import java.nio.ByteBuffer;

/**
 * @author Bonux
**/
public class ExPut_Shape_Shifting_Target_Item_Result extends L2GameServerPacket
{
	public static L2GameServerPacket FAIL = new ExPut_Shape_Shifting_Target_Item_Result(0x00, 0L);
	public static int SUCCESS_RESULT = 0x01;

	private final int _resultId;
	private final long _price;

	public ExPut_Shape_Shifting_Target_Item_Result(int resultId, long price)
	{
		_resultId = resultId;
		_price = price;
	}
	
	@Override
	protected void writeImpl(GameClient client, ByteBuffer buffer)
	{
		buffer.putInt(_resultId);
		buffer.putLong(_price);
	}
}