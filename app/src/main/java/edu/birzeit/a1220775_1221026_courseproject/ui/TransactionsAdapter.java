package edu.birzeit.a1220775_1221026_courseproject.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.birzeit.a1220775_1221026_courseproject.R;
import edu.birzeit.a1220775_1221026_courseproject.data.Category;
import edu.birzeit.a1220775_1221026_courseproject.data.Transaction;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder> {
    private List<Transaction> transactions;
    private Map<Integer, String> categoryCache = new HashMap<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public interface OnTransactionClickListener {
        void onEditClick(Transaction transaction);

        void onDeleteClick(Transaction transaction);
    }

    private OnTransactionClickListener listener;

    public TransactionsAdapter(List<Transaction> transactions, OnTransactionClickListener listener) {
        this.transactions = transactions;
        this.listener = listener;
    }

    public TransactionsAdapter(List<Transaction> transactions) {
        this(transactions, null);
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions != null ? transactions.size() : 0;
    }

    public void updateTransactions(List<Transaction> newTransactions) {
        this.transactions = newTransactions;
        notifyDataSetChanged();
    }

    public void setCategoryCache(Map<Integer, String> cache) {
        this.categoryCache = cache;
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCategory;
        private TextView tvDescription;
        private TextView tvDate;
        private TextView tvAmount;
        private android.widget.ImageButton btnEdit, btnDelete;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvTransactionCategory);
            tvDescription = itemView.findViewById(R.id.tvTransactionDescription);
            tvDate = itemView.findViewById(R.id.tvTransactionDate);
            tvAmount = itemView.findViewById(R.id.tvTransactionAmount);
            btnEdit = itemView.findViewById(R.id.btnEditTransaction);
            btnDelete = itemView.findViewById(R.id.btnDeleteTransaction);
        }

        public void bind(Transaction transaction) {
            // Get category name from cache or show ID
            String categoryName = categoryCache.get(transaction.getCategoryId());
            if (categoryName == null) {
                categoryName = "Category #" + transaction.getCategoryId();
            }
            tvCategory.setText(categoryName);

            tvDescription.setText(transaction.getDescription() != null && !transaction.getDescription().isEmpty()
                    ? transaction.getDescription()
                    : "No description");

            Date date = new Date(transaction.getDate());
            tvDate.setText(dateFormat.format(date));

            String amountText = String.format(Locale.getDefault(), "%.2f", transaction.getAmount());
            if ("INCOME".equals(transaction.getType())) {
                tvAmount.setText("+" + amountText);
                tvAmount.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvAmount.setText("-" + amountText);
                tvAmount.setText("-" + amountText);
                tvAmount.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            }

            btnEdit.setOnClickListener(v -> {
                if (listener != null)
                    listener.onEditClick(transaction);
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null)
                    listener.onDeleteClick(transaction);
            });
        }
    }
}