package money.mezu.mezu;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by davidled on 21/04/2017.
 */

public class BudgetViewActivity extends AppCompatActivity {
    protected static Budget currentBudget;

    private SessionManager sessionManager;
    GoogleApiClient mGoogleApiClient;
    private ExpenseAdapter mAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView budgetName = (TextView) findViewById(R.id.budgetViewName);
        budgetName.setText(currentBudget.toString());


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_expense);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(BudgetViewActivity.this);
            }
        });


        // Create the adapter to convert the array to views
        mAdapter = new ExpenseAdapter(this, currentBudget.getExpenses());
        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.expenses_list);
        listView.setAdapter(mAdapter);

        sessionManager = new SessionManager(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                    }
                }).addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }

    public static void setCurrentBudget(Budget budget) {
        currentBudget = budget;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Toast.makeText(this, "Open Settings ", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.action_log_out) {
            logout();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPopup(final Activity context)
    {
        // Inflate the popup_layout.xml
        LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.activity_add_expense);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.inflate(R.layout.activity_add_expense, viewGroup);

        // Creating the PopupWindow
        final PopupWindow popUp = new PopupWindow(context);
        popUp.setContentView(layout);
        popUp.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        popUp.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        popUp.setFocusable(true);

        popUp.showAtLocation(layout, Gravity.CENTER,0,0);
        Button add_btn=(Button)layout.findViewById(R.id.add_action_btn);
        add_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                EditText amountField = (EditText)layout.findViewById(R.id.EditTextAmount);
                Spinner categorySpinner =(Spinner) layout.findViewById(R.id.SpinnerCategoriesType);

                Category category = Category.getCategoryFromString(categorySpinner.getSelectedItem().toString());
                EditText title = (EditText)layout.findViewById(R.id.EditTextTitle);
                Expense newExpense = new Expense("", Double.parseDouble(amountField.getText().toString()), title.getText().toString(), category, Calendar.getInstance().getTime());
                FirebaseBackend.getInstance().addExpenseToBudget(currentBudget, newExpense);
                currentBudget.addExpense(newExpense);
                popUp.dismiss();
            }
        });
    }

    private void logout() {
        if (sessionManager.getLoginType().equals("Google")) {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            // ...
                        }
                    });
        }
        sessionManager.logoutUser();
    }
}
