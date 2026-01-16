package edu.birzeit.a1220775_1221026_courseproject.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import edu.birzeit.a1220775_1221026_courseproject.R;
import edu.birzeit.a1220775_1221026_courseproject.data.Budget;
import edu.birzeit.a1220775_1221026_courseproject.data.BudgetWithSpent;

public class BudgetsAdapter extends RecyclerView.Adapter<BudgetsAdapter.BudgetViewHolder> {
    private List<BudgetWithSpent> budgets;
    private OnBudgetActionListener listener;

    public interface OnBudgetActionListener {
        void onEditBudget(Budget budget);

        void onDeleteBudget(Budget budget);
    }

    public BudgetsAdapter(List<BudgetWithSpent> budgets, OnBudgetActionListener listener) {
        this.budgets = budgets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        BudgetWithSpent budget = budgets.get(position);
        holder.bind(budget);
    }

    @Override
    public int getItemCount() {
        return budgets != null ? budgets.size() : 0;
    }

    public void updateBudgets(List<BudgetWithSpent> newBudgets) {
        this.budgets = newBudgets;
        notifyDataSetChanged();
    }

    class BudgetViewHolder extends RecyclerView.ViewHolder {
        private TextView tvLimit;
        private TextView tvMonth;
        private TextView tvCategory;
        private LinearProgressIndicator pbProgress;
        private TextView tvSpent;
        private TextView tvRemaining;
        private Chip chipAlert;
        private Button btnEdit;
        private Button btnDelete;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLimit = itemView.findViewById(R.id.tvBudgetLimit);
            tvMonth = itemView.findViewById(R.id.tvBudgetMonth);
            tvCategory = itemView.findViewById(R.id.tvBudgetCategory);
            pbProgress = itemView.findViewById(R.id.pbBudgetProgress);
            tvSpent = itemView.findViewById(R.id.tvBudgetSpent);
            tvRemaining = itemView.findViewById(R.id.tvBudgetRemaining);
            chipAlert = itemView.findViewById(R.id.chipBudgetAlert);
            btnEdit = itemView.findViewById(R.id.btnEditBudget);
            btnDelete = itemView.findViewById(R.id.btnDeleteBudget);

            btnEdit.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEditBudget(budgets.get(pos).getBudget());
                }
            });

            btnDelete.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeleteBudget(budgets.get(pos).getBudget());
                }
            });
        }

        public void bind(BudgetWithSpent item) {
            Budget budget = item.getBudget();
            double spent = item.getSpentAmount();
            double limit = budget.getLimitAmount();
            double remaining = limit - spent;
            String categoryName = item.getCategoryName();

            tvLimit.setText(String.format(Locale.getDefault(), "Limit: %.2f", limit));
            String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September",
                    "October", "November", "December" };
            String monthName = (budget.getMonth() >= 1 && budget.getMonth() <= 12) ? months[budget.getMonth() - 1]
                    : String.valueOf(budget.getMonth());
            tvMonth.setText("Month: " + monthName + " 2026");
            tvCategory.setText(categoryName != null ? categoryName : "Category #" + budget.getCategoryId());

            tvSpent.setText(String.format(Locale.getDefault(), "Spent: %.2f", spent));
            tvRemaining.setText(String.format(Locale.getDefault(), "Remaining: %.2f", remaining));

            int progress = (int) ((spent / limit) * 100);
            pbProgress.setProgress(Math.min(progress, 100));

            // Alert Logic
            double alertThreshold = limit * budget.getAlertRatio();
            if (spent >= alertThreshold) {
                tvSpent.setTextColor(android.graphics.Color.RED);
                chipAlert.setText(
                        String.format(Locale.getDefault(), "Budget Alert (%.0f%%)", budget.getAlertRatio() * 100));
                chipAlert.setVisibility(View.VISIBLE);
            } else {
                tvSpent.setTextColor(android.graphics.Color.BLACK);
                chipAlert.setVisibility(View.GONE);
            }
        }
    }
}
