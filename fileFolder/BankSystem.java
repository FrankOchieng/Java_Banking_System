package fileFolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class BankSystem {
    private Map<Integer, Account> accounts = new HashMap<>();
    private Scanner scanner = new Scanner(System.in);
    private Account currentAccount;
	protected int accountNumberCounter = 10101010; 
	private final String DB_URL = "jdbc:mysql://localhost:3306/bankdb";
	private final String DB_USER = "root"; //database user name defaul is root
	private final String DB_PASS = "c@d=r"; // database password default is c@d=r

	 public BankSystem() {
		loadAccountsFromDB();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT MAX(accountNumber) FROM accounts";
            PreparedStatement stmt = conn.prepareStatement(sql);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                int max = rs.getInt(1);
                if (!rs.wasNull()) {
                    accountNumberCounter = max + 1;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error initializing account number: " + e.getMessage());
        }	
    }


    class Account {
        int accountNumber;
		String name;	
        int pin;
        double balance;
       // String firstName,middleName,surName,address,revenuePin;
	   // int dayObirth,monthObirth,yearObirth,bankPin,bankPinC;

		Account(int accountNumber, String name, int pin, double balance) {
			this.accountNumber = accountNumber;
			this.name = name;
			this.pin = pin;
			this.balance = balance;
		}
    }


	class Transactions{
		int accountNumber;
		String name;
		double deposit;
		double withdraw;
		double balance;


		Transactions(int accountNumber, String name, double deposit, double withdraw, double balance){
			this.accountNumber = accountNumber;
			this.name = name;
			this.deposit = deposit;
			this.withdraw = withdraw;
			this.balance = balance;
		}
	}

	private void loadAccountsFromDB() {
		System.out.println("loading accounts from database...");
		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
			String sql = "SELECT accountNumber, name, pin, balance FROM accounts";
			PreparedStatement stmt = conn.prepareStatement(sql);
			var rs = stmt.executeQuery();
			while (rs.next()) {
				int accNum = rs.getInt("accountNumber");
				String name = rs.getString("name");
				int pin = rs.getInt("pin");
				double balance = rs.getDouble("balance");
				accounts.put(accNum, new Account(accNum, name, pin, balance));
			}
		} catch (SQLException e) {
			System.out.println("Error loading accounts: " + e.getMessage());
		}
		System.out.println("Accounts loaded successfully!");
		System.out.println("Loaded " + accounts.size() + " accounts from database.");
		
}

	public String proccessAccountCreation(String name, int pin){
		int accountNumber = accountNumberCounter++;
		accounts.put(accountNumber, new Account(accountNumber, name, pin, 0.0));
		saveAccountToDB(accounts.get(accountNumber));

		return "Account created successfully. Your account number is: " +accountNumber;
	}	


	public void createAccount() {
		System.out.print("Enter your name: ");
		String name = scanner.nextLine();
		int accountNumber = accountNumberCounter++;
		int pin;
		while (true) {
			pin = validInt(scanner,"Enter a four digit pin: ");
			if (String.valueOf(pin).length() == 4) break;
			System.out.println("Invalid PIN! Must be exactly 4 digits.");
		}
		accounts.put(accountNumber, new Account(accountNumber, name, pin, 0.0));
		saveAccountToDB(accounts.get(accountNumber));
		System.out.println("Account created successfully! Your account number is: " + accountNumber);
		scanner.nextLine(); 
	}


	private void saveAccountToDB(Account account) {
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
        String sql = "INSERT INTO accounts (accountNumber, name, pin, balance) VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, account.accountNumber);
        stmt.setString(2, account.name);
        stmt.setInt(3, account.pin);
        stmt.setDouble(4, account.balance);
        stmt.executeUpdate();
    } catch (SQLException e) {
        System.out.println("Error saving account: " + e.getMessage());
    }
}

	public void selectAccount() {
        int accountNumber = validInt(scanner,"Enter account number: ");
	    
	    if (accounts.containsKey(accountNumber)) {
	        int pin = validInt(scanner,"Enter pin: ");
	
	        Account acc = accounts.get(accountNumber);
	        if (acc.pin == pin) {
	            currentAccount = acc;
	            System.out.println("Login successful!");
	        } else {
	            System.out.println("Incorrect PIN!");
	        }
	    } else {
	        System.out.println("Account not found.");
	    }
	}


	public Account fetchAccountFromDB(int accountNumber, int pin) {
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
        String sql = "SELECT * FROM accounts WHERE accountNumber = ? AND pin = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, accountNumber);
        stmt.setInt(2, pin);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            Account acc = new Account(
                rs.getInt("accountNumber"),
                rs.getString("name"),
                rs.getInt("pin"),
                rs.getDouble("balance")
            );
            return acc;
        }
    } catch (SQLException e) {
        System.out.println("Error fetching account: " + e.getMessage());
    }
    return null;
}

	public Account fetchAccountByNumber(int accountNumber) {
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
        String sql = "SELECT * FROM accounts WHERE accountNumber = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, accountNumber);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return new Account(
                rs.getInt("accountNumber"),
                rs.getString("name"),
                rs.getInt("pin"),
                rs.getDouble("balance")
            );
        }
    } catch (SQLException e) {
        System.out.println("Error fetching account: " + e.getMessage());
    }
    return null;
}

	public Account getCurrentAccount() {
        return currentAccount;
    }

    public void setCurrentAccount(Account account) {
        this.currentAccount = account;
    }

	private void updateAccountBalanceInDB(Account account) {
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
        String sql = "UPDATE accounts SET balance = ? WHERE accountNumber = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setDouble(1, account.balance);
        stmt.setInt(2, account.accountNumber);
        stmt.executeUpdate();
    } catch (SQLException e) {
        System.out.println("Error updating balance: " + e.getMessage());
    }
}


	private void saveTransactionToDB(int accountNumber, String name, double deposit, double withdraw, double balance, double transfer) {
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
        String sql = "INSERT INTO transactions (acc_No, acc_Name, deposit, withdraw, transfer, balance) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, accountNumber);
        stmt.setString(2, name);
        stmt.setDouble(3, deposit);
        stmt.setDouble(4, withdraw);
        stmt.setDouble(5, transfer);
        stmt.setDouble(6, balance);
        stmt.executeUpdate();
        System.out.println("Transaction saved to DB.");
    } catch (SQLException e) {
        System.out.println("Error saving transaction: " + e.getMessage());
    }
}
	
	public void deposit() {
	    if (currentAccount == null) {
	        System.out.println("Select an account first!");
	        return;
	    }
	    double amount = validDouble(scanner,"Enter deposit amount: ");
	    currentAccount.balance += amount;
		updateAccountBalanceInDB(currentAccount);
		saveTransactionToDB(currentAccount.accountNumber, currentAccount.name, amount, 0.0, currentAccount.balance, 0.0); // deposit
	    System.out.printf("Deposit successful! New balance: %.2f\n", currentAccount.balance);
	}

	
	public void withdraw() {
	    if (currentAccount == null) {
	        System.out.println("Select an account first!");
	        return;
	    }
	    double amount = validDouble(scanner,"Enter withdrawal amount: ");
	
	    if (currentAccount.balance < 1) {
	        System.out.println("Insufficient balance! Your balance must be at least 1.00 to withdraw.");
	    } else if (amount > currentAccount.balance) {
	        System.out.println("Withdrawal failed! You cannot withdraw more than your balance.");
	    } else {
	        currentAccount.balance -= amount;
			updateAccountBalanceInDB(currentAccount);
			saveTransactionToDB(currentAccount.accountNumber, currentAccount.name, 0.0, amount, currentAccount.balance, 0.0); // withdraw
	        System.out.printf("Withdrawal successful! New balance: %.2f\n", currentAccount.balance);
	    }
	}

	
	public double checkBalance() {
		double r;
	    if (currentAccount == null) {
	        System.out.println("Select an account first!");
	        
	    }
	    System.out.printf("Your balance: %.2f\n", currentAccount.balance);
		r = currentAccount.balance;
		return r;
	}

	
	public void transfer() {
	    if (currentAccount == null) {
	        System.out.println("Select an account first!");
	        return;
	    }
	    int recipientAccNum = validInt(scanner,"Enter recipient account number: ");
	    if (!accounts.containsKey(recipientAccNum)) {
	        System.out.println("Recipient account not found!");
	        return;
	    }
	    if (recipientAccNum == currentAccount.accountNumber) {
	        System.out.println("Cannot transfer to the same account!");
	        return;
	    }
	    double amount = validDouble(scanner,"Enter transfer amount: ");
	    if (amount <= 0) {
	        System.out.println("Transfer amount must be positive!");
	        return;
	    }
	    if (currentAccount.balance < amount) {
	        System.out.println("Insufficient balance for transfer!");
	        return;
	    }

	    Account recipient = accounts.get(recipientAccNum);
	    currentAccount.balance -= amount;
	    recipient.balance += amount;

	    updateAccountBalanceInDB(currentAccount);
	    updateAccountBalanceInDB(recipient);

	    
	    saveTransactionToDB(currentAccount.accountNumber, currentAccount.name, 0.0, 0.0, currentAccount.balance, -amount);

	    saveTransactionToDB(recipient.accountNumber, recipient.name, 0.0, 0.0, recipient.balance, amount);

	    System.out.printf("Transferred %.2f to %s (Account %d). Your new balance: %.2f\n",
	            amount, recipient.name, recipient.accountNumber, currentAccount.balance);
	}
	private int validInt(Scanner sc,String prompt){
		int ret;
		while(true){
			System.out.print(prompt);
			if(sc.hasNextInt()){
				ret = sc.nextInt();
				//sc.next();
				break;
			}
			else{
				System.out.println("Enter a valid input! Can only contain '0,1,2,3,4,5,6,7,8,9'");
				scanner.next();
			}
		}
		return ret;
	}
	private double validDouble(Scanner sca,String Prompt){
		double v;
		while(true){
			if(sca.hasNextDouble()){
				System.out.print(Prompt);
				v = sca.nextDouble();
				break;
			}
			else{
				System.out.println("Enter valid amount!");
				sca.next();
			}
		}
		return v;

	}
	public void start() {
	    while (true) {
	        System.out.println("\n1. Create Account\n2. Select Account\n3. Deposit\n4. Withdraw\n5. Check Balance\n6. Transfer\n7. Exit");
	        int choice = validInt(scanner, "Enter your choice: ");
	        scanner.nextLine();

	        switch (choice) {
	            case 1 -> createAccount();
	            case 2 -> selectAccount();
	            case 3 -> deposit();
	            case 4 -> withdraw();
	            case 5 -> checkBalance();
	            case 6 -> transfer();
	            case 7 -> {
	                System.out.println("Exiting...");
	                return;
	            }
	            default -> System.out.println("Invalid choice!");
	            }
	        }
	    }

    public static void main(String[] args) {
        new BankSystem().start();
    }
}



