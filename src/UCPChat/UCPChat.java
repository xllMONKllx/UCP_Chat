package UCPChat;

//importera de bibliotek som behövs för att programmet ska fungera.
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

public class UCPChat extends JFrame //Startar programmet
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

    //Multicast egenskaper(använder port 8801 och ip-adressen, userNickname)
    private int port = 8801;
    private NetworkInterface networkInterface = NetworkInterface.getByName("5e");
    private InetAddress iadr = InetAddress.getByName("239.255.255.255");
    private String userNickname;
    private InetSocketAddress inetSocketAddress;
    private MulticastSocket socket;

    public UCPChat() throws IOException
    {
        TypeUserNickname();

        //Egenskaper till UCPChat fönster
        setTitle("UCP Chat Program"); //Fönsters namn
        setSize(2000, 2000); //längd och hög på fönster
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE); //avslutar programmet
        setVisible(true); //Gör chat fönster synlig

        //Functioner som sätter upp panelen och ger den funkualitet
        SetupPanel();
        AddChatListeners();

        //Skapar multicast socket
        socket = new MulticastSocket(this.port);
        inetSocketAddress = new InetSocketAddress(this.iadr, this.port);
        socket.joinGroup(inetSocketAddress, this.networkInterface);

        //Gör UCPChat tillgänlig för ChatListener
        var chatListener = new ChatListener(this);

        //Startar ChatListener
        chatListener.start();

        //Skickar bekräftelse till chatten att användaren loggat in till chatten
        SendMessage("%s har loggat in.\n".formatted(userNickname));
    }

    private void TypeUserNickname()
    {
        //Ber användaren att skriva in sitt smäknamn för att logga in till chatten
        this.userNickname = JOptionPane.showInputDialog(null, "Skriv in dit smeknamnn: ");
    }

    //funktion hämtar lokal tid som används i programmet för att vissa när användaren skickat meddellande till chatten
    private String GetTime()
    {
        return LocalDateTime.now().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM));
    }

    //Sätter chat fönster med bestämd layout och möjlighet att skrolla både vertikalt och horizontalt
    private void SetupPanel()
    {
        this.chatPanel = new JPanel();
        this.chatPanel.setLayout(new BorderLayout());
        this.chatPanel.add(closeProgram, BorderLayout.NORTH);
        this.chatPanel.add(new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS), BorderLayout.CENTER);
        this.chatPanel.add(textField, BorderLayout.SOUTH);

        this.add(chatPanel);
    }

    //Funktioner till ChatListener
    public void AddChatListeners()
    {
        //funktion som meddelar att användaren har lämnat chatten
        closeProgram.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                SendMessage("\n %s har lämnat chatten".formatted(GetTime(), userNickname));
            }
        });

        //funktion som hämtar texten som användaren har skrivit och visar meddelande i gemensamma chat fönster
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

    //updaterar chat huvud fönstret med de nya meddelande
    public void UpdateTextArea(String msg)
    {
        textArea.append(msg);
    }

    //funktion som hanterar meddelande(man kan även skriva på svenska)
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
