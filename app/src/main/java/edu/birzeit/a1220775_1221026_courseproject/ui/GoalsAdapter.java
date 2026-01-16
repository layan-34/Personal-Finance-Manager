package edu.birzeit.a1220775_1221026_courseproject.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.birzeit.a1220775_1221026_courseproject.R;
import edu.birzeit.a1220775_1221026_courseproject.data.Goal;

public class GoalsAdapter extends RecyclerView.Adapter<GoalsAdapter.GoalViewHolder> {
    private List<Goal> goals;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private OnGoalActionListener listener;

    public interface OnGoalActionListener {
        void onEditGoal(Goal goal);

        void onDeleteGoal(Goal goal);

        void onAddFunds(Goal goal);
    }

    public GoalsAdapter(List<Goal> goals, OnGoalActionListener listener) {
        this.goals = goals;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_goal, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal goal = goals.get(position);
        holder.bind(goal);
    }

    @Override
    public int getItemCount() {
        return goals != null ? goals.size() : 0;
    }

    public void updateGoals(List<Goal> newGoals) {
        this.goals = newGoals;
        notifyDataSetChanged();
    }

    class GoalViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private TextView tvTarget;
        private TextView tvSaved;
        private TextView tvRemaining;
        private Chip chipStatus;
        private TextView tvDate;
        private Chip chipAlert;
        private LinearProgressIndicator progressBar;
        private Button btnEdit;
        private Button btnDelete;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvGoalName);
            tvTarget = itemView.findViewById(R.id.tvGoalTarget);
            tvSaved = itemView.findViewById(R.id.tvGoalSaved);
            tvRemaining = itemView.findViewById(R.id.tvGoalRemaining);
            chipStatus = itemView.findViewById(R.id.chipGoalStatus);
            tvDate = itemView.findViewById(R.id.tvGoalDate);
            chipAlert = itemView.findViewById(R.id.chipGoalAlert);
            progressBar = itemView.findViewById(R.id.progressBar);
            btnEdit = itemView.findViewById(R.id.btnEditGoal);
            btnDelete = itemView.findViewById(R.id.btnDeleteGoal);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onAddFunds(goals.get(getAdapterPosition()));
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onEditGoal(goals.get(getAdapterPosition()));
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onDeleteGoal(goals.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Goal goal) {
            tvName.setText(goal.getName());
            tvTarget.setText(String.format(Locale.getDefault(), "Target: %.2f", goal.getTargetAmount()));

            double saved = goal.getCurrentAmount();
            double target = goal.getTargetAmount();
            double remaining = Math.max(0, target - saved);
            int percentage = (int) ((saved / target) * 100);

            tvSaved.setText(String.format(Locale.getDefault(), "Saved: %.2f (%d%%)", saved, percentage));
            tvRemaining.setText(String.format(Locale.getDefault(), "Remaining: %.2f", remaining));

            chipStatus.setText(goal.getStatus());

            if (goal.getTargetDate() != null) {
                Date date = new Date(goal.getTargetDate());
                tvDate.setText(dateFormat.format(date));
            } else {
                tvDate.setText("No target date");
            }

            int p = (int) ((saved / target) * 100);
            progressBar.setProgress(Math.min(p, 100));

            // Alert / Progress Logic
            if (p >= 100) {
                chipAlert.setText(" Milestone Reached!");
                chipAlert.setVisibility(View.VISIBLE);
            } else if (p >= 50) {
                chipAlert.setText(" Halfway there!");
                chipAlert.setVisibility(View.VISIBLE);
            } else {
                chipAlert.setVisibility(View.GONE);
            }
        }
    }
}
