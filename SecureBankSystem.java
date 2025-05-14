import java.sql.*;
import java.util.*;

class User {
    String username;
    String pin;
    double balance;

    public User(String username, String pin, double balance) {
        this.username = username;
        this.pin = pin;
        this.balance = balance;
    }
}

public class SecureBankSystem {
    static final String DB_URL = "jdbc:sqlite:bank.db";
    static final String ADMIN_PASSWORD = "admin123";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            createTables(conn);
            Scanner sc = new Scanner(System.in);
            System.out.println("=== Secure Bank Management System ===");

            while (true) {
                System.out.println("\n1. Register\n2. Login\n3. Admin Login\n4. Exit");
                System.out.print("Choose option: ");
                int choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                    case 1:
                        System.out.print("Enter username: ");
                        String username = sc.nextLine();
                        if (userExists(conn, username)) {
                            System.out.println("User already exists.");
                            break;
                        }
                        System.out.print("Set 4-digit PIN: ");
                        String pin = sc.nextLine();
                        registerUser(conn, username, pin);
                        System.out.println("User registered successfully.");
                        break;
                    case 2:
                        System.out.print("Enter username: ");
                        username = sc.nextLine();
                        System.out.print("Enter PIN: ");
                        pin = sc.nextLine();
                        User user = authenticateUser(conn, username, pin);
                        if (user != null) {
                            userMenu(conn, user, sc);
                        } else {
                            System.out.println("Invalid credentials.");
                        }
                        break;
                    case 3:
                        System.out.print("Enter admin password: ");
                        String adminPass = sc.nextLine();
                        if (adminPass.equals(ADMIN_PASSWORD)) {
                            showAllUsers(conn);
                        } else {
                            System.out.println("Incorrect admin password.");
                        }
                        break;
                    case 4:
                        System.out.println("Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid option.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    static void createTables(Connection conn) throws SQLException {
        String createUsers = "CREATE TABLE IF NOT EXISTS users (username TEXT PRIMARY KEY, pin TEXT, balance REAL DEFAULT 0.0)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createUsers);
        }
    }

    static boolean userExists(Connection conn, String username) throws SQLException {
        String query = "SELECT username FROM users WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    static void registerUser(Connection conn, String username, String pin) throws SQLException {
        String insert = "INSERT INTO users (username, pin) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insert)) {
            pstmt.setString(1, username);
            pstmt.setString(2, pin);
            pstmt.executeUpdate();
        }
    }

    static User authenticateUser(Connection conn, String username, String pin) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ? AND pin = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, pin);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getString("username"), rs.getString("pin"), rs.getDouble("balance"));
            }
            return null;
        }
    }

    static void userMenu(Connection conn, User user, Scanner sc) throws SQLException {
        while (true) {
            System.out.println("\nWelcome, " + user.username);
            System.out.println("1. Check Balance\n2. Deposit\n3. Withdraw\n4. Logout");
            System.out.print("Choose option: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("Balance: Rs." + user.balance);
                    break;
                case 2:
                    System.out.print("Enter amount to deposit: ");
                    double dep = sc.nextDouble();
                    user.balance += dep;
                    updateBalance(conn, user);
                    break;
                case 3:
                    System.out.print("Enter amount to withdraw: ");
                    double wth = sc.nextDouble();
                    if (user.balance >= wth) {
                        user.balance -= wth;
                        updateBalance(conn, user);
                    } else {
                        System.out.println("Insufficient balance.");
                    }
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    static void updateBalance(Connection conn, User user) throws SQLException {
        String update = "UPDATE users SET balance = ? WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(update)) {
            pstmt.setDouble(1, user.balance);
            pstmt.setString(2, user.username);
            pstmt.executeUpdate();
        }
    }

    static void showAllUsers(Connection conn) throws SQLException {
        String query = "SELECT username, balance FROM users";
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            System.out.println("\nAll Users:");
            while (rs.next()) {
                System.out.println("- " + rs.getString("username") + " | Balance: Rs." + rs.getDouble("balance"));
            }
        }
    }
}
