package money.mezu.mezu;

import android.support.annotation.NonNull;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class Expense implements Comparable<Expense> {

    private String mId;
    private double mAmount;
    private String mDescription;
    private String mTitle;
    private Category mCategory;
    private Date mTime;
    private UserIdentifier mUserID;
    private String mUserName;
    private boolean mIsExpense;
    private HashMap<String, Object> mPeriodic;

    public Expense(String id,
                   double amount,
                   String mTitle,
                   String description,
                   Category category,
                   Date time,
                   UserIdentifier uid,
                   String userName,
                   boolean isExpense) {
        super();
        this.mId = id;
        this.mAmount = amount;
        this.mTitle = mTitle;
        this.mDescription = description;
        this.mCategory = category;
        this.mTime = time;
        this.mUserID = uid;
        this.mUserName = userName;
        this.mIsExpense = isExpense;
        this.mPeriodic = null;
    }

    public Expense(HashMap<String, Object> serializedExpense) {
        super();
        this.mId = (String) serializedExpense.get("mId");
        this.mAmount = Double.parseDouble(serializedExpense.get("mAmount").toString());
        this.mTitle = (String) serializedExpense.get("mTitle");
        this.mDescription = (String) serializedExpense.get("mDescription");
        try {
            this.mCategory = Category.values()[Integer.parseInt(serializedExpense.get("mCategory").toString())];
        } catch (NumberFormatException e) {
            this.mCategory = Category.getCategoryFromString(serializedExpense.get("mCategory").toString());
        }

        this.mTime = new Date((long) serializedExpense.get("mTime"));
        this.mUserID = new UserIdentifier((new BigInteger((String) serializedExpense.get("mUserID"))));
        this.mUserName = (String) serializedExpense.get("mUserName");
        try {
            this.mIsExpense = (boolean) serializedExpense.get("mIsExpense");
        } catch (NullPointerException e) {
            this.mIsExpense = true;
        }
        if (serializedExpense.containsKey("mPeriodic"))
        {
            this.mPeriodic = (HashMap<String, Object>) serializedExpense.get("mPeriodic");
        }
    }

    public HashMap<String, Object> serialize() {
        HashMap<String, Object> serialized = new HashMap<String, Object>();
        serialized.put("mId", mId);
        serialized.put("mAmount", mAmount);
        serialized.put("mTitle", mTitle);
        serialized.put("mDescription", mDescription);
        serialized.put("mCategory", mCategory.getValue());
        serialized.put("mTime", mTime.getTime());
        serialized.put("mUserID", mUserID.getId().toString());
        serialized.put("mUserName", mUserName);
        serialized.put("mIsExpense", mIsExpense);
        if (null != this.mPeriodic)
        {
            serialized.put("mPeriodic", this.mPeriodic);
        }
        return serialized;
    }
    //************************************************************************************************************************************************
    public boolean isRecurrent()
    {
        return (this.mPeriodic != null);
    }
    //************************************************************************************************************************************************
    public void setRecurrence(HashMap<String, Object> period)
    {
        this.mPeriodic = period;
    }
    //************************************************************************************************************************************************
    public double getAlmostUniqueId()
    {
        if (null == this.mPeriodic)
        {
            return -1;
        }
        if (!this.mPeriodic.containsKey("almostUniqueId"))
        {
            return -1;
        }
        return (double)this.mPeriodic.get("almostUniqueId");
    }
    //************************************************************************************************************************************************

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public Category getCategory() {
        return mCategory;
    }

    public double getAmount() {
        return mAmount;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public Date getTime() {
        return mTime;
    }

    public void setTime(Date c) {
        mTime = c;
    }

    public UserIdentifier getUserID() {
        return mUserID;
    }

    public String getUserName() {
        return mUserName;
    }

    public boolean getIsExpense() {
        return mIsExpense;
    }

    public int getMinute() {
        Calendar c = Calendar.getInstance();
        c.setTime(mTime);
        return c.get(Calendar.MINUTE);
    }

    public int getHour() {
        Calendar c = Calendar.getInstance();
        c.setTime(mTime);
        return c.get(Calendar.HOUR_OF_DAY);
    }

    public int getDay() {
        Calendar c = Calendar.getInstance();
        c.setTime(mTime);
        return c.get(Calendar.DAY_OF_MONTH);
    }

    public int getMonth() {
        Calendar c = Calendar.getInstance();
        c.setTime(mTime);
        return c.get(Calendar.MONTH);
    }

    public int getYear() {
        Calendar c = Calendar.getInstance();
        c.setTime(mTime);
        return c.get(Calendar.YEAR);
    }

    @Override
    public int compareTo(@NonNull Expense other) {
        if (this.mTime.after(other.mTime)) {
            return -1;
        } else if (other.mTime.after(this.mTime)) {
            return 1;
        } else {
            return 0;
        }
    }

    public boolean expenseDiffers(Expense toCompare)
    {
        if (!mId.equals(toCompare.getId()))
        {
            return true;
        }
        if (mAmount != toCompare.getAmount())
        {
            return true;
        }
        if (!mDescription.equals(toCompare.getDescription()))
        {
            return true;
        }
        if (!mTitle.equals(toCompare.getTitle()))
        {
            return true;
        }
        if (!mCategory.toNiceString().equals(toCompare.getCategory().toNiceString()))
        {
            return true;
        }
        if (!mTime.equals(toCompare.getTime()))
        {
            return true;
        }
        if (!mUserID.getId().toString().equals(toCompare.getUserID().getId().toString()))
        {
            return true;
        }
        if (!mUserName.equals(toCompare.getUserName()))
        {
            return true;
        }
        if (mIsExpense != toCompare.getIsExpense())
        {
            return true;
        }

        return false;
    }

}

