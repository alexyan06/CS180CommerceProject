/**
 * ===== MarketplaceGUI.java =====
 * Complete Swing GUI for ISO-Marketplace
 * - negative / zero price validation
 * - username-exists & bad-login dialogs
 * - greeting + live balance
 * - own-item confirmation, insufficient-funds check
 * - clean, de-duplicated conversation view
 * - Delete button now refreshes inventory on success
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
    private ClientConnection conn;        // active connection to the server
    private String currentUser = null;    // currently logged-in username
    private double balance = 0.0;         // current user balance

    /* ---- widgets ---- */
    private final JFrame frame = new JFrame("CS180 Marketplace Client #1");  // main window
    private final CardLayout cards = new CardLayout();                       // layout for switching panels
    private final JPanel center = new JPanel(cards);                         // container for panels
    private final JLabel balLbl = new JLabel("Balance: $0.00");              // label showing balance
    private final JLabel hiLbl = new JLabel();                               // label for greeting

    private ItemsPanel itemsPanel;                                           // panel for marketplace
    private InventoryPanel invPanel;                                         // panel for inventory
    private MessagesPanel msgPanel;                                          // panel for messages

    // Colors for Purdue theme
    private static final Color purdueGold = new Color(206, 184, 136);         // gold accent color
    private static final Color purdueBlack = new Color(0, 0, 0);              // black accent color

    /* ==================================================================== */
    public static void main(String[] args) {
        new MarketplaceGUI();  // launch GUI
    }

    public MarketplaceGUI() {
        SwingUtilities.invokeLater(this::buildGUI);  // ensure GUI is built on EDT
    }

    /* ============================ GUI BUILD ============================ */
    private void buildGUI() {
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);                      // initial window size
        frame.setLocationRelativeTo(null);            // center on screen

        center.add(new StartPanel(), "start");       // login/register panel

        itemsPanel = new ItemsPanel();
        center.add(itemsPanel, "items");             // marketplace view
        invPanel = new InventoryPanel();
        center.add(invPanel, "inventory");           // inventory view
        msgPanel = new MessagesPanel();
        center.add(msgPanel, "messages");            // messaging view

        frame.add(center, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    /* =========================== START PANEL ========================== */
    private class StartPanel extends JPanel implements ActionListener, ClientConnection.LineHandler {

        private final JTextField user = new JTextField(10);             // input for username
        private final JPasswordField pass = new JPasswordField(10);     // input for password
        private final JTextField start = new JTextField("100", 10);    // input for initial balance

        StartPanel() {
            setLayout(null);
            setBackground(purdueGold);
            setOpaque(true);

            JPanel userRow = new JPanel(new FlowLayout(FlowLayout.CENTER));  // wrapper for username row
            userRow.setBackground(purdueGold);
            JLabel userName = new JLabel("Username");                     // label for username field
            userName.setOpaque(true);
            userName.setBackground(purdueBlack);
            userName.setForeground(purdueGold);
            userRow.add(userName);
            userRow.add(user);
            add(userRow);

            JPanel passRow = new JPanel(new FlowLayout(FlowLayout.CENTER));  // wrapper for password row
            passRow.setBackground(purdueGold);
            JLabel passLabel = new JLabel("Password");                    // label for password field
            passLabel.setOpaque(true);
            passLabel.setBackground(purdueBlack);
            passLabel.setForeground(purdueGold);
            passRow.add(passLabel);
            passRow.add(pass);
            add(passRow);

            JPanel registerRow = new JPanel(new FlowLayout(FlowLayout.CENTER));  // wrapper for balance input
            registerRow.setBackground(purdueGold);
            JLabel registerLabel = new JLabel("Register");                  // label for balance field
            registerLabel.setOpaque(true);
            registerLabel.setBackground(purdueBlack);
            registerLabel.setForeground(purdueGold);
            registerRow.add(registerLabel);
            registerRow.add(start);
            add(registerRow);

            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER));     // wrapper for buttons
            row.setBackground(purdueGold);
            JButton reg = new JButton("Register");                        // register button
            JButton log = new JButton("Login");                           // login button
            reg.addActionListener(this);
            log.addActionListener(this);
            row.add(reg);
            row.add(log);
            add(row);

            frame.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    Dimension size = frame.getContentPane().getSize();  // get current content size
                    int panelWidth = (int)(size.width * 0.5);           // width of each row panel
                    int panelHeight = (int)(size.height * 0.12);        // height of each row panel
                    int gap = (int)(size.height * 0.01);                // vertical gap between rows

                    int totalHeight = 3 * panelHeight + 2 * gap + panelHeight;  // total height of all rows + gap

                    int startY = (size.height - totalHeight) / 2;         // starting Y for first row
                    int startX = (size.width - panelWidth) / 2;           // starting X for panels

                    userRow.setBounds(startX, startY, panelWidth, panelHeight);
                    passRow.setBounds(startX, startY + panelHeight + gap, panelWidth, panelHeight);
                    registerRow.setBounds(startX, startY + 2 * (panelHeight + gap), panelWidth, panelHeight);
                    row.setBounds(startX, startY + 3 * (panelHeight + gap), panelWidth, panelHeight);

                    revalidate();
                    repaint();
                }
            });
        }

        @Override public void actionPerformed(ActionEvent e) {
            try {
                if (conn == null)
                    conn = new ClientConnection(Client.HOST, Client.PORT, this);  // connect to server

                String u = user.getText().trim();                              // username text
                String p = new String(pass.getPassword()).trim();               // password text

                if ("Register".equals(e.getActionCommand()))
                    conn.send("register " + u + " " + p + " " + start.getText().trim());
                else
                    conn.send("login " + u + " " + p);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Server unreachable:\n" + ex.getMessage());
            }
        }

        /* ==== async server lines ==== */
        @Override public void onLine(String s) {
            if (s.startsWith("Login successful")) {
                currentUser = user.getText().trim();                            // set current user
                SwingUtilities.invokeLater(() -> {
                    buildNavbar();
                    cards.show(center, "items");                                // switch to items panel
                });
                conn.send("getbalance"); conn.send("listitems");            // request initial data
            }
            else if (s.startsWith("User registered"))
                JOptionPane.showMessageDialog(this, "Registered – now press Login.");
            else if (s.startsWith("Username already exists"))
                JOptionPane.showMessageDialog(this, "That username is taken.");
            else if (s.startsWith("Invalid credentials"))
                JOptionPane.showMessageDialog(this, "Incorrect username or password.");
            else if (s.startsWith("$")) {
                try { balance = Double.parseDouble(s.substring(1)); } catch (NumberFormatException ignored) {}
                balLbl.setText("BAL: " + s);                                   // update balance display
            }

            itemsPanel.acceptLine(s);
            invPanel.acceptLine(s);
            msgPanel.acceptLine(s);
        }
    }

    /* ============================ NAV BAR ============================= */
    private void buildNavbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));            // navigation bar panel
        bar.setBackground(purdueGold);
        String[][] tabs = {{"Items","items"},{"Inventory","inventory"},{"Messages","messages"}};
        for (String[] t : tabs) {
            JButton b = new JButton(t[0]);                                    // tab button
            String card = t[1];
            b.addActionListener(e -> { cards.show(center, card); refresh(card); });
            bar.add(b);
        }

        JButton logout = new JButton("Logout"), exit = new JButton("Exit");
        bar.add(logout); bar.add(exit);
        bar.add(Box.createHorizontalStrut(20));
        hiLbl.setText("Hi, " + currentUser); bar.add(hiLbl);               // greeting label
        bar.add(Box.createHorizontalStrut(10)); bar.add(balLbl);            // balance label

        logout.addActionListener(e -> {
            if (conn != null) conn.send("logout");
            resetPanels();
            frame.remove(bar); frame.revalidate(); frame.repaint();
            cards.show(center, "start");
        });
        exit.addActionListener(e -> { if (conn != null) conn.close(); frame.dispose(); });

        frame.add(bar, BorderLayout.NORTH); frame.revalidate();
    }

    private void refresh(String tab) {
        switch(tab) {
            case "items"    -> { itemsPanel.reset(); conn.send("listitems"); }
            case "inventory"-> { invPanel.reset();  conn.send("myitems");   }
            case "messages" -> { msgPanel.reset(); conn.send("viewuserlist");}
        }
        conn.send("getbalance");                                            // refresh balance
    }

    private void resetPanels() {
        itemsPanel.reset(); invPanel.reset(); msgPanel.reset();
        balLbl.setText("BAL: $0.00"); hiLbl.setText("");                 // clear UI state
        currentUser = null;
    }
