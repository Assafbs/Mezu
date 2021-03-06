package money.mezu.mezu;

import android.util.Log;

import java.util.HashSet;

/**
 * Created by JB on 5/10/17.
 */

public class EventDispatcher {
    private HashSet<BudgetUpdatedListener> mBudgetUpdatedListeners;
    private HashSet<ExpenseUpdatedListener> mExpenseUpdatedListeners;
    private HashSet<UserLeftBudgetListener> mUserLeftBudgetListeners;
    private HashSet<LocalCacheReadyListener> mLocalCacheReadyListeners;
    private static EventDispatcher mInstance = null;

    private EventDispatcher() {
        mBudgetUpdatedListeners = new HashSet<>();
        mExpenseUpdatedListeners = new HashSet<>();
        mUserLeftBudgetListeners = new HashSet<>();
        mLocalCacheReadyListeners = new HashSet<>();
    }

    //************************************************************************************************************************************************
    public static EventDispatcher getInstance() {
        if (null == mInstance) {
            mInstance = new EventDispatcher();
        }
        return mInstance;
    }

    //************************************************************************************************************************************************
    public void registerBudgetUpdateListener(BudgetUpdatedListener newListener) {
        if (mBudgetUpdatedListeners.contains(newListener)) {
            Log.d("", "EventDispatcher::registerBudgetUpdateListener: registering already registered listener");
        }
        mBudgetUpdatedListeners.add(newListener);
    }

    //************************************************************************************************************************************************
    public void unregisterBudgetUpdatedListener(BudgetUpdatedListener listener) {
        mBudgetUpdatedListeners.remove(listener);
    }

    //************************************************************************************************************************************************
    public void registerExpenseUpdateListener(ExpenseUpdatedListener newListener) {
        mExpenseUpdatedListeners.add(newListener);
    }

    //************************************************************************************************************************************************
    public void registerUserLeftBudgetListener(UserLeftBudgetListener newListener) {
        mUserLeftBudgetListeners.add(newListener);
    }

    //************************************************************************************************************************************************
    public void registerLocalCacheReadyListener(LocalCacheReadyListener newListener) {
        mLocalCacheReadyListeners.add(newListener);
    }

    //************************************************************************************************************************************************
    public void notifyUserLeftBudgetListeners(String bid) {
        for (UserLeftBudgetListener listener : mUserLeftBudgetListeners) {
            listener.userLeftBudgetCallback(bid);
        }
    }

    //************************************************************************************************************************************************
    public void notifyBudgetUpdatedListeners(Budget newBudget) {
        // This is used because some of the listeners might want to unregister when we invoke them.
        // This pattern should be applied whenever an event that supports unregister is invoked.
        HashSet<BudgetUpdatedListener> localListenersList = (HashSet<BudgetUpdatedListener>) mBudgetUpdatedListeners.clone();
        for (BudgetUpdatedListener listener : localListenersList) {
            listener.budgetUpdatedCallback(newBudget);
        }
    }

    //************************************************************************************************************************************************
    public void notifyExpenseUpdatedListeners() {
        Log.d("", "EventDispatcher::notifyExpenseUpdatedListeners dispatching");
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            Log.d("", String.format("EventDispatcher::notifyExpenseUpdatedListeners: %s", ste));
        }

        for (ExpenseUpdatedListener listener : mExpenseUpdatedListeners) {
            listener.expenseUpdatedCallback();
        }
    }

    //************************************************************************************************************************************************
    public void notifyLocalCacheReady() {
        for (LocalCacheReadyListener listener : mLocalCacheReadyListeners) {
            listener.localCacheReadyCallback();
        }
    }

}
