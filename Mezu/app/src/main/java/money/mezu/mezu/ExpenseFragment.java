package money.mezu.mezu;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class ExpenseFragment extends Fragment {
    private View mView;

    EditText mEditTextAmount;
    EditText mEditTextTitle;
    Spinner mCategorySpinner;
    EditText mEditTextDate;
    EditText mEditTextTime;
    EditText mEditTextDescription;
    RadioGroup mRGIsExpense;
    RadioButton mRBExpense;
    RadioButton mRBIncome;
    Button mAddButton;
    ImageView mDeleteBtn;
    ImageView mEditBtn;
    LinearLayout mLinearLayoutEdit;
    TextView mTitleView;

    private int mYear, mMonth, mDay, mHour, mMinute;
    private Calendar c;

    private boolean incomeSelected = false;
    private Category incomeCat = Category.OTHER;
    private Category expenseCat = Category.OTHER;
    private ArrayAdapter<String> incomeAdapter;
    private ArrayAdapter<String> expenseAdapter;

    private Expense expenseToShow;

    public boolean isAdd;

    private BudgetViewActivity mActivity;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = (BudgetViewActivity) getActivity();
        mView = inflater.inflate(R.layout.activity_add_expense, null);

        mEditTextAmount = (EditText) mView.findViewById(R.id.EditTextAmount);
        mEditTextTitle = (EditText) mView.findViewById(R.id.EditTextTitle);
        mCategorySpinner = (Spinner) mView.findViewById(R.id.SpinnerCategoriesType);
        mEditTextDate = (EditText) mView.findViewById(R.id.EditTextDate);
        mEditTextTime = (EditText) mView.findViewById(R.id.EditTextTime);
        mEditTextDescription = (EditText) mView.findViewById(R.id.EditTextDescription);
        mRGIsExpense = (RadioGroup) mView.findViewById(R.id.radio_expense_group);
        mAddButton = (Button) mView.findViewById(R.id.add_action_btn);

        if (isAdd) {
            setupAddOrEditExpense(true);
        } else {
            setupShowExpense();
        }

        return mView;
    }

    public void setShowExpense(Expense expenseToShow) {
        this.expenseToShow = expenseToShow;
        isAdd = false;
    }

    private void setupShowExpense() {
        String titleString = expenseToShow.getTitle();
        mTitleView = (TextView) mView.findViewById(R.id.add_expense_title);
        mLinearLayoutEdit = (LinearLayout) mView.findViewById(R.id.edit_expense_layout);
        mDeleteBtn = (ImageView) mView.findViewById(R.id.delete_expense);
        mEditBtn = (ImageView) mView.findViewById(R.id.edit_expense);

        mTitleView.setText(titleString);
        mTitleView.setVisibility(View.VISIBLE);
        mLinearLayoutEdit.setVisibility(View.VISIBLE);
        mLinearLayoutEdit.setClickable(true);
        mDeleteBtn.setVisibility(View.VISIBLE);
        mDeleteBtn.bringToFront();
        mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("", "ExpenseFragment: clicked delete expense");
                deleteExpense();
            }
        });
        mEditBtn.setVisibility(View.VISIBLE);
        mEditBtn.bringToFront();
        mEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("", "ExpenseFragment: clicked edit expense");
                setupAddOrEditExpense(false);
            }
        });

        if (expenseToShow.getAmount() == 0.0) {
            mEditTextAmount.setText("0.0");
        } else {
            mEditTextAmount.setText("" + expenseToShow.getAmount());
        }

        ArrayList<String> categories = new ArrayList<>();
        categories.add(expenseToShow.getCategory().toString());
        mCategorySpinner.setAdapter(new ArrayAdapter<String>(mActivity, R.layout.category_spinner_item, categories));

        mRBExpense = (RadioButton) mView.findViewById(R.id.radio_expense);
        mRBIncome = (RadioButton) mView.findViewById(R.id.radio_income);
        mRBExpense.setClickable(false);
        mRBIncome.setClickable(false);

        if (expenseToShow.getIsExpense()) {
            mRBExpense.setChecked(true);
            mRBIncome.setChecked(false);
        } else {
            mRBExpense.setChecked(false);
            mRBIncome.setChecked(true);
        }

        mEditTextTitle.setText(getResources().getString(R.string.added_by) + " " + expenseToShow.getUserName());
        mEditTextTitle.setHint("");

        mEditTextDescription.setText(expenseToShow.getDescription());
        mEditTextDescription.setHint("");

        mEditTextDate.setText(DateFormat.getDateInstance().format(expenseToShow.getTime()));
        mEditTextTime.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(expenseToShow.getTime()));

        ViewGroup viewGroup = (ViewGroup) mView.findViewById(R.id.activity_add_expense);
        disableAllFields(viewGroup);

        mAddButton.setVisibility(View.INVISIBLE);
    }

    private void disableAllFields(ViewGroup viewGroup) {
        for (int i = viewGroup.getChildCount() - 1; i >= 0; i--) {
            final View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup)
                disableAllFields((ViewGroup) child);
            if (child != null) {
                if (child.getId() != R.id.delete_expense && child.getId() != R.id.edit_expense) {
                    child.setEnabled(false);
                }
            }
        }
    }

    private void enableAllFields(ViewGroup viewGroup) {
        for (int i = viewGroup.getChildCount() - 1; i >= 0; i--) {
            final View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup)
                enableAllFields((ViewGroup) child);
            if (child != null) {
                if (child.getId() != R.id.delete_expense) {
                    child.setEnabled(true);
                }
            }
        }
    }

    private void setupAddOrEditExpense(final boolean isAdd) {
        if (isAdd) {
            mEditTextAmount.post(new Runnable() {
                public void run() {
                    mEditTextAmount.requestFocusFromTouch();
                    InputMethodManager lManager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    lManager.showSoftInput(mEditTextAmount, 0);
                }
            });
        } else {
            mTitleView.setText(getResources().getString(R.string.edit_expense));
            mEditTextTitle.setText(expenseToShow.getTitle());
            mAddButton.setVisibility(View.VISIBLE);
            mAddButton.setText(getResources().getString(R.string.save));
            mEditBtn.setColorFilter(ContextCompat.getColor(getContext(), R.color.secondary_text));
            ViewGroup viewGroup = (ViewGroup) mView.findViewById(R.id.activity_add_expense);
            enableAllFields(viewGroup);
            mRBExpense.setClickable(true);
            mRBIncome.setClickable(true);
        }

        c = Calendar.getInstance();
        if (isAdd) {
            // Get Current Time
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);
            // Set Current Time to EditTexts
            mEditTextDate.setText(DateFormat.getDateInstance().format(c.getTime()));
            mEditTextTime.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime()));
        } else {
            // Get Expense Time
            mYear = expenseToShow.getYear();
            mMonth = expenseToShow.getMonth();
            mDay = expenseToShow.getDay();
            mHour = expenseToShow.getHour();
            mMinute = expenseToShow.getMinute();
            c.set(mYear, mMonth, mDay, mHour, mMinute);
        }

        mEditTextDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg1, MotionEvent event) {
                return pickDate(event);
            }
        });
        mEditTextTime.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg1, MotionEvent event) {
                return pickTime(event);
            }
        });

        expenseAdapter = new ArrayAdapter<String>(mActivity, R.layout.category_spinner_item, Category.getExpenseCategoriesList());
        incomeAdapter = new ArrayAdapter<String>(mActivity, R.layout.category_spinner_item, Category.getIncomeCategoriesList());
        mCategorySpinner.setAdapter(expenseAdapter);
        if (!isAdd) {
            mCategorySpinner.setSelection(expenseToShow.getCategory().getValue());
        }

        mRGIsExpense.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (checkedId == R.id.radio_income & !incomeSelected) {
                    expenseCat = Category.getCategoryFromString(mCategorySpinner.getSelectedItem().toString());
                    mCategorySpinner.setAdapter(incomeAdapter);
                    mCategorySpinner.setSelection(incomeCat.getSpinnerLocation(true));
                    incomeSelected = true;
                } else {
                    incomeCat = Category.getCategoryFromString(mCategorySpinner.getSelectedItem().toString());
                    mCategorySpinner.setAdapter(expenseAdapter);
                    mCategorySpinner.setSelection(expenseCat.getSpinnerLocation(false));
                    incomeSelected = false;
                }
            }
        });

        if (!isAdd) {
            if (expenseToShow.getDescription().equals("")) {
                mEditTextDescription.setHint(getResources().getString(R.string.Description_optional));
            } else {
                mEditTextDescription.setText(expenseToShow.getDescription());
            }
        }

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View arg0) {
                addOrEditExpense(isAdd);
            }
        });
    }

    public boolean pickDate(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            // Launch Date Picker Dialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(mActivity,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            mYear = year;
                            mMonth = monthOfYear;
                            mDay = dayOfMonth;
                            c.set(mYear, mMonth, mDay, mHour, mMinute);
                            mEditTextDate.setText(DateFormat.getDateInstance().format(c.getTime()));
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
            return true;
        }
        return false;
    }

    public boolean pickTime(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(mActivity,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {
                            mHour = hourOfDay;
                            mMinute = minute;
                            c.set(mYear, mMonth, mDay, mHour, mMinute);
                            mEditTextTime.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime()));
                        }
                    }, mHour, mMinute, true);
            timePickerDialog.show();
            return true;
        }
        return false;
    }

    public void addOrEditExpense(boolean isAdd) {
        String title = mEditTextTitle.getText().toString();
        if (title.equals("")) {
            title = getResources().getString(R.string.general);
        }

        //  Handle is expense / income
        boolean isExpense = true;
        int selectedId = mRGIsExpense.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_income) {
            isExpense = false;
        }

        Category category = Category.getCategoryFromString(mCategorySpinner.getSelectedItem().toString());

        Double amount;
        if (mEditTextAmount.getText().toString().equals("")) {
            amount = 0.0;
        } else {
            amount = Double.parseDouble(mEditTextAmount.getText().toString());
        }
        //CREATE EXPENSE
        Expense newExpense = new Expense("",
                amount,
                title,
                mEditTextDescription.getText().toString(),
                category,
                c.getTime(),
                mActivity.mSessionManager.getUserId(),
                mActivity.mSessionManager.getUserName(),
                isExpense);

        if (isAdd) {
            FirebaseBackend.getInstance().addExpenseToBudget(mActivity.mCurrentBudget, newExpense);
        } else {
            newExpense.setId(expenseToShow.getId());
            FirebaseBackend.getInstance().editExpense(mActivity.mCurrentBudget.getId(), newExpense);
        }
        mActivity.tryReleaseTabs();
    }

    public void deleteExpense() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.confirm_delete);
        builder.setMessage(R.string.delete_confirmation_expense);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.yes, new ExpenseFragment.DeleteDialogListener());
        builder.setNegativeButton(R.string.no, new ExpenseFragment.DeleteDialogListener());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private class DeleteDialogListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            if (i == DialogInterface.BUTTON_POSITIVE) {
                FirebaseBackend.getInstance().deleteExpense(mActivity.mCurrentBudget.getId(), expenseToShow.getId());
                Log.d("", "ExpenseFragment: deleting expense");
                Toast.makeText(mActivity, getResources().getString(R.string.expense_deleted), Toast.LENGTH_SHORT).show();
                // restart app, so won't go back to the deleted expense
                Intent restartIntent = mActivity.getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(mActivity.getPackageName());
                restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mActivity.mSessionManager.goToLastBudget();
                startActivity(restartIntent);

            }
        }
    }
}
