package money.mezu.mezu;

import android.util.Base64;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class Budget {

    private String mId;
    private ArrayList<Expense> mExpenses;
    private String mName;
    private double mInitialBalance;
    private ArrayList<String> mEmails;
    private HashMap<Category, Double> mCategoryCeilings;
    private String mOwner;
    private HashMap<String, String> mPending;

    //************************************************************************************************************************************************
    public Budget(String name, double initialBalance, ArrayList<String> emails, String owner, ArrayList<String> pending) {
        super();
        this.mId = "";
        this.mName = name;
        this.mExpenses = new ArrayList<>();
        this.mInitialBalance = initialBalance;
        if (emails != null) {
            this.mEmails = emails;
        } else {
            this.mEmails = new ArrayList<>();
        }
        this.mCategoryCeilings = new HashMap<>();
        this.mOwner = owner;
        this.mPending = new HashMap<>();
        this.addNewPending(pending);
    }

    //************************************************************************************************************************************************
    public Budget(HashMap<String, Object> serializedBudget) {
        super();
        Log.d("", String.format("Budget:Budget creating budget from serialized budget: %s", serializedBudget.toString()));
        this.mId = (String) serializedBudget.get("mId");
        this.mName = (String) serializedBudget.get("mName");
        if (serializedBudget.containsKey("mOwner")) {
            this.mOwner = (String) serializedBudget.get("mOwner");
        } else {
            this.mOwner = "";
        }
        this.mPending = new HashMap<>();
        if (serializedBudget.containsKey("mPending")) {
            addNewPendingAndDecode((HashMap<String, String>) serializedBudget.get("mPending"));
        }

        ArrayList<Expense> expenses = new ArrayList<>();
        try {
            if (serializedBudget.containsKey("mExpenses")) {
                HashMap<String, HashMap<String, Object>> serializedExpenses = (HashMap<String, HashMap<String, Object>>) serializedBudget.get("mExpenses");
                for (HashMap<String, Object> expense : serializedExpenses.values()) {
                    expenses.add(new Expense(expense));
                }
                this.mExpenses = expenses;
            } else {
                this.mExpenses = new ArrayList<>();
            }
        } catch (ClassCastException e) {
            Log.d("", String.format("Budget:expenses array is corrupted in budget: %s", serializedBudget.toString()));
            this.mExpenses = new ArrayList<>();
        }
        this.mCategoryCeilings = new HashMap<>();
        if (serializedBudget.containsKey("mCategoryCeilings")) {
            try
            {
                HashMap<String, String> serializedCategory = (HashMap<String, String>) serializedBudget.get("mCategoryCeilings");
                for (String key : serializedCategory.keySet())
                {
                    this.mCategoryCeilings.put(Category.valueOf(key), Double.parseDouble(serializedCategory.get(key)));
                }
            }
            catch(Exception e)
            {
                HashMap<String, Double> serializedCategory = (HashMap<String, Double>) serializedBudget.get("mCategoryCeilings");
                for (String key : serializedCategory.keySet())
                {
                    this.mCategoryCeilings.put(Category.valueOf(key), serializedCategory.get(key));
                }
            }
        }

        if (serializedBudget.containsKey("mInitialBalance")) {
            this.mInitialBalance = Double.parseDouble(serializedBudget.get("mInitialBalance").toString());
        } else {
            this.mInitialBalance = 0;
        }
        this.mEmails = (ArrayList<String>) serializedBudget.get("mEmails");
    }

    //************************************************************************************************************************************************
    public void addNewPendingAndDecode(HashMap<String, String> pendingToDecode) {
        for (String pending : pendingToDecode.keySet()) {
            this.mPending.put(new String(Base64.decode(pending.getBytes(), Base64.NO_WRAP)), pendingToDecode.get(pending));
        }
    }

    //************************************************************************************************************************************************
    public HashMap<String, String> getEncodedPending() {
        HashMap<String, String> encodedPending = new HashMap<>();
        for (String pending : this.mPending.keySet()) {
            encodedPending.put(Base64.encodeToString(pending.getBytes(), Base64.NO_WRAP), pending);
        }
        return encodedPending;
    }

    //************************************************************************************************************************************************
    public void addNewPending(ArrayList<String> newPending) {
        for (String pending : newPending) {
            this.mPending.put(pending, pending);
        }
    }

    //************************************************************************************************************************************************
    public void setCeilingForCategory(Category category, Double ceiling) {
        this.mCategoryCeilings.put(category, ceiling);
    }

    //************************************************************************************************************************************************
    private HashMap<Category, Double> getCategoryCeilings() {
        return this.mCategoryCeilings;
    }

    //************************************************************************************************************************************************
    public String getId() {
        return this.mId;
    }

    //************************************************************************************************************************************************
    public ArrayList<String> getEmails() {
        return this.mEmails;
    }

    //************************************************************************************************************************************************
    public void setId(String id) {
        this.mId = id;
    }

    //************************************************************************************************************************************************
    public void setName(String name) {
        this.mName = name;
    }

    //************************************************************************************************************************************************
    public void setInitialBalance(double balance) {
        this.mInitialBalance = balance;
    }

    //************************************************************************************************************************************************
    public ArrayList<Expense> getExpenses() {
        return mExpenses;
    }

    //************************************************************************************************************************************************
    private String getOwner() {
        return this.mOwner;
    }

    //************************************************************************************************************************************************
    public HashMap<String, String> getPending() {
        return this.mPending;
    }

    //************************************************************************************************************************************************
    public void setExpenses(ArrayList<Expense> newExpenses) {
        this.mExpenses = newExpenses;
    }

    //************************************************************************************************************************************************
    public HashMap<String, Object> serializeNoExpenses() {
        HashMap<String, Object> serialized = new HashMap<>();
        serialized.put("mId", mId);
        serialized.put("mName", mName);
        serialized.put("mInitialBalance", mInitialBalance);
        serialized.put("mEmails", mEmails);
        serialized.put("mOwner", mOwner);
        serialized.put("mPending", this.getEncodedPending());
        Log.d("", String.format("Budget:serializeNoExpenses this is the pending list: %s", ((HashMap<String, String>) serialized.get("mPending")).keySet().toString()));
        HashMap<String, String> translatedCategoryCeilings = new HashMap<>();
        for (Category key : this.mCategoryCeilings.keySet()) {
            translatedCategoryCeilings.put(key.toString(), this.mCategoryCeilings.get(key).toString());
        }
        serialized.put("mCategoryCeilings", translatedCategoryCeilings);
        return serialized;
    }

    //************************************************************************************************************************************************
    public HashMap<String, Object> serialize() {
        HashMap<String, Object> serialized = this.serializeNoExpenses();
        if (this.mExpenses.isEmpty()) {
            return serialized;
        }
        HashMap<String, HashMap<String, Object>> expenses = new HashMap<String, HashMap<String, Object>>();
        for (Expense expense : mExpenses) {
            expenses.put(expense.getId(), expense.serialize());
        }
        serialized.put("mExpenses", expenses);
        return serialized;
    }

    //************************************************************************************************************************************************
    public void setFromBudget(Budget budget) {
        this.mId = budget.getId();
        this.mName = budget.getName();
        this.mExpenses = budget.getExpenses();
        this.mInitialBalance = budget.getInitialBalance();
        this.mEmails = budget.getEmails();
        this.mOwner = budget.getOwner();
        this.mPending = budget.getPending();
    }

    //************************************************************************************************************************************************
    public double getInitialBalance() {
        return this.mInitialBalance;
    }

    //************************************************************************************************************************************************
    public double getCurrentBalance() {
        return getInitialBalance() + getTotalIncomes() - getTotalExpenses();
    }

    //************************************************************************************************************************************************
    public double getTotalExpenses() {
        double acc = 0;
        for (Expense expense : mExpenses) {
            if (expense.getIsExpense()) {
                acc += expense.getAmount();
            }
        }
        return acc;
    }

    //************************************************************************************************************************************************
    public double getTotalExpensesPerMonth(int month, int year) {
        double acc = 0;
        Calendar c = Calendar.getInstance();
        for (Expense expense : mExpenses) {
            if (expense.getIsExpense() && expense.getMonth() == month && expense.getYear() == year)
            {
                acc += expense.getAmount();
            }
        }
        return acc;
    }

    //************************************************************************************************************************************************
    public double getTotalIncomes() {
        double acc = 0;
        for (Expense expense : mExpenses) {
            if (!expense.getIsExpense()) {
                acc += expense.getAmount();
            }
        }
        return acc;
    }

    //************************************************************************************************************************************************
    public Category getMostExpensiveCategory() {
        Category maxCategory = Category.OTHER;
        double categoryArray[] = new double[Category.values().length];
        for (int i = 0; i < Category.values().length; i++) {
            categoryArray[i] = 0;
        }
        double max = 0.0;
        double amount;
        for (Expense expense : mExpenses) {
            amount = expense.getAmount();
            if (expense.getIsExpense()) {
                categoryArray[expense.getCategory().getValue()] += amount;
                amount = categoryArray[expense.getCategory().getValue()];
                if (amount > max) {
                    max = amount;
                    maxCategory = expense.getCategory();
                }
            }
        }
        return maxCategory;
    }

    //************************************************************************************************************************************************
    public double getTotalExpensesPerCategory(Category category) {
        double acc = 0;
        for (Expense expense : mExpenses) {
            if (expense.getIsExpense() && expense.getCategory().equals(category)) {
                acc += expense.getAmount();
            }
        }
        return acc;
    }

    //************************************************************************************************************************************************
    public double getTotalExpensesPerCategoryAndMonth(Category category,int month, int year) {
        double acc = 0;
        Calendar c = Calendar.getInstance();
        for (Expense expense : mExpenses) {
            if (expense.getIsExpense() && expense.getCategory().equals(category)
                    && expense.getMonth() == month && expense.getYear() == year) {
                acc += expense.getAmount();
            }
        }
        return acc;
    }

    //************************************************************************************************************************************************
    public String getName() {
        return this.mName;
    }

    //************************************************************************************************************************************************
    public String toString() {
        return mName;
    }

    //************************************************************************************************************************************************
    public int getMostExpensiveMonthPerYear(int year) {
        int maxMonth = 1;
        double monthArray[] = new double[12];
        for (int i = 0; i < 12; i++) {
            monthArray[i] = 0;
        }
        double max = 0.0;
        double amount;
        for (Expense expense : mExpenses) {
            amount = expense.getAmount();
            if (expense.getIsExpense() && expense.getYear() == year) {
                monthArray[expense.getMonth()] += amount;
                amount = monthArray[expense.getMonth()];
                if (amount > max) {
                    max = amount;
                    maxMonth = expense.getMonth();
                }
            }
        }
        return maxMonth;
    }

    //************************************************************************************************************************************************
    public ArrayList<String> getArrayOfUserNamesExpensesOnly() {
        ArrayList<String> users = new ArrayList<>();
        boolean exists = false;

        for (Expense expense : mExpenses) {
            if (expense.getIsExpense()) {
                for (int i = 0; i < users.size(); i++) {
                    if (users.get(i).equals(expense.getUserName())) {
                        exists = true;
                    }
                }
                if (!exists) {
                    users.add(expense.getUserName());
                }
                exists = false;
            }
        }
        return users;
    }

    //************************************************************************************************************************************************
    public double getAmountPerUserName(String user) {
        double acc = 0;
        for (Expense expense : mExpenses) {
            if (expense.getIsExpense() && expense.getUserName().equals(user)) {
                acc += expense.getAmount();
            }
        }
        return acc;
    }

    //************************************************************************************************************************************************
    public String getMostExpensiveUser() {
        ArrayList<String> users = getArrayOfUserNamesExpensesOnly();
        if (users.isEmpty()) {
            return "";
        }
        String maxUser = users.get(0);
        double userAmount;
        double maxAmount = getAmountPerUserName(users.get(0));
        int i = 0;

        for (String user : users) {
            userAmount = getAmountPerUserName((users.get(i)));
            if (userAmount > maxAmount) {
                maxAmount = userAmount;
                maxUser = user;
            }
            i++;
        }
        return maxUser;
    }

    //************************************************************************************************************************************************
    public boolean isEstimatedToOverSpendThisMonth() {
        double monthExpensesTotal = 0;
        double incomeThisMonth = 0;
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
        for (Expense expense : this.mExpenses) {
            if (expense.getMonth() == currentMonth && expense.getYear() == currentYear)
            {
                if (expense.getIsExpense())
                {
                    monthExpensesTotal += expense.getAmount();
                }
                else
                {
                    incomeThisMonth += expense.getAmount();
                }
            }

        }

        monthExpensesTotal -= incomeThisMonth  / 2;
        double relativeBudgetLeft = incomeThisMonth * (currentDay + 1) / (2 * (daysInMonth + 1));
        return monthExpensesTotal > relativeBudgetLeft;
    }

    //************************************************************************************************************************************************
    public double tryGetCategoryCeiling(Category category) {
        HashMap<Category, Double> categoryCeilings = this.getCategoryCeilings();
        if (categoryCeilings == null)
        {
            return -1;
        }
        if (null == categoryCeilings.get(category))
        {
            return -1;
        }

        return categoryCeilings.get(category);
    }

    //************************************************************************************************************************************************
    public boolean expensesDiffer(Budget budgetToCompare) {
        for (Expense myExpense : this.getExpenses()) {
            boolean foundMatch = false;
            for (Expense theirExpense : budgetToCompare.getExpenses()) {
                if (myExpense.getId().equals(theirExpense.getId())) {
                    foundMatch = true;
                    if (myExpense.expenseDiffers(theirExpense)) {
                        return true;
                    }
                    break;
                }
            }
            if (!foundMatch) {
                return true;
            }
        }
        return false;
    }

}
