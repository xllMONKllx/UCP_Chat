package UCPChat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ChatListener extends Thread
{
    private int port = 8801;
    private InetAddress iadr = InetAddress.getByName("239.255.255.255");
    MulticastSocket socket;
    UCPChat ucpChat;

    public ChatListener(UCPChat ucpChat) throws IOException
    {
        this.ucpChat = ucpChat;
        socket = new MulticastSocket(port);
        socket.joinGroup(iadr);
        this.ucpChat = this.ucpChat;
    }

    @Override
    public void run()
    {
        while(true)
        {
            byte[] data = new byte[512];
            DatagramPacket packet = new DatagramPacket(data, data.length);

            try
            {
                socket.receive(packet);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            String message = new String(packet.getData(), 0, packet.getLength());

            ucpChat.UpdateTextArea(message);
        }
    }
}