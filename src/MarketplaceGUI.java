import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.HashSet;

/**
 * ===== MarketplaceGUI.java =====
 * Complete Swing GUI for CS180 Marketplace
 * - gradient background
 * - Purdue color theming
 * - validation dialogs
 * - live balance and greeting
 * - custom list renderers and button styling
 */
public class MarketplaceGUI {
    /* Connection/state */
    private ClientConnection conn;
    private String currentUser = null;
    private double balance = 0.0;

    /* Main frame and layout */
    private final JFrame frame = new JFrame("CS180 Marketplace Client #1");
    private final CardLayout cards = new CardLayout();
    private final JPanel center = new JPanel(cards) {
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            GradientPaint gp = new GradientPaint(0, 0, purdueGold, 0, getHeight(), Color.WHITE);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    };
    private final JLabel balLbl = new JLabel("Balance: $0.00");
    private final JLabel hiLbl = new JLabel();

    /* Panels */
    private ItemsPanel itemsPanel;
    private InventoryPanel invPanel;
    private MessagesPanel msgPanel;

    /* Colors */
    private static final Color purdueGold = new Color(206, 184, 136);
    private static final Color purdueBlack = new Color(0, 0, 0);

    public static void main(String[] args) {
        UIManager.put("Button.background", purdueBlack);
        UIManager.put("Button.foreground", purdueGold);
        SwingUtilities.invokeLater(MarketplaceGUI::new);
    }

    public MarketplaceGUI() {
        buildGUI();
    }

    private void buildGUI() {
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        center.add(new StartPanel(), "start");

        itemsPanel = new ItemsPanel();
        itemsPanel.setBackground(new Color(230,230,245));
        center.add(itemsPanel, "items");

        invPanel = new InventoryPanel();
        invPanel.setBackground(new Color(245,230,230));
        center.add(invPanel, "inventory");

        msgPanel = new MessagesPanel();
        msgPanel.setBackground(new Color(230,245,230));
        center.add(msgPanel, "messages");

        frame.add(center, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private class StartPanel extends JPanel implements ActionListener, ClientConnection.LineHandler {
        private final JTextField user = new JTextField(10);
        private final JPasswordField pass = new JPasswordField(10);
        private final JTextField start = new JTextField("100", 10);

        StartPanel() {
            setLayout(null);
            setBackground(purdueGold);
            setOpaque(true);

            JPanel userRow = createRow("Username", user);
            JPanel passRow = createRow("Password", pass);
            JPanel registerRow = createRow("Register", start);
            JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonRow.setBackground(purdueGold);

            JButton reg = new JButton("Register");
            JButton log = new JButton("Login");
            reg.addActionListener(this);
            log.addActionListener(this);
            styleButton(reg);
            styleButton(log);
            buttonRow.add(reg);
            buttonRow.add(log);
            add(userRow);
            add(passRow);
            add(registerRow);
            add(buttonRow);

            frame.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    layoutRows(userRow, passRow, registerRow, buttonRow);
                }
            });
        }

        private JPanel createRow(String labelText, JComponent field) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER));
            row.setBackground(purdueGold);
            JLabel label = new JLabel(labelText);
            label.setOpaque(true);
            label.setBackground(purdueBlack);
            label.setForeground(purdueGold);
            row.add(label);
            row.add(field);
            return row;
        }

        private void layoutRows(JPanel... rows) {
            Dimension size = frame.getContentPane().getSize();
            int pw = size.width/2;
            int ph = size.height/8;
            int gap = size.height/50;
            int y = (size.height - (rows.length*ph + (rows.length-1)*gap)) / 2;
            for (JPanel row : rows) {
                row.setBounds((size.width-pw)/2, y, pw, ph);
                y += ph + gap;
            }
            revalidate(); repaint();
        }

        @Override public void actionPerformed(ActionEvent e) {
            try {
                if (conn==null) conn = new ClientConnection(Client.HOST, Client.PORT, this);
                String u = user.getText().trim();
                String p = new String(pass.getPassword()).trim();
                if (e.getActionCommand().equals("Register"))
                    conn.send("register "+u+" "+p+" "+start.getText().trim());
                else conn.send("login "+u+" "+p);
            } catch(IOException ex) {
                JOptionPane.showMessageDialog(this, "Server unreachable:\n"+ex.getMessage());
            }
        }

        @Override public void onLine(String s) {
            if (s.startsWith("Login successful")) {
                currentUser = user.getText().trim();
                SwingUtilities.invokeLater(() -> {
                    buildNavbar(); cards.show(center,"items");
                });
                conn.send("getbalance"); conn.send("listitems");
            } else if (s.startsWith("User registered"))
                JOptionPane.showMessageDialog(this,"Registered – now press Login.");
            else if (s.contains("exists")||s.contains("Invalid"))
                JOptionPane.showMessageDialog(this,s);
            else if (s.startsWith("$")) {
                balance = Double.parseDouble(s.substring(1));
                balLbl.setText("BAL: "+s);
            }
            itemsPanel.acceptLine(s);
            invPanel.acceptLine(s);
            msgPanel.acceptLine(s);
        }
    }

    private void buildNavbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setBackground(purdueGold);
        String[][] tabs = {{"Items","items"},{"Inventory","inventory"},{"Messages","messages"}};
        for (String[] t:tabs) {
            JButton b = new JButton(t[0]); styleButton(b);
            b.addActionListener(e->{cards.show(center,t[1]); refresh(t[1]);});
            bar.add(b);
        }
        JButton logout=new JButton("Logout"), exit=new JButton("Exit");
        styleButton(logout); styleButton(exit);
        bar.add(logout); bar.add(exit);
        bar.add(Box.createHorizontalStrut(20));
        hiLbl.setFont(new Font("SansSerif",Font.BOLD,14)); hiLbl.setForeground(purdueBlack);
        hiLbl.setText("Hi, "+currentUser);
        bar.add(hiLbl); bar.add(Box.createHorizontalStrut(10)); bar.add(balLbl);
        logout.addActionListener(e->{if(conn!=null)conn.send("logout");resetPanels();frame.remove(bar);cards.show(center,"start");});
        exit.addActionListener(e->{if(conn!=null)conn.close();frame.dispose();});
        frame.add(bar,BorderLayout.NORTH); frame.revalidate();
    }

    private void refresh(String tab) {
        if(tab.equals("items")) {itemsPanel.reset(); conn.send("listitems");}
        if(tab.equals("inventory")) {invPanel.reset(); conn.send("myitems");}
        if(tab.equals("messages")) {msgPanel.reset(); conn.send("viewuserlist");}
        conn.send("getbalance");
    }

    private void resetPanels() {
        itemsPanel.reset(); invPanel.reset(); msgPanel.reset();
        balLbl.setText("BAL: $0.00"); hiLbl.setText(""); currentUser=null;
    }

    private void styleButton(JButton b) {
        b.setBackground(purdueBlack); b.setForeground(purdueGold);
        b.setOpaque(true); b.setBorderPainted(false);
    }

    private class ItemsPanel extends JPanel implements ActionListener {
        private final DefaultListModel<String> m=new DefaultListModel<>();
        private final JList<String> list=new JList<>(m);
        private final JTextField search=new JTextField(10);
        private final JTextField priceFld=new JTextField(5);
        ItemsPanel() {
            setLayout(new BorderLayout());
            list.setCellRenderer(new DefaultListCellRenderer(){
                @Override public Component getListCellRendererComponent(JList<?> l,Object v,int i,boolean sel,boolean f){
                    Component c=super.getListCellRendererComponent(l,v,i,sel,f);
                    if(!sel) c.setBackground((i%2==0)?new Color(250,250,255):Color.WHITE);
                    return c;
                }
            });
            add(new JScrollPane(list),BorderLayout.CENTER);
            JPanel south=new JPanel(); south.setBackground(getBackground());
            JButton searchBtn=new JButton("Search"), buyBtn=new JButton("Buy"), changeBtn=new JButton("Change"), msgBtn=new JButton("Msg"), addBtn=new JButton("Add");
            for(JButton b:new JButton[]{searchBtn,buyBtn,changeBtn,msgBtn,addBtn}) styleButton(b);
            searchBtn.addActionListener(e->conn.send("searchitem "+search.getText().trim()));
            buyBtn.setActionCommand("Buy"); changeBtn.setActionCommand("Change");
            buyBtn.addActionListener(this); changeBtn.addActionListener(this); msgBtn.addActionListener(this);
            addBtn.addActionListener(e->new ListItemDialog());
            south.add(new JLabel("Item")); south.add(search); south.add(searchBtn);
            south.add(buyBtn); south.add(priceFld); south.add(changeBtn); south.add(msgBtn); south.add(addBtn);
            add(south,BorderLayout.SOUTH);
        }
        void reset(){ SwingUtilities.invokeLater(m::clear); }
        void acceptLine(String s){ if(s.contains("Seller:")) SwingUtilities.invokeLater(()->m.addElement(s)); }
        @Override public void actionPerformed(ActionEvent e){ String sel=list.getSelectedValue(); if(sel==null)return;
            String[] p=sel.split(" - "); String item=p[0]; double pr=Double.parseDouble(p[1].substring(1)); String sl=p[2].split(":")[1].trim();
            switch(e.getActionCommand()){ case "Buy": if(sl.equals(currentUser)){if(JOptionPane.showConfirmDialog(this,"Remove your own item?","Confirm",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){conn.send("unsellitem "+item);reset();}}else if(pr>balance) JOptionPane.showMessageDialog(this,"Insufficient funds."); else conn.send("buy "+item); break;
                case "Change": try{double np=Double.parseDouble(priceFld.getText());conn.send("changeitemprice "+item+" "+np);}catch(Exception ignored){}break;
                default: msgPanel.openConversation(sl,true); cards.show(center,"messages"); }
        }
    }

    private class InventoryPanel extends JPanel implements ActionListener {
        private final DefaultListModel<String> m=new DefaultListModel<>();
        private final JList<String> list=new JList<>(m);
        private final JTextField priceFld=new JTextField(5);
        InventoryPanel() {
            setLayout(new BorderLayout());
            list.setCellRenderer(itemsPanel.list.getCellRenderer());
            add(new JScrollPane(list),BorderLayout.CENTER);
            JPanel south=new JPanel(); south.setBackground(getBackground());
            JButton sell=new JButton("Sell"), del=new JButton("Delete"), change=new JButton("Change");
            for(JButton b:new JButton[]{sell,del,change}) styleButton(b);
            sell.addActionListener(this); del.addActionListener(this); change.addActionListener(this);
            south.add(sell); south.add(del); south.add(priceFld); south.add(change);
            add(south,BorderLayout.SOUTH);
        }
        void reset(){ SwingUtilities.invokeLater(m::clear); }
        void acceptLine(String s){ if(s.contains(" - $")) SwingUtilities.invokeLater(()->m.addElement(s)); }
        @Override public void actionPerformed(ActionEvent e){ int i=list.getSelectedIndex(); if(i<0){JOptionPane.showMessageDialog(this,"Select item");return;} String item=list.getSelectedValue().split(" - ")[0]; String cmd=e.getActionCommand();
            switch(cmd){ case "Sell": conn.send("sellitem "+item); break; case "Delete": conn.send("deleteitem "+item); break; case "Change": try{double np=Double.parseDouble(priceFld.getText());conn.send("changeitemprice "+item+" "+np);}catch(Exception ignored){} break; }
        }
    }

    private class MessagesPanel extends JPanel implements ActionListener {
        private final DefaultListModel<String> um=new DefaultListModel<>();
        private final JList<String> users=new JList<>(um);
        private final DefaultListModel<String> cm=new DefaultListModel<>();
        private final JList<String> convo=new JList<>(cm);
        private final JTextField msgFld=new JTextField(20), usrFld=new JTextField(12);
        private String active=null;
        MessagesPanel() {
            setLayout(new BorderLayout());
            JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,makeUserPanel(),makeConvoPanel());
            add(split,BorderLayout.CENTER);
        }
        private JPanel makeUserPanel(){ JPanel p=new JPanel(new BorderLayout()); p.setBackground(getBackground());
            JPanel top=new JPanel(); styleButton(new JButton()); top.setBackground(getBackground()); top.add(new JLabel("Find:")); top.add(usrFld);
            JButton go=new JButton("Go"); styleButton(go); go.addActionListener(e->searchUser()); top.add(go); p.add(top,BorderLayout.NORTH);
            users.setCellRenderer(itemsPanel.list.getCellRenderer()); p.add(new JScrollPane(users),BorderLayout.CENTER);
            users.addListSelectionListener(e->{ if(!e.getValueIsAdjusting()) openConversation(users.getSelectedValue(),false); });
            return p;
        }
        private JPanel makeConvoPanel(){ JPanel p=new JPanel(new BorderLayout()); p.setBackground(getBackground());
            convo.setCellRenderer(users.getCellRenderer()); p.add(new JScrollPane(convo),BorderLayout.CENTER);
            JPanel bot=new JPanel(); bot.setBackground(getBackground()); bot.add(new JLabel("Msg:")); bot.add(msgFld);
            JButton send=new JButton("Send"); styleButton(send); send.addActionListener(this); bot.add(send); p.add(bot,BorderLayout.SOUTH);
            return p;
        }
        void reset(){ SwingUtilities.invokeLater(()->{um.clear();cm.clear();active=null;}); }
        void searchUser(){ String u=usrFld.getText().trim(); if(u.isEmpty()||u.equals(currentUser))return; openConversation(u,true); if(!um.contains(u)) JOptionPane.showMessageDialog(this,"No user."); }
        void openConversation(String u,boolean add){ if(u==null)return; active=u; cm.clear(); if(add&&!um.contains(u)) um.addElement(u); conn.send("viewconversation "+u); cards.show(center,"messages"); }
        void acceptLine(String s){ if(s.startsWith("No messaging")){ reset(); return;} if(s.startsWith("$"))return; if(!s.contains(" ")&&!s.equals(currentUser)){ if(!um.contains(s)) um.addElement(s); return;} if(active!=null&& (s.startsWith(currentUser+" ")||s.startsWith(active+" "))){ String[] p=s.split(" ",3); String msg="["+p[0]+"] → ["+p[1]+"]: "+p[2]; if(!cm.contains(msg)) SwingUtilities.invokeLater(()->cm.addElement(msg)); }}
        @Override public void actionPerformed(ActionEvent e){ String o=users.getSelectedValue(); if(o==null){JOptionPane.showMessageDialog(this,"Select user");return;} String m=msgFld.getText().trim(); if(m.isEmpty())return; conn.send("sendmessage "+o+" "+m); msgFld.setText(""); openConversation(o,false); }
    }

    private class ListItemDialog extends JDialog implements ActionListener {
        private final JTextField name=new JTextField(10), price=new JTextField(5);
        ListItemDialog(){ super(frame,"Add new item",true); setLayout(new GridLayout(3,2,5,5)); add(new JLabel("Item")); add(name); add(new JLabel("Price")); add(price); JButton ok=new JButton("Add"); styleButton(ok); ok.addActionListener(this); add(ok); pack(); setLocationRelativeTo(frame); setVisible(true); }
        @Override public void actionPerformed(ActionEvent e){ try{ double p=Double.parseDouble(price.getText()); if(p<=0){ JOptionPane.showMessageDialog(this,"Price must be >0"); return;} conn.send("additem "+name.getText().trim()+" "+price.getText().trim()); dispose(); }catch(Exception ex){ JOptionPane.showMessageDialog(this,"Invalid price"); }}
    }
}
