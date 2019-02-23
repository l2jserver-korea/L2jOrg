package org.l2j.scripts.npc.model.heavenlyrift;

import org.l2j.scripts.manager.HeavenlyRift;
import org.l2j.commons.collections.MultiValueSet;
import org.l2j.commons.util.Rnd;
import org.l2j.gameserver.instancemanager.ServerVariables;
import org.l2j.gameserver.model.Party;
import org.l2j.gameserver.model.Player;
import org.l2j.gameserver.model.instances.NpcInstance;
import org.l2j.gameserver.network.l2.components.SystemMsg;
import org.l2j.gameserver.network.l2.s2c.SystemMessagePacket;
import org.l2j.gameserver.templates.npc.NpcTemplate;

import java.util.StringTokenizer;

/**
 * @reworked by Bonux
 */
public class ArushinaiInstance extends NpcInstance
{
	private static final long serialVersionUID = 1L;

	public ArushinaiInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, "_");
		String cmd = st.nextToken();
		if(cmd.equals("proceed"))
		{
			if(!player.isGM())
			{
				Party party = player.getParty();
				if(party == null)
				{
					// TODO: Add message.
					player.teleToLocation(114264, 13352, -5104);
					return;
				}
				if(!party.isLeader(player))
				{
					// TODO: Add message.
					return;
				}
			}
			if(ServerVariables.getInt("heavenly_rift_complete", 0) == 0)
			{
				int riftLevel = Rnd.get(1, 3);
				ServerVariables.set("heavenly_rift_level", riftLevel);
				ServerVariables.set("heavenly_rift_complete", 4);
				switch(riftLevel) 
				{
					case 1:
						HeavenlyRift.startEvent20Bomb(player);
						break;
					case 2:
						HeavenlyRift.startEventTower(player);
						break;
					case 3:
						HeavenlyRift.startEvent40Angels(player);
						break;
					default:
						break;
				}
			}	
			else
				showBusyWindow(player);
		}
		else if(cmd.equals("finish"))
		{
			if(player.isInParty())
			{
				Party party = player.getParty();
				if(party.isLeader(player))
				{
					for(Player partyMember : party.getPartyMembers())
					{
						if(!player.isInRange(partyMember.getLoc(), 1000))
						{
							SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.C1_IS_IN_A_LOCATION_WHICH_CANNOT_BE_ENTERED_THEREFORE_IT_CANNOT_BE_PROCESSED);
							sm.addName(partyMember);
							party.broadcastToPartyMembers(player, sm);
							return;
						}
					}

					ServerVariables.set("heavenly_rift_reward", 0);
					//ServerVariables.set("heavenly_rift_complete", 0);
					for(Player partyMember : party.getPartyMembers())
						partyMember.teleToLocation(114264, 13352, -5104);
				}
				else
				{
					// TODO: Add message.
				}
			}
			else
			{
				if(player.isGM())
				{
					ServerVariables.set("heavenly_rift_complete", 0);
					player.teleToLocation(114264, 13352, -5104);
				}
				else
				{
					// TODO: Add message.
				}
			}
		}		
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public String getHtmlFilename(int val, Player player)
	{
		String filename = "";
		if(val == 1)
			filename = getNpcId() + "-1.htm";
		else if(ServerVariables.getInt("heavenly_rift_complete", 0) > 0)
			filename = getNpcId() + "-2.htm";
		else
			filename = getNpcId() + ".htm";
		return filename;
	}
}