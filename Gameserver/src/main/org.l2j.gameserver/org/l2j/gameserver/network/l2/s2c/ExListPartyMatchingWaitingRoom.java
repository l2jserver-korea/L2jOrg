package org.l2j.gameserver.network.l2.s2c;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.l2j.gameserver.data.xml.holder.InstantZoneHolder;
import org.l2j.gameserver.instancemanager.MatchingRoomManager;
import org.l2j.gameserver.model.Player;
import org.l2j.gameserver.network.l2.GameClient;


public class ExListPartyMatchingWaitingRoom extends L2GameServerPacket
{
	private static final int ITEMS_PER_PAGE = 64;
	private final List<PartyMatchingWaitingInfo> _waitingList = new ArrayList<PartyMatchingWaitingInfo>(ITEMS_PER_PAGE);
	private final int _fullSize;

	public ExListPartyMatchingWaitingRoom(Player searcher, int minLevel, int maxLevel, int page, int[] classes)
	{
		final List<Player> temp = MatchingRoomManager.getInstance().getWaitingList(minLevel, maxLevel, classes);
		_fullSize = temp.size();

		final int first = Math.max((page - 1) * ITEMS_PER_PAGE, 0);
		final int firstNot = Math.min(page * ITEMS_PER_PAGE, _fullSize);
		for(int i = first; i < firstNot; i++)
			_waitingList.add(new PartyMatchingWaitingInfo(temp.get(i)));
	}

	@Override
	protected void writeImpl(GameClient client, ByteBuffer buffer)
	{
		buffer.putInt(_fullSize);
		buffer.putInt(_waitingList.size());
		for(PartyMatchingWaitingInfo waitingInfo : _waitingList)
		{
			writeString(waitingInfo.name, buffer);
			buffer.putInt(waitingInfo.classId);
			buffer.putInt(waitingInfo.level);
			buffer.putInt(waitingInfo.locationId);
			buffer.putInt(waitingInfo.instanceReuses.size());
			for(int i : waitingInfo.instanceReuses)
				buffer.putInt(i);
		}
	}

	static class PartyMatchingWaitingInfo
	{
		public final int classId, level, locationId;
		public final String name;
		public final List<Integer> instanceReuses;

		public PartyMatchingWaitingInfo(Player member)
		{
			name = member.getName();
			classId = member.getClassId().getId();
			level = member.getLevel();
			locationId = MatchingRoomManager.getInstance().getLocation(member);
			instanceReuses = InstantZoneHolder.getInstance().getLockedInstancesList(member);
		}
	}
}