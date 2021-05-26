package UCPChat;

//importerar de bibliotek som behövs för att programmet ska fungera.
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class UCPChat extends JFrame
{

    public static void main(String[] args) throws UnknownHostException, IOException
    {
        UCPChat main = new UCPChat();
    }

    //Skapar JFrame med Chat fönster med huvud chaten och text fälten som användaren kan skriva in sina meddelande
    private JPanel chatPanel;
    private JButton closeProgram = new JButton("Klick här om du vill avsliuta programmet");
    private JTextArea textArea = new JTextArea();
    private JTextField textField = new JTextField("");

    private int port = 8801;
    private NetworkInterface networkInterface = NetworkInterface.getByName("5e");
    private InetAddress iadr = InetAddress.getByName("239.255.255.255");
    private String userNickname;
    private InetSocketAddress inetSocketAddress;
    private MulticastSocket socket;

    public UCPChat() throws IOException
    {
        TypeUserNickname();

        setTitle("UCP Chat Program");
        setSize(2000, 2000);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

        SetupPanel();
        AddChatListeners();

        socket = new MulticastSocket(this.port);
        inetSocketAddress = new InetSocketAddress(this.iadr, this.port);
        socket.joinGroup(inetSocketAddress, this.networkInterface);

        var chatListener = new ChatListener(this);

        chatListener.start();

        SendMessage("%s har loggat in.\n".formatted(userNickname));
    }

    private void TypeUserNickname()
    {
        this.userNickname = JOptionPane.showInputDialog(null, "Skriv in dit smeknamnn: ");
    }

    private String GetTime()
    {
        return LocalDateTime.now().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM));
    }

    private void SetupPanel()
    {
        this.chatPanel = new JPanel();
        this.chatPanel.setLayout(new BorderLayout());
        this.chatPanel.add(closeProgram, BorderLayout.NORTH);
        this.chatPanel.add(new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS), BorderLayout.CENTER);
        this.chatPanel.add(textField, BorderLayout.SOUTH);

        this.add(chatPanel);
    }

    public void AddChatListeners()
    {
        closeProgram.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                SendMessage("\n %s har lämnat chatten".formatted(GetTime(), userNickname));
            }
        });

        textField.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(textField.getText().equalsIgnoreCase(""))
                {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                textField.getText();
                SendMessage("\n%s %s \n%s\n".formatted(userNickname, GetTime(), textField.getText()));
                textField.setText("");
            }
        });
    }

    public void UpdateTextArea(String msg)
    {
        textArea.append(msg);
    }

    public void SendMessage(String message)
    {
        DatagramPacket packet = new DatagramPacket(message.getBytes(StandardCharsets.UTF_8), message.length(), iadr, port);

        try
        {
            socket.send(packet);
        }
        catch(IOException ioException)
        {
            ioException.printStackTrace();
        }
    }
}
