package org.l2j.gameserver.network.authcomm.as2gs;

import org.l2j.gameserver.model.Player;
import org.l2j.gameserver.network.authcomm.AuthServerCommunication;
import org.l2j.gameserver.network.authcomm.ReceivablePacket;
import org.l2j.gameserver.network.l2.GameClient;
import org.l2j.gameserver.network.l2.components.SystemMsg;
import org.l2j.gameserver.network.l2.s2c.ServerCloseSocketPacket;

import java.nio.ByteBuffer;

public class KickPlayer extends ReceivablePacket
{
    private String account;

    @Override
    public void readImpl(ByteBuffer buffer)
    {
        account = readString(buffer);
    }

    @Override
    protected void runImpl()
    {
        GameClient client = AuthServerCommunication.getInstance().removeWaitingClient(account);
        if(client == null)
            client = AuthServerCommunication.getInstance().removeAuthedClient(account);
        if(client == null)
            return;

        Player activeChar = client.getActiveChar();
        if(activeChar != null)
        {
            //FIXME [G1ta0] сообщение чаще всего не показывается, т.к. при закрытии соединения очередь на отправку очищается
            activeChar.sendPacket(SystemMsg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);
            activeChar.kick();
        }
        else
        {
            client.close(ServerCloseSocketPacket.STATIC);
        }
    }
}