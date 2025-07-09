import java.util.HashMap;
import java.util.Map;

public class TransactionManager {
    private final BankingDatabase db;
    private final Map<Integer, Object> accountLocks = new HashMap<>();

    public TransactionManager() {
        db = new BankingDatabase();
    }

    private synchronized Object getLock(int accountId) {
        return accountLocks.computeIfAbsent(accountId, k -> new Object());
    }

    public void depositAsync(int accountId, double amount, Runnable onSuccess, Runnable onFailure) {
        new Thread(() -> {
            synchronized (getLock(accountId)) {
                if (db.deposit(accountId, amount)) {
                    onSuccess.run();
                } else {
                    onFailure.run();
                }
            }
        }).start();
    }

    public void withdrawAsync(int accountId, double amount, Runnable onSuccess, Runnable onFailure) {
        new Thread(() -> {
            synchronized (getLock(accountId)) {
                if (db.withdraw(accountId, amount)) {
                    onSuccess.run();
                } else {
                    onFailure.run();
                }
            }
        }).start();
    }

    public void transferAsync(int senderId, int receiverId, double amount, Runnable onSuccess, Runnable onFailure) {
        Object lock1 = getLock(Math.min(senderId, receiverId));
        Object lock2 = getLock(Math.max(senderId, receiverId));   //prevent deadlock by locking both sender and receiver in a fixed order

        new Thread(() -> {
            synchronized (lock1) {
                synchronized (lock2) {
                    if (db.transferMoney(senderId, receiverId, amount)) {
                        onSuccess.run();
                    } else {
                        onFailure.run();
                    }
                }
            }
        }).start();
    }

    public String getAccountDetails(int accountId) {
        return db.getAccountDetails(accountId);
    }

    public String getAllOtherAccounts(int excludeId) {
        return db.getAllOtherAccounts(excludeId);
    }

    public String getAllUserDetails() {
        return db.getAllUserDetails();
    }
}
