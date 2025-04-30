/*  =====  MarketplaceGUI.java  =====
    Complete Swing GUI for ISO-Marketplace
    — negative / zero price validation
    — username-exists & bad-login dialogs
    — greeting + live balance
    — own-item confirmation, insufficient-funds check
    — clean, de-duplicated conversation view
    — Delete button now refreshes inventory on success
 */
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.HashSet;

/* ========================================================================= */
public class MarketplaceGUI {

    /* ---- connection / state ---- */
    private ClientConnection conn;
    private String  currentUser = null;
    private double  balance     = 0.0;

    /* ---- widgets ---- */
    private final JFrame  frame  = new JFrame("CS180 Marketplace Client #1");
    private final CardLayout cards = new CardLayout();
    private final JPanel  center   = new JPanel(cards);
    private final JLabel  balLbl   = new JLabel("Balance: $0.00");
    private final JLabel  hiLbl    = new JLabel();

    private ItemsPanel     itemsPanel;
    private InventoryPanel invPanel;
    private MessagesPanel  msgPanel;

    //Colors
    private static final Color purdueGold = new Color(206, 184, 136);
    private static final Color purdueBlack = new Color(0, 0, 0);

    /* ==================================================================== */
    public static void main(String[] args) {
        new MarketplaceGUI();
    }

    public MarketplaceGUI() {
        SwingUtilities.invokeLater(this::buildGUI);
    }

    /* ============================ GUI BUILD ============================ */
    private void buildGUI() {
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800,600);
        frame.setLocationRelativeTo(null);

        center.add(new StartPanel(), "start");

        itemsPanel = new ItemsPanel();
        center.add(itemsPanel,"items");
        invPanel   = new InventoryPanel();
        center.add(invPanel,"inventory");
        msgPanel   = new MessagesPanel();
        center.add(msgPanel,"messages");

        frame.add(center,BorderLayout.CENTER);
        frame.setVisible(true);
    }

    /* =========================== START PANEL ========================== */
    private class StartPanel extends JPanel implements ActionListener, ClientConnection.LineHandler {

        private final JTextField     user  = new JTextField(10);
        private final JPasswordField pass  = new JPasswordField(10);
        private final JTextField     start = new JTextField("100",10);

        StartPanel() {
            setLayout(null);
            setBackground(purdueGold);
            setOpaque(true);

            JPanel userRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
            userRow.setBackground(purdueGold);
            JLabel userName = new JLabel("Username");
            userName.setOpaque(true);
            userName.setBackground(purdueBlack);
            userName.setForeground(purdueGold);
            userRow.add(userName);
            userRow.add(user);
            add(userRow);

            JPanel passRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
            passRow.setBackground(purdueGold);
            JLabel passLabel = new JLabel("Password");
            passLabel.setOpaque(true);
            passLabel.setBackground(purdueBlack);
            passLabel.setForeground(purdueGold);
            passRow.add(passLabel);
            passRow.add(pass);
            add(passRow);

            JPanel registerRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
            registerRow.setBackground(purdueGold);
            JLabel registerLabel = new JLabel("Register");
            registerLabel.setOpaque(true);
            registerLabel.setBackground(purdueBlack);
            registerLabel.setForeground(purdueGold);
            registerRow.add(registerLabel);
            registerRow.add(start);
            add(registerRow);

            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER));
            row.setBackground(purdueGold);
            JButton reg = new JButton("Register");
            JButton log = new JButton("Login");
            reg.addActionListener(this);
            log.addActionListener(this);
            row.add(reg);
            row.add(log);
            add(row);

            frame.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    Dimension size = frame.getContentPane().getSize();
                    int panelWidth = (int)(size.width * 0.5); // 50% of frame width
                    int panelHeight = (int)(size.height * 0.12); // each row about 8% of frame height
                    int gap = (int)(size.height * 0.01); // gap = 2% of height

                    int totalHeight = 3 * panelHeight + 2 * gap + panelHeight; // 3 input rows + 2 gaps + button row

                    int startY = (size.height - totalHeight) / 2; // center vertically
                    int startX = (size.width - panelWidth) / 2;    // center horizontally

                    userRow.setBounds(startX, startY, panelWidth, panelHeight);
                    passRow.setBounds(startX, startY + panelHeight + gap, panelWidth, panelHeight);
                    registerRow.setBounds(startX, startY + 2 * (panelHeight + gap), panelWidth, panelHeight);
                    row.setBounds(startX, startY + 3 * (panelHeight + gap), panelWidth, panelHeight);

                    revalidate();
                    repaint();
                }
            });
        }


        @Override public void actionPerformed(ActionEvent e){
            try{
                if(conn==null)
                    conn = new ClientConnection(Client.HOST,Client.PORT,this);

                String u=user.getText().trim();
                String p=new String(pass.getPassword()).trim();

                if("Register".equals(e.getActionCommand()))
                    conn.send("register "+u+" "+p+" "+start.getText().trim());
                else
                    conn.send("login "+u+" "+p);
            }catch(IOException ex){
                JOptionPane.showMessageDialog(this,"Server unreachable:\n"+ex.getMessage());
            }
        }

        /* ==== async server lines ==== */
        @Override public void onLine(String s){
            if(s.startsWith("Login successful")){
                currentUser = user.getText().trim();
                SwingUtilities.invokeLater(()->{
                    buildNavbar();
                    cards.show(center,"items");
                });
                conn.send("getbalance"); conn.send("listitems");
            }
            else if(s.startsWith("User registered"))
                JOptionPane.showMessageDialog(this,"Registered – now press Login.");
            else if(s.startsWith("Username already exists"))
                JOptionPane.showMessageDialog(this,"That username is taken.");
            else if(s.startsWith("Invalid credentials"))
                JOptionPane.showMessageDialog(this,"Incorrect username or password.");
            else if(s.startsWith("$")){
                try{ balance = Double.parseDouble(s.substring(1)); }catch(NumberFormatException ignored){}
                balLbl.setText("BAL: "+s);
            }

            itemsPanel.acceptLine(s);
            invPanel  .acceptLine(s);
            msgPanel  .acceptLine(s);
        }
    }

    /* ============================ NAV BAR ============================= */
    private void buildNavbar(){
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setBackground(purdueGold);
        String[][] tabs={{"Items","items"},{"Inventory","inventory"},{"Messages","messages"}};
        for(String[] t: tabs){
            JButton b=new JButton(t[0]);
            String card=t[1];
            b.addActionListener(e->{ cards.show(center,card); refresh(card); });
            bar.add(b);
        }

        JButton logout=new JButton("Logout"), exit=new JButton("Exit");
        bar.add(logout); bar.add(exit);
        bar.add(Box.createHorizontalStrut(20));
        hiLbl.setText("Hi, "+currentUser); bar.add(hiLbl);
        bar.add(Box.createHorizontalStrut(10)); bar.add(balLbl);

        logout.addActionListener(e->{
            if(conn!=null) conn.send("logout");
            resetPanels();
            frame.remove(bar); frame.revalidate(); frame.repaint();
            cards.show(center,"start");
        });
        exit.addActionListener(e->{ if(conn!=null) conn.close(); frame.dispose(); });

        frame.add(bar,BorderLayout.NORTH); frame.revalidate();
    }

    private void refresh(String tab){
        switch(tab){
            case "items"    -> { itemsPanel.reset(); conn.send("listitems"); }
            case "inventory"-> { invPanel.reset();  conn.send("myitems");  }
            case "messages" -> { msgPanel.reset(); conn.send("viewuserlist");}
        }
        conn.send("getbalance");
    }

    private void resetPanels(){
        itemsPanel.reset(); invPanel.reset(); msgPanel.reset();
        balLbl.setText("BAL: $0.00"); hiLbl.setText("");
        currentUser=null;
    }

    /* ============================= ITEMS =============================== */
    private class ItemsPanel extends JPanel implements ActionListener{
        private final DefaultListModel<String> m=new DefaultListModel<>();
        private final JList<String> list=new JList<>(m);
        private final JTextField search=new JTextField(10);
        private final JTextField newPrice = new JTextField(5);

        ItemsPanel(){
            setLayout(new BorderLayout());
            add(new JScrollPane(list),BorderLayout.CENTER);
            setBackground(purdueGold);

            JPanel south=new JPanel();
            south.setBackground(purdueGold);
            JButton searchBtn=new JButton("Search"),
                    buyBtn   =new JButton("Buy/Unsell"),
                    changeBtn=new JButton("Change Price"),
                    msgBtn   =new JButton("Message"),
                    listBtn  =new JButton("Add Item");

            buyBtn.setActionCommand("Buy");
            changeBtn.setActionCommand("Change");

            searchBtn.addActionListener(e->conn.send("searchitem "+search.getText().trim()));
            buyBtn.addActionListener(this);
            changeBtn.addActionListener(this);
            msgBtn.addActionListener(this);
            listBtn.addActionListener(e->new ListItemDialog());

            south.add(new JLabel("Item")); south.add(search); south.add(searchBtn);
            south.add(buyBtn); south.add(newPrice); south.add(changeBtn); south.add(msgBtn); south.add(listBtn);
            add(south,BorderLayout.SOUTH);
        }

        void reset(){ SwingUtilities.invokeLater(m::clear); }

        void acceptLine(String s){
            if(s.contains("Seller:"))
                SwingUtilities.invokeLater(()->m.addElement(s));

            else if(s.startsWith("Transaction processed")){
                JOptionPane.showMessageDialog(this,"Purchase complete!");
                refresh("items");
            }
            else if(s.startsWith("Item removed from sale."))
                JOptionPane.showMessageDialog(this,"Item removed from market.");
            else if(s.startsWith("Invalid cost") || s.startsWith("Invalid price"))
                JOptionPane.showMessageDialog(this,"Invalid price.");
            else if(s.startsWith("Found item:"))
                SwingUtilities.invokeLater(()->JOptionPane.showMessageDialog(this,s));
        }

        @Override public void actionPerformed(ActionEvent e){
            String sel=list.getSelectedValue(); if(sel==null) return;

            String[] parts=sel.split(" - ");
            String item   = parts[0];
            double price  = Double.parseDouble(parts[1].substring(1));
            String seller = parts[2].substring("Seller:".length()).trim();

            switch(e.getActionCommand()){
                case "Buy" -> handleBuy(item,price,seller);
                case "Message" -> {
                    msgPanel.openConversation(seller,true);
                    cards.show(center,"messages");
                }
                case "Change" -> {
                    try {
                        double changePrice = Double.parseDouble(newPrice.getText().trim());
                        conn.send("changeitemprice " + item + " " + changePrice);
                        refresh("items");
                        JOptionPane.showMessageDialog(this, "Your " + item + " price " +
                                "is now $" + changePrice, "Change", JOptionPane.INFORMATION_MESSAGE);
                    } catch (NumberFormatException ignored){
                        //ignored
                    }
                }
            }
        }

        private void handleBuy(String item,double price,String seller){
            if(seller.equals(currentUser)){
                int ans=JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to take your own item, \""+item+
                                "\", off the market?","Confirm",
                        JOptionPane.YES_NO_OPTION);
                if(ans==JOptionPane.YES_OPTION) {
                    conn.send("unsellitem "+item);
                    refresh("items");
                }
                return;
            }
            if(price>balance){
                JOptionPane.showMessageDialog(this,"Insufficient funds.");
                return;
            }
            conn.send("buy "+item);
        }
    }

    /* =========================== INVENTORY ============================= */
    private class InventoryPanel extends JPanel implements ActionListener{
        private final DefaultListModel<String> m=new DefaultListModel<>();
        private final JList<String> list=new JList<>(m);
        private final JTextField price=new JTextField(5);

        InventoryPanel(){
            setBackground(purdueGold);
            setLayout(new BorderLayout());
            add(new JScrollPane(list),BorderLayout.CENTER);

            JPanel south=new JPanel();
            south.setBackground(purdueGold);
            JButton sell=new JButton("Sell"), del=new JButton("Delete");
            JButton changePrice=new JButton("Change");
            sell.addActionListener(this); del.addActionListener(this);
            changePrice.addActionListener(this);
            south.add(sell); south.add(del); south.add(price); south.add(changePrice);
            add(south,BorderLayout.SOUTH);
        }

        void reset(){ SwingUtilities.invokeLater(m::clear); }

        void acceptLine(String s){
            if(s.contains(" - $")){
                SwingUtilities.invokeLater(()->m.addElement(s));
                return;
            }

            if(s.startsWith("Item listed for sale")){
                JOptionPane.showMessageDialog(this,"Item listed successfully!");
                refresh("inventory");
            } else if(s.startsWith("Item added to inventory")){
                JOptionPane.showMessageDialog(this,"Item added to inventory.");
                refresh("inventory");
            } else if(s.startsWith("Item deleted")){
                JOptionPane.showMessageDialog(this,"Item deleted.");
                refresh("inventory");          // **force full refresh**
            } else if(s.startsWith("Item not found") || s.startsWith("You can only delete")){
                JOptionPane.showMessageDialog(this,s);
            }
        }

        @Override public void actionPerformed(ActionEvent e){
            int idx=list.getSelectedIndex();
            if(idx==-1){ JOptionPane.showMessageDialog(this,"Select an item."); return; }

            String itemName=list.getSelectedValue().split(" - ")[0];

            if("Sell".equals(e.getActionCommand()))
                conn.send("sellitem "+itemName);
            if("Delete".equals(e.getActionCommand()))
                conn.send("deleteitem "+itemName);
            if("Change".equals(e.getActionCommand()))
                try {
                    double finalPriceChange = Double.parseDouble(price.getText());
                    conn.send("changeitemprice "+ itemName + " " + finalPriceChange);
                    refresh("inventory");
                    JOptionPane.showMessageDialog(this, "Your " + itemName + " price " +
                            "is now $" + finalPriceChange, "Change", JOptionPane.INFORMATION_MESSAGE);
                } catch (NumberFormatException ignored){
                    //ignored
                }
        }
    }

    /* ============================ MESSAGES ============================= */
    private class MessagesPanel extends JPanel implements ActionListener{
        private final DefaultListModel<String> usersM=new DefaultListModel<>();
        private final JList<String> users=new JList<>(usersM);

        private final DefaultListModel<String> convoM=new DefaultListModel<>();
        private final JList<String> convo=new JList<>(convoM);
        private final HashSet<String> convoLines = new HashSet<>();

        private final JTextField msgField   = new JTextField(20);
        private final JTextField searchUser = new JTextField(12);
        private String activeOther=null;

        MessagesPanel(){
            setBackground(purdueGold);
            setLayout(new BorderLayout());

            JPanel left=new JPanel(new BorderLayout());
            JPanel searchRow=new JPanel(new FlowLayout());
            searchRow.setBackground(purdueGold);
            searchRow.add(new JLabel("Search")); searchRow.add(searchUser);
            JButton find=new JButton("Go"); find.addActionListener(e->searchForUser());
            searchRow.add(find);
            left.add(searchRow,BorderLayout.NORTH);
            left.add(new JScrollPane(users),BorderLayout.CENTER);

            JPanel right=new JPanel(new BorderLayout());
            right.add(new JScrollPane(convo),BorderLayout.CENTER);
            JPanel send=new JPanel();
            send.setBackground(purdueGold);
            send.add(new JLabel("Message")); send.add(msgField);
            JButton sendBtn=new JButton("Send"); sendBtn.addActionListener(this);
            send.add(sendBtn);
            right.add(send,BorderLayout.SOUTH);

            JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,left,right);
            add(split,BorderLayout.CENTER);

            users.addListSelectionListener(e->{
                if(!e.getValueIsAdjusting()) openConversation(users.getSelectedValue(),false);
            });
        }

        void reset(){ SwingUtilities.invokeLater(()->{
            usersM.clear(); convoM.clear(); convoLines.clear(); activeOther=null;
        }); }

        private void searchForUser(){
            String target=searchUser.getText().trim();
            if(target.isEmpty() || target.equals(currentUser)) return;
            openConversation(target,true);
            if(!usersM.contains(target))
                JOptionPane.showMessageDialog(this,"User not found.");
        }

        void openConversation(String other, boolean forceAdd){
            if(other==null) return;
            activeOther=other;
            convoM.clear(); convoLines.clear();
            if(forceAdd && !usersM.contains(other)) usersM.addElement(other);
            conn.send("viewconversation "+other);
        }

        void acceptLine(String s){
            if(s.startsWith("$")) return;

            if("No messaging history.".equals(s))
                SwingUtilities.invokeLater(()->{ usersM.clear(); convoM.clear(); });

            else if(!s.contains(" ") && !s.equals(currentUser))
                SwingUtilities.invokeLater(()->{ if(!usersM.contains(s)) usersM.addElement(s); });

            else if(activeOther!=null &&
                    (s.startsWith(currentUser+" ")||s.startsWith(activeOther+" "))){
                String[] p=s.split(" ",3);
                String pretty="["+p[0]+"] → ["+p[1]+"]: "+p[2];
                if(convoLines.add(pretty))
                    SwingUtilities.invokeLater(()->convoM.addElement(pretty));
            }
        }

        @Override public void actionPerformed(ActionEvent e){
            String other=users.getSelectedValue();
            if(other==null){ JOptionPane.showMessageDialog(this,"Select a user."); return; }
            String msg=msgField.getText().trim(); if(msg.isEmpty()) return;
            conn.send("sendmessage "+other+" "+msg);
            msgField.setText("");
            conn.send("viewconversation "+other);
        }
    }

    /* ===================== LIST-NEW-ITEM DIALOG ======================= */
    private class ListItemDialog extends JDialog implements ActionListener{
        private final JTextField name=new JTextField(10),
                price=new JTextField(5);
        ListItemDialog(){
            super(frame,"Add new item",true);
            setLayout(new GridLayout(3,2,5,5));
            add(new JLabel("Item"));  add(name);
            add(new JLabel("Price")); add(price);
            JButton ok=new JButton("Add"); ok.addActionListener(this); add(ok);
            pack(); setLocationRelativeTo(frame); setVisible(true);
        }
        @Override public void actionPerformed(ActionEvent e){
            String priceTxt=price.getText().trim();
            double p;
            try{ p=Double.parseDouble(priceTxt); }
            catch(NumberFormatException ex){
                JOptionPane.showMessageDialog(this,"Invalid price.");
                return;
            }
            if(p<=0){
                JOptionPane.showMessageDialog(this,"Price must be positive.");
                return;
            }
            conn.send("additem "+name.getText().trim()+" "+priceTxt);
            dispose();
        }
    }
}
