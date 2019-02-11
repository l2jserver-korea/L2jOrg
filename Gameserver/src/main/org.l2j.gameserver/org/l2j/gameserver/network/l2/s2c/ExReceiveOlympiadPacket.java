package org.l2j.gameserver.network.l2.s2c;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.l2j.gameserver.Config;
import org.l2j.gameserver.model.base.TeamType;
import org.l2j.gameserver.model.entity.olympiad.Olympiad;
import org.l2j.gameserver.model.entity.olympiad.OlympiadGame;
import org.l2j.gameserver.model.entity.olympiad.OlympiadManager;
import org.l2j.gameserver.model.entity.olympiad.OlympiadMember;
import org.l2j.gameserver.network.l2.GameClient;
import org.l2j.gameserver.network.l2.ServerPacketOpcodes;

/**
 * @author VISTALL
 * @date 0:50/09.04.2011
 */
public abstract class ExReceiveOlympiadPacket extends L2GameServerPacket
{
	public static class MatchList extends ExReceiveOlympiadPacket
	{
		private List<ArenaInfo> _arenaList = Collections.emptyList();

		public MatchList()
		{
			super(0);
			OlympiadManager manager = Olympiad._manager;
			if(manager != null)
			{
				_arenaList = new ArrayList<ArenaInfo>();
				for(int i = 0; i < Olympiad.STADIUMS.length; i++)
				{
					OlympiadGame game = manager.getOlympiadInstance(i);
					if(game != null && game.getState() > 0)
						_arenaList.add(new ArenaInfo(i, game.getState(), game.getType().ordinal(), game.getMemberName1(), game.getMemberName2()));
				}
			}
		}

		public MatchList(List<ArenaInfo> arenaList)
		{
			super(0);
			_arenaList = arenaList;
		}

		@Override
		protected void writeImpl(GameClient client, ByteBuffer buffer)
		{
			super.writeImpl(client, buffer);
			buffer.putInt(_arenaList.size());
			buffer.putInt(0x00); //unknown
			for(ArenaInfo arena : _arenaList)
			{
				buffer.putInt(arena._id);
				buffer.putInt(arena._matchType);
				buffer.putInt(arena._status);
				writeString(arena._name1, buffer);
				writeString(arena._name2, buffer);
			}
		}

		public static class ArenaInfo
		{
			public int _status;
			private int _id, _matchType;
			public String _name1, _name2;

			public ArenaInfo(int id, int status, int match_type, String name1, String name2) {
				_id = id;
				_status = status;
				_matchType = match_type;
				_name1 = name1;
				_name2 = name2;
			}
		}
	}

	public static class MatchResult extends ExReceiveOlympiadPacket
	{
		private boolean _tie;
		private String _name;
		private List<PlayerInfo> _teamOne = new ArrayList<PlayerInfo>(3);
		private List<PlayerInfo> _teamTwo = new ArrayList<PlayerInfo>(3);

		public MatchResult(boolean tie, String winnerName)
		{
			super(1);
			_tie = tie;
			_name = winnerName;
		}

		public void addPlayer(TeamType team, OlympiadMember member, int gameResultPoints, int dealOutDamage)
		{
			int points = Config.OLYMPIAD_OLDSTYLE_STAT ? 0 : member.getStat().getPoints();

			addPlayer(team, member.getName(), member.getClanName(), member.getClassId(), points, gameResultPoints, dealOutDamage);
		}

		public void addPlayer(TeamType team, String name, String clanName, int classId, int points, int resultPoints, int damage)
		{
			switch(team)
			{
				case RED:
					_teamOne.add(new PlayerInfo(name, clanName, classId, points, resultPoints, damage));
					break;
				case BLUE:
					_teamTwo.add(new PlayerInfo(name, clanName, classId, points, resultPoints, damage));
					break;
			}
		}

		@Override
		protected void writeImpl(GameClient client, ByteBuffer buffer)
		{
			super.writeImpl(client, buffer);
			buffer.putInt(_tie? 1 : 0);
			writeString(_name, buffer);
			buffer.putInt(0x01);
			buffer.putInt(_teamOne.size());
			for (PlayerInfo playerInfo : _teamOne)
			{
				writeString(playerInfo._name, buffer);
				writeString(playerInfo._clanName, buffer);
				buffer.putInt(0x00);
				buffer.putInt(playerInfo._classId);
				buffer.putInt(playerInfo._damage);
				buffer.putInt(playerInfo._currentPoints);
				buffer.putInt(playerInfo._gamePoints);
				buffer.putInt(0x00);
			}
			buffer.putInt(0x02);
			buffer.putInt(_teamTwo.size());
			for(PlayerInfo playerInfo : _teamTwo)
			{
				writeString(playerInfo._name, buffer);
				writeString(playerInfo._clanName, buffer);
				buffer.putInt(0x00);
				buffer.putInt(playerInfo._classId);
				buffer.putInt(playerInfo._damage);
				buffer.putInt(playerInfo._currentPoints);
				buffer.putInt(playerInfo._gamePoints);
				buffer.putInt(0x00);
			}
		}

		private static class PlayerInfo
		{
			private String _name, _clanName;
			private int _classId, _currentPoints, _gamePoints, _damage;

			public PlayerInfo(String name, String clanName, int classId, int currentPoints, int gamePoints, int damage)
			{
				_name = name;
				_clanName = clanName;
				_classId = classId;
				_currentPoints = currentPoints;
				_gamePoints = gamePoints;
				_damage = damage;
			}
		}
	}

	private int _type;

	public ExReceiveOlympiadPacket(int type)
	{
		_type = type;
	}

	@Override
	protected ServerPacketOpcodes getOpcodes()
	{
		return ServerPacketOpcodes.ExReceiveOlympiadPacket;
	}

	@Override
	protected void writeImpl(GameClient client, ByteBuffer buffer)
	{
		buffer.putInt(_type);
	}
}