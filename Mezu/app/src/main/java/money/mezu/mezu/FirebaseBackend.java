package money.mezu.mezu;

import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Or on 4/27/2017.
 */

public class FirebaseBackend {
    private DatabaseReference mDatabase;
    private static FirebaseBackend mInstance = null;
    private static HashSet<Pair<String, ValueEventListener>> mPathsIListenTo = new HashSet<>();

    private FirebaseBackend() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    //************************************************************************************************************************************************
    public static FirebaseBackend getInstance() {
        if (mInstance == null)
            mInstance = new FirebaseBackend();

        return mInstance;
    }

    //************************************************************************************************************************************************
    public void startListeningForAllUserBudgetUpdates(UserIdentifier uid) {
        Log.d("", "FirebaseBackend:registerForBudgetUpdates: start");
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users/" + uid.getId().toString() + "/budgets");
        ValueEventListener listener = ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("", String.format("FirebaseBackend:registerForBudgetUpdates: budgets have changed:%s", dataSnapshot.toString()));
                HashMap<String, String> budgets = (HashMap<String, String>) dataSnapshot.getValue();
                if (null == budgets) {
                    EventDispatcher.getInstance().notifyLocalCacheReady();
                    return;
                } else {
                    BudgetsDownloadedNotifier.handleIfFirstExecution(budgets.keySet());
                }
                Log.d("", String.format("FirebaseBackend:registerForBudgetUpdates: budgets have changed:%s", budgets.toString()));
                for (String key : budgets.keySet()) {
                    boolean pathFound = false;
                    for (Pair<String, ValueEventListener> currentPair : mPathsIListenTo) {
                        if (currentPair.first.equals("budgets/" + key + "/budget")) {
                            pathFound = true;
                            break;
                        }
                    }
                    if (!pathFound) {
                        registerForBudgetUpdates(key);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
        mPathsIListenTo.add(Pair.create("users/" + uid.getId().toString() + "/budgets", listener));
    }

    //************************************************************************************************************************************************
    private void registerForBudgetUpdates(String bid) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("budgets/" + bid + "/budget");
        ValueEventListener listener = ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("", String.format("FirebaseBackend:registerForBudgetUpdates: budget has changed: hip hip horay got the following z: %s", dataSnapshot.toString()));
                Budget newBudget = new Budget((HashMap<String, Object>) dataSnapshot.getValue());
                Log.d("", String.format("FirebaseBackend:registerForBudgetUpdates: deserialized budget is: %s", newBudget.toString()));
                EventDispatcher.getInstance().notifyBudgetUpdatedListeners(newBudget);
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
        mPathsIListenTo.add(Pair.create("budgets/" + bid + "/budget", listener));
    }

    //************************************************************************************************************************************************
    public void leaveBudget(String bid, UserIdentifier uid, String userEmail) {
        final String bidToLeave = bid;
        final String uidToUpdate = uid.getId().toString();
        final String emailToRemove = userEmail;
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        stopListeningOnPath("budgets/" + bid + "/budget");
        EventDispatcher.getInstance().notifyUserLeftBudgetListeners(bid);

        DatabaseReference ref = database.getReference("budgets/" + bid);
        final ValueEventListener newListener = ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("", String.format("FirebaseBackend:leaveBudget: onDataChange:start, with bidToLeave: %s", bidToLeave));
                stopListeningOnPath("budgets/" + bidToLeave);
                ArrayList<String> emails = (ArrayList<String>) dataSnapshot.child("budget").child("mEmails").getValue();
                HashMap<String, String> pendingUsers = (HashMap<String, String>) dataSnapshot.child("budget").child("mPending").getValue();
                emails.remove(emailToRemove);
                mDatabase.child("budgets").child(bidToLeave).child("budget").child("mEmails").setValue(emails);
                HashMap<String, Object> uidDict = (HashMap<String, Object>) dataSnapshot.child("/users").getValue();
                mDatabase.child("users").child(uidToUpdate).child("budgets").child(bidToLeave).removeValue();
                mDatabase.child("budgets").child(bidToLeave).child("users").child(uidToUpdate).removeValue();
                // second condition verifies that we were a member of the budget to begin with.
                if (1 == uidDict.size() && uidDict.containsKey(uidToUpdate))
                {
                    Log.d("", "FirebaseBackend::leaveBudget: user is the last one in budget, deleting budget");
                    mDatabase.child("budgets").child(bidToLeave).removeValue();
                    // remove pending references
                    if(null != pendingUsers)
                    {
                        for (String pendingBase64 : pendingUsers.keySet()) {
                            Log.d("", String.format("FirebaseBackend::leaveBudget: removing pending user: %s from budget %s ", pendingBase64, bidToLeave));
                            mDatabase.child("mails").child(pendingBase64).child("pendingBudgets").child(bidToLeave).removeValue();
                        }
                    }
                }
                // This line appears twice to handle a very unlikely race condition (which wasn't witnessed)
                // that can occur if the first invocation occurs before the path is added to the hashmap.
                stopListeningOnPath("budgets/" + bidToLeave);
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
        mPathsIListenTo.add(new Pair<>("budgets/" + bidToLeave, newListener));
    }

    //************************************************************************************************************************************************
    public void editBudget(Budget budget) {
        Log.d("", "FirebaseBackend::editBudget: invoked");
        final Budget budgetToEdit = budget;
        Log.d("", String.format("FirebaseBackend::editBudget: pending list is %s", budget.getPending().toString()));
        mDatabase.child("budgets").child(budgetToEdit.getId()).child("budget").
                setValue(budgetToEdit.serialize());
    }

    //************************************************************************************************************************************************
    public void deleteExpense(String bid, String eid) {
        mDatabase.child("budgets").child(bid).child("budget").child("mExpenses").child(eid).removeValue();
    }

    //************************************************************************************************************************************************
    public void editExpense(String bid, Expense expense) {
        HashMap<String, Object> serializedExpense = expense.serialize();
        mDatabase.child("budgets").child(bid).child("budget").child("mExpenses").
                child(expense.getId()).setValue(serializedExpense);
    }

    //************************************************************************************************************************************************
    public void createBudgetAndAddToUser(Budget budget, UserIdentifier uid) {
        Log.d("", "FirebaseBackend:addBudgetToUser: adding budget to user");
        String newBid = createBudget(budget);
        connectBudgetAndUser(newBid, uid.getId().toString());
    }

    //************************************************************************************************************************************************
    private String createBudget(Budget budget) {
        Log.d("", "FirebaseBackend:createBudget: creating budget");
        DatabaseReference budgetRef = mDatabase.child("budgets").push();
        String bid = budgetRef.getKey();
        budget.setId(bid);
        budgetRef.child("budget").setValue(budget.serializeNoExpenses());
        Log.d("", String.format("FirebaseBackend:createBudget: created budget with id:%s", budget.getId().toString()));
        return bid;
    }

    //************************************************************************************************************************************************
    private void addBudgetToUser(String bid, String uid) {
        Log.d("", String.format("FirebaseBackend:addBudgetToUser: adding budget with id: %s", bid));
        mDatabase.child("users").child(uid).child("budgets").child(bid).setValue(bid);
        Log.d("", "FirebaseBackend:addBudgetToUser: added budget");
    }

    //************************************************************************************************************************************************
    private void addUserToBudget(String bid, String uid) {
        mDatabase.child("budgets").child(bid).child("users").child(uid).setValue(uid);
    }

    //************************************************************************************************************************************************
    public void addExpenseToBudget(Budget budget, Expense expense) {
        DatabaseReference expenseRef = mDatabase.child("budgets").child(budget.getId()).child("budget").child("mExpenses").push();
        String eid = expenseRef.getKey();
        expense.setId(eid);
        HashMap<String, Object> serializedExpense = expense.serialize();
        expenseRef.setValue(serializedExpense);
    }

    //************************************************************************************************************************************************
    public void resetBackend() {
        Log.d("", "FirebaseBackend:resetBackend: stopping");
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        for (Pair<String, ValueEventListener> pathListener : mPathsIListenTo) {
            DatabaseReference ref = database.getReference(pathListener.first);
            Log.d("", String.format("FirebaseBackend:resetBackend: will not listen on:%s", pathListener.first));
            ref.removeEventListener(pathListener.second);
        }
        mPathsIListenTo = new HashSet<>();
        BackendCache.getInstance().clearCache();
        BudgetsDownloadedNotifier.reset();
    }

    //************************************************************************************************************************************************
    private void stopListeningOnPath(String path) {
        HashSet<Pair<String, ValueEventListener>> pairsToDelete = new HashSet<>();
        for (Pair<String, ValueEventListener> currentPair : mPathsIListenTo) {
            if (currentPair.first.equals(path)) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference(currentPair.first);
                Log.d("", String.format("FirebaseBackend:stopListeningOnEvents: will not listen on:%s", currentPair.first));
                ref.removeEventListener(currentPair.second);
                pairsToDelete.add(currentPair);
            }
        }
        for (Pair<String, ValueEventListener> pairToDelete : pairsToDelete) {
            mPathsIListenTo.remove(pairToDelete);
        }
    }

    //************************************************************************************************************************************************
    public void addUserIfNeededAndRefreshToken(UserIdentifier uid, String username, String email) {
        final UserIdentifier lUid = uid;
        final String usernameToAdd = username;
        final String emailToAdd = email;
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users/");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String uidToAdd = lUid.getId().toString();
                if (!dataSnapshot.hasChild(uidToAdd)) {
                    mDatabase.child("users").child(uidToAdd).child("username").setValue(usernameToAdd);
                    mDatabase.child("users").child(uidToAdd).child("email").setValue(emailToAdd);
                    mDatabase.child("mails").child(Base64.encodeToString(emailToAdd.getBytes(), Base64.NO_WRAP)).child("uid").setValue(uidToAdd);
                    setShouldNotifyBudgetExceededThreshold(true, lUid);
                    setShouldNotifyOnTransaction(true, lUid);
                    setMinimalTransactionNotificationValue(0, lUid);
                    setShouldNotifyWhenAddedToBudget(true, lUid);
                }
                mDatabase.child("users").child(uidToAdd).child("notificationToken").setValue(FirebaseInstanceId.getInstance().getToken());
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    //************************************************************************************************************************************************
    public void updateUserNotificationToken(String refreshedToken, UserIdentifier uid) {
        Log.d("", String.format("FirebaseBackend:updateUserNotificationToken: updating notification token:%s", refreshedToken));
        mDatabase.child("users").child(uid.getId().toString()).child("notificationToken").setValue(refreshedToken);
    }

    //************************************************************************************************************************************************
    private void connectBudgetAndUser(String bid, String uid) {
        addBudgetToUser(bid, uid);
        addUserToBudget(bid, uid);
    }

    //************************************************************************************************************************************************
    public void setShouldNotifyOnTransaction(boolean shouldNotify, UserIdentifier uid) {
        mDatabase.child("users").child(uid.getId().toString()).child("settings").child("shouldNotifyOnTransaction").setValue(shouldNotify);
    }

    //************************************************************************************************************************************************
    public void setMinimalTransactionNotificationValue(int minimalNotificationValue, UserIdentifier uid) {
        mDatabase.child("users").child(uid.getId().toString()).child("settings").child("minimalNotificationValue").setValue(minimalNotificationValue);
    }

    //************************************************************************************************************************************************
    public void setShouldNotifyBudgetExceededThreshold(boolean shouldNotify, UserIdentifier uid) {
        mDatabase.child("users").child(uid.getId().toString()).child("settings").child("nofityBudgetExceeded").setValue(shouldNotify);
    }

    //************************************************************************************************************************************************
    public void setShouldNotifyWhenAddedToBudget(boolean shouldNotify, UserIdentifier uid) {
        mDatabase.child("users").child(uid.getId().toString()).child("settings").child("notifyWhenAddedToBudget").setValue(shouldNotify);
    }
}
