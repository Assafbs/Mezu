package money.mezu.mezu;

import java.util.List;

/**
 * Created by asafb on 4/14/2017.
 */

// This Interface will be implemented by the Back-end team
// (may change overtime upon the needs)

public interface BackendInterface {
    // TODO: modify according to the needs

    List<Budget> getUsersBudgets(UserIdentifier uid);
    List<Expense> getExpensesOfBudget(BudgetIdentifier bid);
    void deleteBudget(BudgetIdentifier bid);
    void addBudgetToUser(UserIdentifier uid, BudgetIdentifier bid);
    void addUserToBudget(BudgetIdentifier bid, UserIdentifier uid);
    void addExpenseToBudget(BudgetIdentifier bid, ExpenseIdentifier eid);

}