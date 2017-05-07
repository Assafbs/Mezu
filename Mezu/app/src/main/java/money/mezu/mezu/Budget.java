package money.mezu.mezu;

import android.util.Log;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by asafb on 4/15/2017.
 */

public class Budget {

    private String mId;
    private ArrayList<Expense> mExpenses;
    private String mName;
    private double mInitialBalance;

    public Budget(String name, double initialBalance)
    {
        super();
        this.mId = "";
        this.mName = name;
        this.mExpenses = new ArrayList<>();
        this.mInitialBalance = initialBalance;
        //TODO: backend to fill
    }

    public Budget(HashMap<String, Object> serializedBudget)
    {
        super();
        Log.d("",String.format("Budget:Budget creating budget from serialized budget: %s", serializedBudget.toString()));
        this.mId = (String)serializedBudget.get("mId");
        this.mName = (String)serializedBudget.get("mName");
        ArrayList<Expense> expenses = new ArrayList<Expense>();
        if (serializedBudget.containsKey("mExpenses"))
        {
            HashMap<String,HashMap<String, Object>> serializedExpenses = (HashMap<String,HashMap<String, Object>>)serializedBudget.get("mExpenses");
            for(HashMap<String, Object> expense :serializedExpenses.values())
            {
                expenses.add(new Expense(expense));
            }
            this.mExpenses = expenses;
        }
        else
        {
            this.mExpenses = new ArrayList<>();
        }
        if (serializedBudget.containsKey("mInitialBalance"))
        {
            this.mInitialBalance = Double.parseDouble(serializedBudget.get("mInitialBalance").toString());
        }
        else
        {
            this.mInitialBalance = 0;
        }
    }

    public String getId() {
        return mId;
    }
    public void setId(String id) {
        this.mId = id;
    }

    public void addExpense(Expense expense)
    {
        mExpenses.add(expense);
    }

    public ArrayList<Expense> getExpenses() {
        return mExpenses;
    }

    public Expense getExpenseByID(BudgetIdentifier bi){
        for (Expense expense:mExpenses) {
            if (expense.getId().equals(bi)){
                return expense;
            }
        }
        //ERROR MESSAGE
        return null;
    }

    public HashMap<String, Object> serializeNoExpenses()
    {
        HashMap<String, Object> serialized = new HashMap<>();
        serialized.put("mId", mId);
        serialized.put("mName", mName);
        serialized.put("mInitialBalance", mInitialBalance);
        return serialized;
    }

    public String toString(){ return mName; }
}
