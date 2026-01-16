package edu.birzeit.a1220775_1221026_courseproject.ui.transactions;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import edu.birzeit.a1220775_1221026_courseproject.data.Transaction;
import edu.birzeit.a1220775_1221026_courseproject.repository.TransactionRepository;
import edu.birzeit.a1220775_1221026_courseproject.repository.UserRepository;

public class TransactionsViewModel extends AndroidViewModel {
    private TransactionRepository transactionRepository;
    private UserRepository userRepository;
    private MutableLiveData<List<Transaction>> transactions = new MutableLiveData<>();
    private MutableLiveData<String> filterType = new MutableLiveData<>("ALL"); // "ALL", "INCOME", "EXPENSE"
    private MutableLiveData<String> sortOrder = new MutableLiveData<>("DESC"); // "DESC", "ASC"

    public TransactionsViewModel(@NonNull Application application) {
        super(application);
        transactionRepository = new TransactionRepository(application);
        userRepository = new UserRepository(application);
        loadTransactions();
    }

    public LiveData<List<Transaction>> getTransactions() {
        return transactions;
    }

    public LiveData<String> getFilterType() {
        return filterType;
    }

    public void loadTransactions() {
        String userEmail = userRepository.getCurrentUser();
        if (userEmail == null || userEmail.isEmpty()) {
            return;
        }

        String type = filterType.getValue();
        String order = sortOrder.getValue();
        List<Transaction> transactionList;
        if (type == null || "ALL".equals(type)) {
            transactionList = transactionRepository.getTransactionsByUser(userEmail, order);
        } else {
            transactionList = transactionRepository.getTransactionsByUserAndType(userEmail, type);
        }
        transactions.setValue(transactionList);
    }

    public void setFilterType(String type) {
        filterType.setValue(type);
        loadTransactions();
    }

    public void setSortOrder(String order) {
        sortOrder.setValue(order);
        loadTransactions();
    }

    public void deleteTransaction(Transaction transaction) {
        transactionRepository.deleteTransaction(transaction);
        loadTransactions();
    }

    public void updateTransaction(Transaction transaction) {
        transactionRepository.updateTransaction(transaction);
        loadTransactions();
    }

    public void refresh() {
        loadTransactions();
    }
}
