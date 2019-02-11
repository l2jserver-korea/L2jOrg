package org.l2j.gameserver.network.l2.c2s;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.l2j.commons.threading.RunnableImpl;
import org.l2j.gameserver.Config;
import org.l2j.gameserver.ThreadPoolManager;
import org.l2j.gameserver.data.xml.holder.BuyListHolder;
import org.l2j.gameserver.data.xml.holder.ItemHolder;
import org.l2j.gameserver.model.Player;
import org.l2j.gameserver.model.instances.NpcInstance;
import org.l2j.gameserver.model.items.Inventory;
import org.l2j.gameserver.network.l2.components.SystemMsg;
import org.l2j.gameserver.network.l2.s2c.ShopPreviewInfoPacket;
import org.l2j.gameserver.network.l2.s2c.ShopPreviewListPacket;
import org.l2j.gameserver.templates.item.ItemTemplate;
import org.l2j.gameserver.templates.item.WeaponTemplate.WeaponType;
import org.l2j.gameserver.templates.npc.BuyListTemplate;
import org.l2j.gameserver.utils.NpcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestPreviewItem extends L2GameClientPacket
{
	// format: cdddb
	private static final Logger _log = LoggerFactory.getLogger(RequestPreviewItem.class);

	@SuppressWarnings("unused")
	private int _unknow;
	private int _listId;
	private int _count;
	private int[] _items;

	@Override
	protected void readImpl(ByteBuffer buffer)
	{
		_unknow = buffer.getInt();
		_listId = buffer.getInt();
		_count = buffer.getInt();
		if(_count * 4 > buffer.remaining() || _count > Short.MAX_VALUE || _count < 1)
		{
			_count = 0;
			return;
		}
		_items = new int[_count];
		for(int i = 0; i < _count; i++)
			_items[i] = buffer.getInt();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null || _count == 0)
			return;

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && activeChar.isPK() && !activeChar.isGM())
		{
			activeChar.sendActionFailed();
			return;
		}

		BuyListTemplate list = null;

		NpcInstance merchant = NpcUtils.canPassPacket(activeChar, this, new Object[0]);
		if(merchant != null)
			list = merchant.getBuyList(_listId);

		if(activeChar.isGM() && (merchant == null || list == null || merchant.getNpcId() != list.getNpcId()))
			list = BuyListHolder.getInstance().getBuyList(_listId);

		if(list == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		int slots = 0;
		long totalPrice = 0; // Цена на примерку каждого итема 10 Adena.

		Map<Integer, Integer> itemList = new HashMap<Integer, Integer>();
		try
		{
			for(int i = 0; i < _count; i++)
			{
				int itemId = _items[i];
				if(list.getItemByItemId(itemId) == null)
				{
					activeChar.sendActionFailed();
					return;
				}

				ItemTemplate template = ItemHolder.getInstance().getTemplate(itemId);
				if(template == null)
					continue;

				if(!template.isEquipable())
					continue;

				int paperdoll = Inventory.getPaperdollIndex(template.getBodyPart());
				if(paperdoll < 0)
					continue;

				if(template.getItemType() == WeaponType.CROSSBOW || template.getItemType() == WeaponType.RAPIER || template.getItemType() == WeaponType.ANCIENTSWORD)
					continue;

				if(itemList.containsKey(paperdoll))
				{
					activeChar.sendPacket(SystemMsg.YOU_CAN_NOT_TRY_THOSE_ITEMS_ON_AT_THE_SAME_TIME);
					return;
				}
				else
					itemList.put(paperdoll, itemId);

				totalPrice += ShopPreviewListPacket.getWearPrice(template);
			}

			if(!activeChar.reduceAdena(totalPrice))
			{
				activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
		}
		catch(ArithmeticException ae)
		{
			//TODO audit
			activeChar.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}

		if(!itemList.isEmpty())
		{
			activeChar.sendPacket(new ShopPreviewInfoPacket(itemList));
			// Schedule task
			ThreadPoolManager.getInstance().schedule(new RemoveWearItemsTask(activeChar), Config.WEAR_DELAY * 1000);
		}
	}

	private static class RemoveWearItemsTask extends RunnableImpl
	{
		private Player _activeChar;

		public RemoveWearItemsTask(Player activeChar)
		{
			_activeChar = activeChar;
		}

		public void runImpl() throws Exception
		{
			_activeChar.sendPacket(SystemMsg.YOU_ARE_NO_LONGER_TRYING_ON_EQUIPMENT_);
			_activeChar.sendUserInfo(true);
		}
	}
}