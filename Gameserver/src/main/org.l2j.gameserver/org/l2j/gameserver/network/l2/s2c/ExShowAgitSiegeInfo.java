package org.l2j.gameserver.network.l2.s2c;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.l2j.gameserver.data.xml.holder.ResidenceHolder;
import org.l2j.gameserver.model.entity.residence.clanhall.NormalClanHall;
import org.l2j.gameserver.model.pledge.Clan;
import org.l2j.gameserver.network.l2.GameClient;
import org.l2j.gameserver.tables.ClanTable;

public class ExShowAgitSiegeInfo extends L2GameServerPacket
{
	private final List<AgitInfo> _infos;

	public ExShowAgitSiegeInfo()
	{
		List<NormalClanHall> clanHalls = ResidenceHolder.getInstance().getResidenceList(NormalClanHall.class);
		_infos = new ArrayList<AgitInfo>(clanHalls.size());
		clanHalls.forEach(clanHall ->
		{
			int ch_id = clanHall.getId();
			int getType = clanHall.getClanHallType().ordinal();
			Clan clan = ClanTable.getInstance().getClan(clanHall.getOwnerId());
			String clan_name = clanHall.getOwnerId() == 0 || clan == null ? "" : clan.getName();
			String leader_name = clanHall.getOwnerId() == 0 || clan == null ? "" : clan.getLeaderName();
			int siegeDate = (int) (clanHall.getSiegeDate().getTimeInMillis() / 1000);
			_infos.add(new AgitInfo(clan_name, leader_name, ch_id, getType, siegeDate));
		});
	}

	@Override
	protected final void writeImpl(GameClient client, ByteBuffer buffer)
	{
		buffer.putInt(_infos.size());
		_infos.forEach(info ->
		{
			buffer.putInt(info.ch_id);
			buffer.putInt(info.siegeDate);
			writeString(info.clan_name, buffer);
			writeString(info.leader_name, buffer);
			buffer.putShort((short) info.getType);
		});
	}

	static class AgitInfo
	{
		public String clan_name;
		public String leader_name;
		public int ch_id;
		public int getType;
		public int siegeDate;

		public AgitInfo(String clan_name, String leader_name, int ch_id, int lease, int siegeDate)
		{
			this.clan_name = clan_name;
			this.leader_name = leader_name;
			this.ch_id = ch_id;
			this.getType = lease;
			this.siegeDate = siegeDate;
		}
	}

}

