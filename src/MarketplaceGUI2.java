import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.HashSet;

/**
 * ===== MarketplaceGUI2.java =====
 * Complete Swing GUI for CS180 Marketplace Client #2
 * <p>
 * Features:
 * - Gradient background for main panel
 * - Purdue color theming via UIManager and custom colors
 * - Validation dialogs for login and item operations
 * - Live balance and personalized greeting
 * - Custom list renderers for alternating row colors
 * - Styled buttons to match theme
 * </p>
 */
public class MarketplaceGUI2 {
    /** Connection to the server, initialized on first request */
    private ClientConnection conn;
    /** Currently logged-in username, or null if none */
    private String currentUser = null;
    /** Current user's balance, updated on each server response */
    private double balance = 0.0;

    /** Main application window */
    private final JFrame frame = new JFrame("CS180 Marketplace Client #2");
    /** Layout manager for swapping between panels */
    private final CardLayout cards = new CardLayout();
    /** Container panel that holds Start, Items, Inventory, and Messages panels */
    private final JPanel center = new JPanel(cards) {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Vertical gradient from purdueGold to white
            Graphics2D g2 = (Graphics2D) g;
            GradientPaint gp = new GradientPaint(0, 0, purdueGold, 0, getHeight(), Color.WHITE);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    };
    /** Label displaying the user's balance */
    private final JLabel balLbl = new JLabel("Balance: $0.00");
    /** Label displaying the greeting with username */
    private final JLabel hiLbl = new JLabel();

    /** Panel showing marketplace items */
    private ItemsPanel itemsPanel;
    /** Panel showing user's inventory */
    private InventoryPanel invPanel;
    /** Panel for direct messaging */
    private MessagesPanel msgPanel;

    /** Purdue gold accent color */
    private static final Color purdueGold = new Color(206, 184, 136);
    /** Purdue black accent color */
    private static final Color purdueBlack = new Color(0, 0, 0);

    /**
     * Application entry point: launch GUI on EDT
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MarketplaceGUI2::new);
    }

    /**
     * Constructor: sets up GUI theme and builds components
     */
    public MarketplaceGUI2() {
        // Apply button theming
        UIManager.put("Button.background", purdueBlack);
        UIManager.put("Button.foreground", purdueGold);
        buildGUI();
    }

    /**
     * Builds the main GUI: frame settings and panel initialization
     */
    private void buildGUI() {
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        // Start panel for login/registration
        center.add(new StartPanel(), "start");

        // Marketplace items view with lavender accent
        itemsPanel = new ItemsPanel();
        itemsPanel.setBackground(new Color(230, 230, 245));
        center.add(itemsPanel, "items");

        // Inventory view with rose accent
        invPanel = new InventoryPanel();
        invPanel.setBackground(new Color(245, 230, 230));
        center.add(invPanel, "inventory");

        // Messages view with mint accent
        msgPanel = new MessagesPanel();
        msgPanel.setBackground(new Color(230, 245, 230));
        center.add(msgPanel, "messages");

        frame.add(center, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    /**
     * Inner panel for login and registration, handles server responses
     */
    private class StartPanel extends JPanel implements ActionListener, ClientConnection.LineHandler {
        private final JTextField user = new JTextField(10);           // Username input
        private final JPasswordField pass = new JPasswordField(10);   // Password input
        private final JTextField start = new JTextField("100", 10);  // Initial balance input

        StartPanel() {
            setLayout(null);
            setBackground(purdueGold);
            setOpaque(true);

            // Create and position rows
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

            // Adjust layout on resize
            frame.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    layoutRows(userRow, passRow, registerRow, buttonRow);
                }
            });
        }

        /** Helper to create a label-input row */
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

        /** Helper to position rows centered in the frame */
        private void layoutRows(JPanel... rows) {
            Dimension size = frame.getContentPane().getSize();
            int pw = size.width / 2;
            int ph = size.height / 8;
            int gap = size.height / 50;
            int y = (size.height - (rows.length * ph + (rows.length - 1) * gap)) / 2;
            for (JPanel row : rows) {
                row.setBounds((size.width - pw) / 2, y, pw, ph);
                y += ph + gap;
            }
            revalidate();
            repaint();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (conn == null)
                    conn = new ClientConnection(Client.HOST, Client.PORT, this);
                String u = user.getText().trim();
                String p = new String(pass.getPassword()).trim();
                if ("Register".equals(e.getActionCommand()))
                    conn.send("register " + u + " " + p + " " + start.getText().trim());
                else
                    conn.send("login " + u + " " + p);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Server unreachable:\n" + ex.getMessage());
            }
        }

        @Override
        public void onLine(String s) {
            if (s.startsWith("Login successful")) {
                currentUser = user.getText().trim();
                SwingUtilities.invokeLater(() -> buildNavbar());
                cards.show(center, "items");
                conn.send("getbalance");
                conn.send("listitems");
            } else if (s.startsWith("User registered")) {
                JOptionPane.showMessageDialog(this, "Registered – now press Login.");
            } else if (s.contains("exists") || s.contains("Invalid")) {
                JOptionPane.showMessageDialog(this, s);
            } else if (s.startsWith("$")) {
                balance = Double.parseDouble(s.substring(1));
                balLbl.setText("BAL: " + s);
            }
            itemsPanel.acceptLine(s);
            invPanel.acceptLine(s);
            msgPanel.acceptLine(s);
        }
    }

    /**
     * Builds the navigation bar (Items, Inventory, Messages, Logout, Exit)
     */
    private void buildNavbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setBackground(purdueGold);
        String[][] tabs = {{"Items","items"},{"Inventory","inventory"},{"Messages","messages"}};
        for (String[] t : tabs) {
            JButton b = new JButton(t[0]);
            styleButton(b);
            b.addActionListener(e -> {
                cards.show(center, t[1]);
                refresh(t[1]);
            });
            bar.add(b);
        }
        JButton logout = new JButton("Logout"), exit = new JButton("Exit");
        styleButton(logout);
        styleButton(exit);
        bar.add(logout);
        bar.add(exit);
        bar.add(Box.createHorizontalStrut(20));
        hiLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        hiLbl.setForeground(purdueBlack);
        hiLbl.setText("Hi, " + currentUser);
        bar.add(hiLbl);
        bar.add(Box.createHorizontalStrut(10));
        bar.add(balLbl);
        logout.addActionListener(e -> {
            if (conn != null) conn.send("logout");
            resetPanels();
            frame.remove(bar);
            cards.show(center,"start");
        });
        exit.addActionListener(e -> {
            if (conn != null) conn.close();
            frame.dispose();
        });
        frame.add(bar, BorderLayout.NORTH);
        frame.revalidate();
    }

    /**
     * Refreshes the given tab and updates balance
     */
    private void refresh(String tab) {
        switch (tab) {
            case "items"    -> { itemsPanel.reset(); conn.send("listitems"); }
            case "inventory"-> { invPanel.reset();  conn.send("myitems");  }
            case "messages" -> { msgPanel.reset();  conn.send("viewuserlist"); }
        }
        conn.send("getbalance");
    }

    /**
     * Resets all panels and clears user state
     */
    private void resetPanels() {
        itemsPanel.reset();
        invPanel.reset();
        msgPanel.reset();
        balLbl.setText("BAL: $0.00");
        hiLbl.setText("");
        currentUser = null;
    }

    /**
     * Styles a button with theme colors and no border
     */
    private void styleButton(JButton b) {
        b.setBackground(purdueBlack);
        b.setForeground(purdueGold);
        b.setOpaque(true);
        b.setBorderPainted(false);
    }
}
