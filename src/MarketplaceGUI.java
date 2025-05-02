import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.HashSet;

/**
 * ===== MarketplaceGUI.java =====
 * Complete Swing GUI for CS180 Marketplace
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
public class MarketplaceGUI {
    /** Connection to the server, initialized on first request */
    private ClientConnection conn;
    /** Currently logged-in username, or null if none */
    private String currentUser = null;
    /** Current user's balance, updated on each server response */
    private double balance = 0.0;

    /** Main application window */
    private final JFrame frame = new JFrame("CS180 Marketplace Client #1");
    /** Layout manager for swapping between panels */
    private final CardLayout cards = new CardLayout();
    /** Container panel that holds Start, Items, Inventory, and Messages panels */
    private final JPanel center = new JPanel(cards) {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Draw a vertical gradient from purdueGold to white
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
     * Application entry point: sets UI defaults and launches GUI on EDT
     */
    public static void main(String[] args) {
        // Set default button colors across all Swing components
        UIManager.put("Button.background", purdueBlack);
        UIManager.put("Button.foreground", purdueGold);
        SwingUtilities.invokeLater(MarketplaceGUI::new);
    }

    /**
     * Constructor: builds the GUI components
     */
    public MarketplaceGUI() {
        buildGUI();
    }

    /**
     * Builds the main GUI: initializes panels and shows frame
     */
    private void buildGUI() {
        // Configure frame
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        // Add StartPanel for login/registration
        center.add(new StartPanel(), "start");

        // Initialize and add ItemsPanel with lavender background
        itemsPanel = new ItemsPanel();
        itemsPanel.setBackground(new Color(230, 230, 245));
        center.add(itemsPanel, "items");

        // Initialize and add InventoryPanel with rose background
        invPanel = new InventoryPanel();
        invPanel.setBackground(new Color(245, 230, 230));
        center.add(invPanel, "inventory");

        // Initialize and add MessagesPanel with mint background
        msgPanel = new MessagesPanel();
        msgPanel.setBackground(new Color(230, 245, 230));
        center.add(msgPanel, "messages");

        // Add container to frame and display
        frame.add(center, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    /**
     * Panel for user login and registration
     */
    private class StartPanel extends JPanel implements ActionListener, ClientConnection.LineHandler {
        private final JTextField user = new JTextField(10);           // Username input field
        private final JPasswordField pass = new JPasswordField(10);   // Password input field
        private final JTextField start = new JTextField("100", 10);  // Initial balance input field

        StartPanel() {
            setLayout(null);
            setBackground(purdueGold);
            setOpaque(true);

            // Create rows for username, password, and initial balance
            JPanel userRow = createRow("Username", user);
            JPanel passRow = createRow("Password", pass);
            JPanel registerRow = createRow("Register", start);

            // Button row for Register and Login actions
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

            // Add rows to panel
            add(userRow);
            add(passRow);
            add(registerRow);
            add(buttonRow);

            // Re-layout rows on frame resize
            frame.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    layoutRows(userRow, passRow, registerRow, buttonRow);
                }
            });
        }

        /**
         * Helper: creates a labeled row with given text and input field
         */
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

        /**
         * Helper: positions rows evenly centered within the frame
         */
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

        /**
         * Sends register or login command when button is pressed
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (conn == null)
                    conn = new ClientConnection(Client.HOST, Client.PORT, this);
                String u = user.getText().trim();
                String p = new String(pass.getPassword()).trim();
                if (e.getActionCommand().equals("Register"))
                    conn.send("register " + u + " " + p + " " + start.getText().trim());
                else
                    conn.send("login " + u + " " + p);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Server unreachable:\n" + ex.getMessage());
            }
        }

        /**
         * Handles asynchronous server responses for login/register and balance updates
         */
        @Override
        public void onLine(String s) {
            if (s.startsWith("Login successful")) {
                currentUser = user.getText().trim();
                SwingUtilities.invokeLater(() -> {
                    buildNavbar();
                    cards.show(center, "items");
                });
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
            // Forward lines to child panels
            itemsPanel.acceptLine(s);
            invPanel.acceptLine(s);
            msgPanel.acceptLine(s);
        }
    }

    /**
     * Builds the navigation bar with Items, Inventory, Messages, Logout, and Exit buttons
     */
    private void buildNavbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setBackground(purdueGold);
        String[][] tabs = {{"Items", "items"}, {"Inventory", "inventory"}, {"Messages", "messages"}};
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

        // Greeting label
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
            cards.show(center, "start");
        });
        exit.addActionListener(e -> {
            if (conn != null) conn.close();
            frame.dispose();
        });

        frame.add(bar, BorderLayout.NORTH);
        frame.revalidate();
    }

    /**
     * Sends commands to refresh the specified tab, then updates balance
     */
    private void refresh(String tab) {
        if (tab.equals("items")) {
            itemsPanel.reset();
            conn.send("listitems");
        }
        if (tab.equals("inventory")) {
            invPanel.reset();
            conn.send("myitems");
        }
        if (tab.equals("messages")) {
            msgPanel.reset();
            conn.send("viewuserlist");
        }
        conn.send("getbalance");
    }

    /**
     * Clears all panels and resets greeting and balance
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
     * Applies theme styling to a JButton: background, foreground, opacity, border
     */
    private void styleButton(JButton b) {
        b.setBackground(purdueBlack);
        b.setForeground(purdueGold);
        b.setOpaque(true);
        b.setBorderPainted(false);
    }
}
