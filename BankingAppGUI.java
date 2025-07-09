import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import javax.swing.*;

public class BankingAppGUI extends JFrame implements ActionListener {
    private JPanel mainPanel, accountPanel, loginPanel, dashboardPanel;
    private JTextField nameField, phoneField, balanceField, passwordField;
    private JTextField loginNameField;
    private JPasswordField loginPasswordField;
    private JTextArea dashboardArea;
    private int loggedInCustomerId = -1;
    private JPanel adminLoginPanel;
    private JTextField adminUserField;                    
    private JPasswordField adminPassField;
    private JPanel adminPanel;   

    private JPanel adminDashboardPanel;
     private JPanel adminStatsPanel;
    private JPanel adminUserDetailsPanel;
    private JTextArea adminStatsTextArea;
private JTextArea adminUserDetailsTextArea;

private Timer balanceRefreshTimer;

                                     
    private JTextArea adminTextArea;
    private final String ADMIN_USERNAME = "admin";
    private final String ADMIN_PASSWORD = "admin123";
    private final TransactionManager transactionManager = new TransactionManager();
    private Timer adminRefreshTimer;

   


    public BankingAppGUI() {
        setTitle("Banking Application");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new CardLayout());

        initializeMainPanel();
        initializeAccountCreationPanel();
        initializeLoginPanel();
        initializeDashboardPanel();
        initializeAdminLoginPanel();
        initializeAdminPanel();

        initializeAdminDashboardPanel();
initializeAdminStatsPanel();
initializeAdminUserDetailsPanel();


        setVisible(true);
    }

    private void initializeMainPanel() {
        mainPanel = new JPanel(new GridLayout(4, 1, 20, 20));
        JLabel welcomeLabel = new JLabel("Welcome to the Banking Application", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));

        JButton createAccountButton = new JButton("Create Account");
        JButton loginButton = new JButton("User Login");
        JButton adminLoginButton = new JButton("Admin Login");

        createAccountButton.addActionListener(e -> switchPanel(accountPanel));
        loginButton.addActionListener(e -> switchPanel(loginPanel));
        adminLoginButton.addActionListener(e -> switchPanel(adminLoginPanel));

        mainPanel.add(welcomeLabel);
        mainPanel.add(createAccountButton);
        mainPanel.add(loginButton);
        mainPanel.add(adminLoginButton);

        add(mainPanel, "Main");
    }

    private void initializeAccountCreationPanel() {
        accountPanel = new JPanel(new GridLayout(6, 2, 10, 10));

        nameField = new JTextField();
        phoneField = new JTextField();
        balanceField = new JTextField();
        passwordField = new JTextField();
        JButton createAccountButton = new JButton("Create Account");

        createAccountButton.addActionListener(this);

        accountPanel.add(new JLabel("Name:"));
        accountPanel.add(nameField);
        accountPanel.add(new JLabel("Phone Number:"));
        accountPanel.add(phoneField);
        accountPanel.add(new JLabel("Initial Balance (Min. 1000):"));
        accountPanel.add(balanceField);
        accountPanel.add(new JLabel("Password:"));
        accountPanel.add(passwordField);
        accountPanel.add(createAccountButton);
        accountPanel.add(new JButton("Back to Main") {{ addActionListener(e -> switchPanel(mainPanel)); }});

        add(accountPanel, "CreateAccount");
    }

    private void initializeLoginPanel() {
        loginPanel = new JPanel(new GridLayout(4, 2, 10, 10));

       loginNameField = new JTextField();
        loginPasswordField = new JPasswordField();
        JButton loginButton = new JButton("Login");

        loginButton.addActionListener(e -> handleLogin());

        loginPanel.add(new JLabel("Name:"));
        loginPanel.add(loginNameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(loginPasswordField);
        loginPanel.add(loginButton);
        loginPanel.add(new JButton("Back to Main") {{ addActionListener(e -> switchPanel(mainPanel)); }});

        add(loginPanel, "Login");
    }

    private void initializeDashboardPanel() {
    dashboardPanel = new JPanel(new BorderLayout(10, 10));

    // Center: Single scrollable area
    dashboardArea = new JTextArea();
    dashboardArea.setEditable(false);
    dashboardArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
    JScrollPane scrollPane = new JScrollPane(dashboardArea);
    dashboardPanel.add(scrollPane, BorderLayout.CENTER);

    //Left: Add Transaction History button and account detail button only
    JPanel leftPanel = new JPanel(new GridLayout(3, 1, 5, 5)); // 3rows, 1 column, small gap
    JButton historyButton = new JButton("📜 Transaction History");
    JButton accountDetailsButton = new JButton("🏦 Account Details");
    JButton changePasswordButton = new JButton("🔒 Change Password");
    historyButton.addActionListener(e -> showTransactionHistory()); // defined below
    accountDetailsButton.addActionListener(e -> showAccountDetails());
    changePasswordButton.addActionListener(e -> showChangePasswordDialog());
    leftPanel.add(historyButton);
    leftPanel.add(accountDetailsButton);
    leftPanel.add(changePasswordButton);
    dashboardPanel.add(leftPanel, BorderLayout.WEST);

    //  Bottom: All user action buttons
    JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 10, 10));
    JButton depositButton = new JButton("💰 Deposit");
    JButton withdrawButton = new JButton("💸 Withdraw");
    JButton transferButton = new JButton("🔁 Transfer");
    JButton logoutButton = new JButton("🚪 Logout");

    depositButton.addActionListener(e -> handleDeposit());
    withdrawButton.addActionListener(e -> handleWithdraw());
    transferButton.addActionListener(e -> showTransferDialog());
    logoutButton.addActionListener(e -> logout());

    buttonPanel.add(depositButton);
    buttonPanel.add(withdrawButton);
    buttonPanel.add(transferButton);
    buttonPanel.add(logoutButton);

    dashboardPanel.add(buttonPanel, BorderLayout.SOUTH);

    add(dashboardPanel, "Dashboard");
}
private void showAccountDetails() {
    stopBalanceAutoRefresh();
    BankingDatabase db = new BankingDatabase();
    String details = db.getAccountDetails(loggedInCustomerId); // call your database method
    dashboardArea.setText("🏦 Account Details\n\n" + details);
}
 private void showChangePasswordDialog() {
    JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

    JPasswordField oldPassField = new JPasswordField();
    JPasswordField newPassField = new JPasswordField();
    JPasswordField confirmPassField = new JPasswordField();

    panel.add(new JLabel("🔑 Current Password:"));
    panel.add(oldPassField);
    panel.add(new JLabel("🆕 New Password:"));
    panel.add(newPassField);
    panel.add(new JLabel("🔁 Confirm New Password:"));
    panel.add(confirmPassField);

    int result = JOptionPane.showConfirmDialog(this, panel, "Change Password", JOptionPane.OK_CANCEL_OPTION);

    if (result == JOptionPane.OK_OPTION) {
        String oldPass = new String(oldPassField.getPassword());
        String newPass = new String(newPassField.getPassword());
        String confirmPass = new String(confirmPassField.getPassword());

        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "❌ New passwords do not match!");
            return;
        }

        if (newPass.length() < 6) {
            JOptionPane.showMessageDialog(this, "❌ New password must be at least 6 characters.");
            return;
        }

        BankingDatabase db = new BankingDatabase();
        if (db.changePassword(loggedInCustomerId, oldPass, newPass)) {
            JOptionPane.showMessageDialog(this, "✅ Password changed successfully!");
        } else {
            JOptionPane.showMessageDialog(this, "❌ Incorrect current password.");
        }
    }
}




    private void initializeAdminLoginPanel() {
    adminLoginPanel = new JPanel(new GridLayout(3, 2, 10, 10));
    adminUserField = new JTextField();
    adminPassField = new JPasswordField();

    JButton loginButton = new JButton("Login");
    loginButton.addActionListener(e -> handleAdminLogin());

    adminLoginPanel.add(new JLabel("Admin Username:"));
    adminLoginPanel.add(adminUserField);
    adminLoginPanel.add(new JLabel("Password:"));
    adminLoginPanel.add(adminPassField);
    adminLoginPanel.add(loginButton);
    adminLoginPanel.add(new JButton("Back to Main") {{ addActionListener(e -> switchPanel(mainPanel)); }});

    add(adminLoginPanel, "AdminLogin");
}


private void initializeAdminPanel() {
    adminPanel = new JPanel(new BorderLayout());
    adminTextArea = new JTextArea();
    adminTextArea.setEditable(false);

    JButton backButton = new JButton("Logout Admin");
    backButton.addActionListener(e -> switchPanel(mainPanel));

    adminPanel.add(new JScrollPane(adminTextArea), BorderLayout.CENTER);
    adminPanel.add(backButton, BorderLayout.SOUTH);

    add(adminPanel, "AdminPage");
}

    private void switchPanel(JPanel panel) {
        getContentPane().removeAll();
        add(panel);
        revalidate();
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String name = nameField.getText();
        String phone = phoneField.getText();
        String password = passwordField.getText();
        double initialBalance;

        try {
            initialBalance = Double.parseDouble(balanceField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid balance input.");
            return;
        }

        BankingDatabase db = new BankingDatabase();
        if (db.createAccount(name, phone, password, initialBalance)) {
            JOptionPane.showMessageDialog(this, "Account Created Successfully!");
            switchPanel(mainPanel);
        } else {
            JOptionPane.showMessageDialog(this, "Account creation failed. Please try again.");
        }
    }

    private void handleLogin() {
    String name = loginNameField.getText();
    String password = new String(loginPasswordField.getPassword());

    BankingDatabase db = new BankingDatabase();
    if (db.verifyLogin(name, password)) {
        try {
            loggedInCustomerId = db.getCustomerIdByName(name);
            if (loggedInCustomerId != -1) {
                loadDashboard();
                switchPanel(dashboardPanel);
               startBalanceAutoRefresh();
            } else {
                JOptionPane.showMessageDialog(this, "User not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error retrieving user ID.");
        }
    } else {
        JOptionPane.showMessageDialog(this, "Invalid login credentials.");
    }
}

private void initializeAdminDashboardPanel() {
    adminDashboardPanel = new JPanel(new GridLayout(3, 1, 20, 20));
    adminDashboardPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

    JButton statsButton = new JButton("📊 View Bank Stats");
    JButton usersButton = new JButton("👥 View All User Details");
    JButton logoutButton = new JButton("🚪 Logout Admin");

    statsButton.addActionListener(e -> refreshAndSwitchToStats());
    usersButton.addActionListener(e -> refreshAndSwitchToUserDetails());
    logoutButton.addActionListener(e -> switchPanel(mainPanel));

    adminDashboardPanel.add(statsButton);
    adminDashboardPanel.add(usersButton);
    adminDashboardPanel.add(logoutButton);

    add(adminDashboardPanel, "AdminDashboard");
}

private void initializeAdminStatsPanel() {
    adminStatsPanel = new JPanel(new BorderLayout());

    adminStatsTextArea = new JTextArea();
    adminStatsTextArea.setEditable(false);
    adminStatsTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

    JButton backButton = new JButton("🔙 Back to Dashboard");
    backButton.addActionListener(e -> switchPanel(adminDashboardPanel));

    adminStatsPanel.add(new JScrollPane(adminStatsTextArea), BorderLayout.CENTER);
    adminStatsPanel.add(backButton, BorderLayout.SOUTH);

    add(adminStatsPanel, "AdminStats");
}

private void initializeAdminUserDetailsPanel() {
    adminUserDetailsPanel = new JPanel(new BorderLayout(10, 10));

    // Text area to show user details
    adminUserDetailsTextArea = new JTextArea();
    adminUserDetailsTextArea.setEditable(false);
    adminUserDetailsTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

    // Search section at the top
    JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
    JTextField searchField = new JTextField();
    JButton searchButton = new JButton("🔍 Search");

    searchPanel.add(new JLabel("Search by Account ID: "), BorderLayout.WEST);
    searchPanel.add(searchField, BorderLayout.CENTER);
    searchPanel.add(searchButton, BorderLayout.EAST);

    // Search functionality
    searchButton.addActionListener(e -> {
        String id = searchField.getText().trim();
        if (id.isEmpty()) {
            adminUserDetailsTextArea.setText("Please enter Account ID to search.");
        } else {
            try {
                BankingDatabase db = new BankingDatabase();
                String result = db.searchUser(id);  // Assumes this returns a String
                adminUserDetailsTextArea.setText(result.isEmpty() ? "No user found." : result);
            } catch (Exception ex) {
                adminUserDetailsTextArea.setText("Error while fetching user data: " + ex.getMessage());
            }
        }
        searchField.setText(""); // Clear the input field
    });

    // Back to Dashboard button
    JButton backToDashboardButton = new JButton("🔙 Back to Dashboard");
    backToDashboardButton.addActionListener(e -> switchPanel(adminDashboardPanel));

    // Bottom panel with only one back button (if that's what you intend)
    JPanel bottomPanel = new JPanel(new BorderLayout());
    bottomPanel.add(backToDashboardButton, BorderLayout.EAST);

    // Combine all panels
    adminUserDetailsPanel.add(searchPanel, BorderLayout.NORTH);
    adminUserDetailsPanel.add(new JScrollPane(adminUserDetailsTextArea), BorderLayout.CENTER);
    adminUserDetailsPanel.add(bottomPanel, BorderLayout.SOUTH);

    // Add to card layout
    add(adminUserDetailsPanel, "AdminUsers");
}

private void refreshAndSwitchToStats() {
    BankingDatabase db = new BankingDatabase();
    adminStatsTextArea.setText(db.getBankStats());
    switchPanel(adminStatsPanel);
}



private void refreshAndSwitchToUserDetails() {
    BankingDatabase db = new BankingDatabase();
    adminUserDetailsTextArea.setText(db.getAllUserDetails());
    switchPanel(adminUserDetailsPanel);
}

    private void loadAdminPanel() {
    BankingDatabase db = new BankingDatabase();
    String allAccounts = db.getAllUserDetails(); // Assuming this method exists
    adminTextArea.setText(allAccounts);
        
    }
  

private void refreshAllAdminViews() {
    BankingDatabase db = new BankingDatabase();
    String userDetails = db.getAllUserDetails();

    // Update admin dashboard view
    if (adminTextArea != null) {
        adminTextArea.setText(userDetails);
    }

    // Update user details panel if it's visible
    if (adminUserDetailsTextArea != null && adminUserDetailsPanel.isShowing()) {
        adminUserDetailsTextArea.setText(userDetails);
    }
}



    private void handleAdminLogin() {
    String username = adminUserField.getText();
    String password = new String(adminPassField.getPassword());

    if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
        adminUserField.setText("");
        adminPassField.setText("");
        loadAdminPanel();
        switchPanel(adminDashboardPanel);

        if (adminRefreshTimer != null) {
    adminRefreshTimer.stop();
}

adminRefreshTimer = new Timer(5000, new ActionListener() {
    public void actionPerformed(ActionEvent e) {

        refreshAllAdminViews();

    }
});

adminRefreshTimer.start();

    } else {
        JOptionPane.showMessageDialog(this, "Invalid admin credentials.");
    }
}

    private void logout() {
        loggedInCustomerId = -1;
        switchPanel(mainPanel);
    }

    private void loadDashboard() {
        BankingDatabase db = new BankingDatabase();
        String accountDetails = db.getAccountDetails(loggedInCustomerId);
        dashboardArea.setText(accountDetails);
    }
   // auto referesh balance
    private void startBalanceAutoRefresh() {
        if (balanceRefreshTimer != null) {
        balanceRefreshTimer.stop();  // stop any previous instance
    }
    balanceRefreshTimer = new Timer(3000, e -> {
        if (loggedInCustomerId != -1) {
            loadDashboard(); // refresh every 3 seconds
        }
    });
    balanceRefreshTimer.start();
}
private void stopBalanceAutoRefresh() {
    if (balanceRefreshTimer != null) {
        balanceRefreshTimer.stop();
    }
}



private void showTransactionHistory() {
    stopBalanceAutoRefresh();  // Stop refreshing while viewing history
    BankingDatabase db = new BankingDatabase();
    String history = db.getTransactionHistory(String.valueOf(loggedInCustomerId));

    if (history == null || history.isEmpty()) {
        dashboardArea.setText("No transaction history found.");
    } else {
        dashboardArea.setText(history);
    }
}



    private void handleDeposit() {
    String amountStr = JOptionPane.showInputDialog(this, "Enter deposit amount:");
    try {
        double amount = Double.parseDouble(amountStr);
        transactionManager.depositAsync(
            loggedInCustomerId,
            amount,
            () -> SwingUtilities.invokeLater(() -> {
                loadDashboard();
               refreshAllAdminViews();

                JOptionPane.showMessageDialog(this, "Deposit successful!");
            }),
            () -> SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, "Deposit failed. Please try again."))
        );
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Invalid amount entered.");
    }
}


    private void handleWithdraw() {
    String amountStr = JOptionPane.showInputDialog(this, "Enter withdrawal amount:");
    try {
        double amount = Double.parseDouble(amountStr);
        transactionManager.withdrawAsync(
            loggedInCustomerId,
            amount,
            () -> SwingUtilities.invokeLater(() -> {
                loadDashboard();
                refreshAllAdminViews();

                JOptionPane.showMessageDialog(this, "Withdrawal successful!");
            }),
            () -> SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, "Withdrawal failed. Please try again."))
        );
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Invalid amount entered.");
    }
}


private void showTransferDialog() {
    BankingDatabase db = new BankingDatabase();
    String allAccounts = db.getAllOtherAccounts(loggedInCustomerId);

    JTextArea accountList = new JTextArea(allAccounts);
    accountList.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(accountList);
    scrollPane.setPreferredSize(new Dimension(300, 200));

    int result = JOptionPane.showConfirmDialog(this, scrollPane, "Available Recipients", JOptionPane.OK_CANCEL_OPTION);

    if (result == JOptionPane.OK_OPTION) {
        try {
            String receiverIdStr = JOptionPane.showInputDialog(this, "Enter Receiver's Customer ID:");
            String amountStr = JOptionPane.showInputDialog(this, "Enter Amount to Transfer:");

            int receiverId = Integer.parseInt(receiverIdStr);
            double amount = Double.parseDouble(amountStr);

            transactionManager.transferAsync(
                loggedInCustomerId,
                receiverId,
                amount,
                () -> SwingUtilities.invokeLater(() -> {
                    loadDashboard();  //updste user side
                    refreshAllAdminViews();

                    JOptionPane.showMessageDialog(this, "Transfer successful.");
                }),
                () -> SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this, "Transfer failed. Please check balance or recipient ID."))
            );

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter numbers only.");
        }
    }
}



    public static void main(String[] args) {
        new BankingAppGUI();
    }
}