package money.mezu.mezu;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ExpenseAdapter extends ArrayAdapter<Expense> {
    Context mContext;
    private BudgetViewActivity mActivity;

    public ExpenseAdapter(Context context, ArrayList<Expense> expenses) {
        super(context, 0, expenses);
        mContext = context;
        mActivity = (BudgetViewActivity) context;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        Expense expense = getItem(position);
        assert expense != null;
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_expense, parent, false);
        }
        // Lookup view for data population
        TextView category = (TextView) convertView.findViewById(R.id.expenseCategory);
        TextView amount = (TextView) convertView.findViewById(R.id.expenseAmount);
        TextView title = (TextView) convertView.findViewById(R.id.expenseTitle);
        LinearLayout expenseRow = (LinearLayout) convertView.findViewById(R.id.expenseRow);
        expenseRow.setTag(expense);

        // Populate the data into the template view using the data object
        if (expense.getCategory() != null) {
            category.setText(expense.getCategory().toNiceString());
        } else {
            category.setText(R.string.category_other);
        }
        char sign;
        if (!expense.getIsExpense()) {
            sign = '+';
            amount.setTextColor(ContextCompat.getColor(mContext, R.color.income_green));
        } else {
            sign = '-';
            amount.setTextColor(ContextCompat.getColor(mContext, R.color.expense_red));
        }
        String expAmount = Double.toString(expense.getAmount());
        amount.setText(LanguageUtils.isRTL() ? expAmount + " " + sign : sign + " " + expAmount);

        String t_title = expense.getTitle();
        if (t_title == null) {
            title.setText(R.string.general);
        } else {
            title.setText(expense.getTitle());
        }

        expenseRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Expense expense = (Expense) view.getTag();
                showExpense(expense);
            }
        });

        // Return the complete view to render on screen
        return convertView;
    }

    private void showExpense(Expense expense) {
        ExpenseFragment expenseFragment = new ExpenseFragment();
        expenseFragment.setShowExpense(expense);
        mActivity.setExpenseFragment(expenseFragment);
    }
}
