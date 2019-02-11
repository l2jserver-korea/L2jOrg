package org.l2j.gameserver.network.l2.c2s;

import org.l2j.gameserver.Config;
import org.l2j.gameserver.model.Player;
import org.l2j.gameserver.model.items.ItemInstance;
import org.l2j.gameserver.network.l2.components.SystemMsg;
import org.l2j.gameserver.network.l2.s2c.ExPutCommissionResultForVariationMake;
import org.l2j.gameserver.templates.item.support.variation.VariationFee;
import org.l2j.gameserver.utils.VariationUtils;

import java.nio.ByteBuffer;

public class RequestConfirmGemStone extends L2GameClientPacket
{
	// format: (ch)dddd
	private int _targetItemObjId;
	private int _refinerItemObjId;
	private int _feeItemObjectId;
	private long _feeItemCount;

	@Override
	protected void readImpl(ByteBuffer buffer)
	{
		_targetItemObjId = buffer.getInt();
		_refinerItemObjId = buffer.getInt();
		_feeItemObjectId = buffer.getInt();
		_feeItemCount = buffer.getLong();
	}

	@Override
	protected void runImpl()
	{
		if(_feeItemCount <= 0)
			return;

		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		if(!Config.ALLOW_AUGMENTATION)
		{
			activeChar.sendActionFailed();
			return;
		}
		ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
		ItemInstance feeItem = activeChar.getInventory().getItemByObjectId(_feeItemObjectId);

		if(targetItem == null || refinerItem == null || feeItem == null)
		{
			activeChar.sendPacket(SystemMsg.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}

		if(!targetItem.canBeAugmented(activeChar))
		{
			activeChar.sendPacket(SystemMsg.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}

		VariationFee fee = VariationUtils.getVariationFee(targetItem, refinerItem);
		if(fee == null)
		{
			activeChar.sendPacket(SystemMsg.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}

		if(fee.getFeeItemId() != feeItem.getItemId())
		{
			activeChar.sendPacket(SystemMsg.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}

		/*if(_feeItemCount != fee.getFeeItemCount())
		{
			activeChar.sendPacket(SystemMsg.GEMSTONE_QUANTITY_IS_INCORRECT);
			return;
		}*/

		activeChar.sendPacket(new ExPutCommissionResultForVariationMake(_feeItemObjectId, fee.getFeeItemCount()), SystemMsg.PRESS_THE_AUGMENT_BUTTON_TO_BEGIN);
	}
}