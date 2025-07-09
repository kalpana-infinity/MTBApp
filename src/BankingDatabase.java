import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;

public class BankingDatabase {

    // Create new customer account
    public boolean createAccount(String name, String phoneNumber, String password, double initialBalance) {
        if (initialBalance < 1000) {
            JOptionPane.showMessageDialog(null, "Minimum balance required to open an account is 1000.");
            return false;
        }
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false); 
            String sql = "INSERT INTO Customers (name, phonenumber, password, createdat, balance) VALUES (?, ?, ?, SYSDATE, ?)";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, phoneNumber);
            stmt.setString(3, password);
            stmt.setDouble(4, initialBalance);
            int rowsAffected = stmt.executeUpdate();

         if (rowsAffected > 0) {
                connection.commit();  // Explicitly commit the transaction
                 System.out.println("Account created successfully for " + name);
                return true;
            } else {
                connection.rollback();  // Rollback if no rows affected
                JOptionPane.showMessageDialog(null, "Account creation failed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error creating account: " + e.getMessage());
        }
        return false;
    }

    public boolean verifyLogin(String name, String password) {
    String sql = "SELECT customerid FROM Customers WHERE name = ? AND password = ?";
    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setString(1, name);
        stmt.setString(2, password);
        ResultSet rs = stmt.executeQuery();
        return rs.next();  // If a record is found, login is valid
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error during login: " + e.getMessage());
        return false;
    }
}

public String getBankStats() {
    
    StringBuilder sb = new StringBuilder();
    try (Connection conn = DatabaseConnection.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT COUNT(*), SUM(balance),avg(balance) FROM customers")) {

        if (rs.next()) {
            int totalUsers = rs.getInt(1);
            double totalBalance = rs.getDouble(2);
            double avgBalance=rs.getDouble(3);

            sb.append("📊 Bank Statistics\n\n");
            sb.append("Total Users: ").append(totalUsers).append("\n");
            sb.append("Total Balance in Bank: ₹").append(String.format("%.2f", totalBalance)).append("\n");
            sb.append("Average Balance in Bank: ₹").append(String.format("%.2f", avgBalance)).append("\n");
        }
    } catch (SQLException e) {
        sb.append("Error retrieving stats: ").append(e.getMessage());
        e.printStackTrace();
    }
    return sb.toString();
}

public String searchUser(String input) {
    StringBuilder result = new StringBuilder();
    String queryByName = "SELECT customerid, name, phonenumber, balance FROM customers WHERE LOWER(name) LIKE ?";
    String queryById = "SELECT customerid, name, phonenumber, balance FROM customers WHERE customerid = ?";

    try (Connection conn = DatabaseConnection.getConnection()) {
        PreparedStatement stmt;

        // Try to parse input as integer (i.e., search by ID)
        try {
            int id = Integer.parseInt(input);
            stmt = conn.prepareStatement(queryById);
            stmt.setInt(1, id);
        } catch (NumberFormatException e) {
            // If not a valid ID, fallback to name search
            stmt = conn.prepareStatement(queryByName);
            stmt.setString(1, "%" + input.toLowerCase() + "%");
        }

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            int id = rs.getInt("customerid");
            String userName = rs.getString("name");
            String phone = rs.getString("phonenumber");
            double balance = rs.getDouble("balance");

            result.append("ID: ").append(id).append(" | ")
                  .append("Name: ").append(userName).append(" | ")
                  .append("Phone: ").append(phone).append(" | ")
                  .append("Balance: ₹").append(String.format("%.2f", balance)).append("\n");
        }

        if (result.length() == 0) {
            result.append("No user found matching input: ").append(input);
        }

    } catch (SQLException e) {
        e.printStackTrace();
        return "Error retrieving search results.";
    }

    return result.toString();
}

    // Get customer ID by name
public int getCustomerIdByName(String name) throws SQLException {
    String query = "SELECT customerid FROM Customers WHERE name = ?";
    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setString(1, name);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("customerid");
        } else {
            return -1; // Not found
        }
    }
}


    // Deposit money
    public boolean deposit(int customerId, double amount) {
        if (amount <= 0) {
            JOptionPane.showMessageDialog(null, "Amount must be positive.");
            return false;
        }
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            // Update balance
            String updateBalanceSQL = "UPDATE Customers SET balance = balance + ? WHERE customerid = ?";
            PreparedStatement balanceStmt = connection.prepareStatement(updateBalanceSQL);
            balanceStmt.setDouble(1, amount);
            balanceStmt.setInt(2, customerId);
            int rowsUpdated = balanceStmt.executeUpdate();

            if (rowsUpdated > 0) {
                // Insert transaction
                String insertTransactionSQL = "INSERT INTO Transactions (customer_id, transaction_type, amount) VALUES (?, 'Deposit', ?)";
                PreparedStatement transactionStmt = connection.prepareStatement(insertTransactionSQL);
                transactionStmt.setInt(1, customerId);
                transactionStmt.setDouble(2, amount);
                transactionStmt.executeUpdate();
                connection.commit();
                return true;
            } else {
                connection.rollback();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error during deposit: " + e.getMessage());
        }
        return false;
    }
    // get user details for admin
    public String getAllUserDetails() {
    StringBuilder sb = new StringBuilder();
    try {
        Connection conn = DatabaseConnection.getConnection();
        String query = "SELECT CUSTOMERID, NAME, PHONENUMBER, BALANCE FROM CUSTOMERS";
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            sb.append("ID: ").append(rs.getInt("CUSTOMERID")).append("\n");
            sb.append("Name: ").append(rs.getString("NAME")).append("\n");
            sb.append("Phone: ").append(rs.getString("PHONENUMBER")).append("\n");
            sb.append("Balance: ").append(rs.getDouble("BALANCE")).append("\n");
            sb.append("-------------------------\n");
        }
        conn.close();
    } catch (SQLException e) {
        e.printStackTrace();
        sb.append("Error retrieving user data.");
    }
    return sb.toString();
}
// get account details for login 
public String getAccountDetails(int customerId) {
    StringBuilder sb = new StringBuilder();
    try (Connection conn = DatabaseConnection.getConnection()) {
        String sql = "SELECT name, phonenumber, balance FROM Customers WHERE customerid = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, customerId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            sb.append("Name: ").append(rs.getString("name")).append("\n");
            sb.append("Phone: ").append(rs.getString("phonenumber")).append("\n");
            sb.append("Balance: ").append(rs.getDouble("balance")).append("\n");
        } else {
            sb.append("Account details not found.");
        }
    } catch (SQLException e) {
        e.printStackTrace();
        sb.append("Error retrieving account details.");
    }
    return sb.toString();
}



    // Withdraw money
    public boolean withdraw(int customerId, double amount) {
        if (amount <= 0) {
            JOptionPane.showMessageDialog(null, "Amount must be positive.");
            return false;
        }
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            // Check balance
            String balanceCheckSQL = "SELECT balance FROM Customers WHERE customerid = ?";
            PreparedStatement checkStmt = connection.prepareStatement(balanceCheckSQL);
            checkStmt.setInt(1, customerId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                double currentBalance = rs.getDouble("balance");
                if (amount > currentBalance) {
                    JOptionPane.showMessageDialog(null, "Insufficient balance.");
                    return false;
                }
            }

            // Update balance
            String updateBalanceSQL = "UPDATE Customers SET balance = balance - ? WHERE customerid = ?";
            PreparedStatement balanceStmt = connection.prepareStatement(updateBalanceSQL);
            balanceStmt.setDouble(1, amount);
            balanceStmt.setInt(2, customerId);
            int rowsUpdated = balanceStmt.executeUpdate();

            if (rowsUpdated > 0) {
                // Insert transaction
                String insertTransactionSQL = "INSERT INTO Transactions (customer_id, transaction_type, amount) VALUES (?, 'Withdrawal', ?)";
                PreparedStatement transactionStmt = connection.prepareStatement(insertTransactionSQL);
                transactionStmt.setInt(1, customerId);
                transactionStmt.setDouble(2, amount);
                transactionStmt.executeUpdate();
                connection.commit();
                return true;
            } else {
                connection.rollback();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error during withdrawal: " + e.getMessage());
        }
        return false;
}

//other account detail for money transfer
public String getAllOtherAccounts(int currentCustomerId) {
    StringBuilder sb = new StringBuilder();
    try (Connection connection = DatabaseConnection.getConnection()) {
        String query = "SELECT customerid, name FROM Customers WHERE customerid != ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, currentCustomerId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            sb.append("Customer ID: ").append(rs.getInt("customerid")).append("\n");
            sb.append("Name: ").append(rs.getString("name")).append("\n\n");
        }
    } catch (SQLException e) {
        e.printStackTrace();
        sb.append("Error retrieving accounts.");
    }
    return sb.toString();
}


    // Transfer money between two customers
public boolean transferMoney(int senderId, int receiverId, double amount) {
    if (amount <= 0) {
        JOptionPane.showMessageDialog(null, "Amount must be positive.");
        return false;
    }

    if (senderId == receiverId) {
        JOptionPane.showMessageDialog(null, "Cannot transfer to the same account. ");
        return false;
    }

    try (Connection connection = DatabaseConnection.getConnection()) {
        connection.setAutoCommit(false);

        //  Check sender's balance
        String checkBalanceSQL = "SELECT balance FROM Customers WHERE customerid = ?";
        PreparedStatement checkStmt = connection.prepareStatement(checkBalanceSQL);
        checkStmt.setInt(1, senderId);
        ResultSet rs = checkStmt.executeQuery();

        if (!rs.next()) {
            JOptionPane.showMessageDialog(null, "Sender not found.");
            return false;
        }

        double senderBalance = rs.getDouble("balance");
        if (senderBalance < amount) {
            JOptionPane.showMessageDialog(null, "Insufficient funds.");
            return false;
        }

        //  Deduct from sender
        String deductSQL = "UPDATE Customers SET balance = balance - ? WHERE customerid = ?";
        PreparedStatement deductStmt = connection.prepareStatement(deductSQL);
        deductStmt.setDouble(1, amount);
        deductStmt.setInt(2, senderId);
        deductStmt.executeUpdate();

        //  Credit to receiver
        String creditSQL = "UPDATE Customers SET balance = balance + ? WHERE customerid = ?";
        PreparedStatement creditStmt = connection.prepareStatement(creditSQL);
        creditStmt.setDouble(1, amount);
        creditStmt.setInt(2, receiverId);
        int updated = creditStmt.executeUpdate();
        if (updated == 0) {
            connection.rollback();
            JOptionPane.showMessageDialog(null, "Receiver not found.");
            return false;
        }

        //  Record transaction for sender
       String insertTxnSQL = "INSERT INTO Transactions (transaction_id, customer_id, transaction_type, amount, transaction_date) VALUES (transaction_seq.NEXTVAL, ?, ?, ?, SYSDATE)";
    

// Sender transaction
PreparedStatement senderTxnStmt = connection.prepareStatement(insertTxnSQL);
senderTxnStmt.setInt(1, senderId);
senderTxnStmt.setString(2, "Transfer Sent"); // Must be ≤ 20 characters
senderTxnStmt.setDouble(3, amount);
senderTxnStmt.executeUpdate();

// Receiver transaction
PreparedStatement receiverTxnStmt = connection.prepareStatement(insertTxnSQL);
receiverTxnStmt.setInt(1, receiverId);
receiverTxnStmt.setString(2, "Transfer Received"); // Must be ≤ 20 characters
receiverTxnStmt.setDouble(3, amount);
receiverTxnStmt.executeUpdate();

   connection.commit();
        return true;
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error during transfer: " + e.getMessage());
        return false;
    }
}

public String getTransactionHistory(String customerId) {
    StringBuilder sb = new StringBuilder();
    String query = "SELECT TRANSACTION_ID, TRANSACTION_TYPE, AMOUNT, TRANSACTION_DATE " +
                   "FROM transactions WHERE CUSTOMER_ID = ? ORDER BY TRANSACTION_DATE DESC";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(query)) {

        ps.setString(1, customerId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            int transactionId = rs.getInt("TRANSACTION_ID");
            String type = rs.getString("TRANSACTION_TYPE");
            double amount = rs.getDouble("AMOUNT");
            java.sql.Timestamp date = rs.getTimestamp("TRANSACTION_DATE");

            sb.append("Transaction ID: ").append(transactionId).append("\n");
            sb.append("Type: ").append(type).append("\n");
            sb.append("Amount: ₹").append(amount).append("\n");
            sb.append("Date: ").append(date).append("\n");
            sb.append("----------------------------------\n");
        }

    } catch (SQLException e) {
        return "Error loading history: " + e.getMessage();
    }

    return sb.length() == 0 ? "No transactions found." : sb.toString();
}

public boolean changePassword(int customerId, String oldPassword, String newPassword) {
    String query = "SELECT password FROM customers WHERE customerid = ?";
    String update = "UPDATE customers SET password = ? WHERE customerid = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setInt(1, customerId);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            String currentPass = rs.getString("password");
            if (currentPass.equals(oldPassword)) {
                try (PreparedStatement updateStmt = conn.prepareStatement(update)) {
                    updateStmt.setString(1, newPassword);
                    updateStmt.setInt(2, customerId);
                    updateStmt.executeUpdate();
                    return true;
                }
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return false;
}


}
