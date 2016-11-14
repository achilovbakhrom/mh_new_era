package com.jim.pocketaccounter.managers;

import android.content.Context;

import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.Account;
import com.jim.pocketaccounter.database.AccountDao;
import com.jim.pocketaccounter.database.AccountOperation;
import com.jim.pocketaccounter.database.AccountOperationDao;
import com.jim.pocketaccounter.database.AutoMarket;
import com.jim.pocketaccounter.database.AutoMarketDao;
import com.jim.pocketaccounter.database.BoardButton;
import com.jim.pocketaccounter.database.BoardButtonDao;
import com.jim.pocketaccounter.database.CreditDetials;
import com.jim.pocketaccounter.database.CreditDetialsDao;
import com.jim.pocketaccounter.database.Currency;
import com.jim.pocketaccounter.database.CurrencyCostState;
import com.jim.pocketaccounter.database.CurrencyCostStateDao;
import com.jim.pocketaccounter.database.CurrencyDao;
import com.jim.pocketaccounter.database.CurrencyWithAmount;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.DebtBorrow;
import com.jim.pocketaccounter.database.DebtBorrowDao;
import com.jim.pocketaccounter.database.FinanceRecord;
import com.jim.pocketaccounter.database.FinanceRecordDao;
import com.jim.pocketaccounter.database.Person;
import com.jim.pocketaccounter.database.PersonDao;
import com.jim.pocketaccounter.database.Purpose;
import com.jim.pocketaccounter.database.PurposeDao;
import com.jim.pocketaccounter.database.Recking;
import com.jim.pocketaccounter.database.ReckingCredit;
import com.jim.pocketaccounter.database.ReckingCreditDao;
import com.jim.pocketaccounter.database.ReckingDao;
import com.jim.pocketaccounter.database.RootCategory;
import com.jim.pocketaccounter.database.RootCategoryDao;
import com.jim.pocketaccounter.database.SmsParseObject;
import com.jim.pocketaccounter.database.SmsParseObjectDao;
import com.jim.pocketaccounter.database.SmsParseSuccess;
import com.jim.pocketaccounter.database.SmsParseSuccessDao;
import com.jim.pocketaccounter.database.SubCategory;
import com.jim.pocketaccounter.database.SubCategoryDao;
import com.jim.pocketaccounter.database.TemplateVoice;
import com.jim.pocketaccounter.database.UserEnteredCalendars;
import com.jim.pocketaccounter.database.UserEnteredCalendarsDao;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;
import com.jim.pocketaccounter.utils.cache.DataCache;

import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
/**
 * Created by DEV on 28.08.2016.
 */

public class LogicManager {
    @Inject DaoSession daoSession;
    @Inject CommonOperations commonOperations;
    @Inject DataCache dataCache;
    @Inject List<TemplateVoice> voices;
    private Context context;
    private CurrencyDao currencyDao;
    private FinanceRecordDao recordDao;
    private DebtBorrowDao debtBorrowDao;
    private CreditDetialsDao creditDetialsDao;
    private AccountDao accountDao;
    private ReckingCreditDao reckingCreditDao;
    private SubCategoryDao subCategoryDao;
    private BoardButtonDao boardButtonDao;
    private RootCategoryDao rootCategoryDao;
    private PurposeDao purposeDao;
    private PersonDao personDao;
    private ReckingDao reckingDao;
    private AccountOperationDao accountOperationDao;
    private AutoMarketDao autoMarketDao;
    private SmsParseObjectDao smsParseObjectDao;
    private SmsParseSuccessDao smsParseSuccessDao;
    private CurrencyCostStateDao currencyCostStateDao;
    private UserEnteredCalendarsDao userEnteredCalendarsDao;
    public LogicManager(Context context) {
        this.context = context;
        ((PocketAccounterApplication) context.getApplicationContext()).component().inject(this);
        currencyDao = daoSession.getCurrencyDao();
        recordDao = daoSession.getFinanceRecordDao();
        debtBorrowDao = daoSession.getDebtBorrowDao();
        creditDetialsDao = daoSession.getCreditDetialsDao();
        smsParseObjectDao = daoSession.getSmsParseObjectDao();
        accountDao = daoSession.getAccountDao();
        reckingCreditDao = daoSession.getReckingCreditDao();
        subCategoryDao = daoSession.getSubCategoryDao();
        boardButtonDao = daoSession.getBoardButtonDao();
        rootCategoryDao = daoSession.getRootCategoryDao();
        purposeDao = daoSession.getPurposeDao();
        personDao = daoSession.getPersonDao();
        reckingDao = daoSession.getReckingDao();
        accountOperationDao = daoSession.getAccountOperationDao();
        autoMarketDao = daoSession.getAutoMarketDao();
        smsParseObjectDao = daoSession.getSmsParseObjectDao();
        smsParseSuccessDao = daoSession.getSmsParseSuccessDao();
        currencyCostStateDao = daoSession.getCurrencyCostStateDao();
        userEnteredCalendarsDao = daoSession.getUserEnteredCalendarsDao();
    }

    public int deleteCurrency(List<Currency> currencies) {
        List<Currency> allCureencies = currencyDao.loadAll();
        if (allCureencies.size() < 2 || currencies.size() == allCureencies.size())
            return LogicManagerConstants.MUST_BE_AT_LEAST_ONE_OBJECT;
        for (Currency currency : currencies) {
            for (FinanceRecord record : recordDao.loadAll()) {
                if (record.getCurrency().getId().matches(currency.getId())) {
                    recordDao.delete(record);
                }
            }
            for (DebtBorrow debtBorrow : debtBorrowDao.loadAll()) {
                if (debtBorrow.getCurrency().getId().matches(currency.getId()))
                    debtBorrowDao.delete(debtBorrow);
            }
            for (CreditDetials creditDetials : creditDetialsDao.loadAll()) {
                if (creditDetials.getValyute_currency().getId().matches(currency.getId()))
                    creditDetialsDao.delete(creditDetials);
            }
            for (SmsParseObject smsParseObject : smsParseObjectDao.loadAll()) {
                if (smsParseObject.getCurrency().getId().matches(currency.getId()))
                    smsParseObjectDao.delete(smsParseObject);
            }
            for (CurrencyCostState currencyCostState : currencyCostStateDao.loadAll()) {
                boolean found = currencyCostState.getMainCurrency().getId().equals(currency.getId());
                if (found) {
                    for (CurrencyWithAmount withAmount : currencyCostState.getCurrencyWithAmountList())
                        daoSession.getCurrencyWithAmountDao().delete(withAmount);
                    currencyCostStateDao.delete(currencyCostState);
                }
                else {
                    for (CurrencyWithAmount withAmount : currencyCostState.getCurrencyWithAmountList()) {
                        if (withAmount.getCurrencyId().equals(currency.getId())) {
                            daoSession.getCurrencyWithAmountDao().delete(withAmount);
                            currencyCostState.resetCurrencyWithAmountList();
                        }
                    }
                }
            }
            if (currency.getMain())
                setMainCurrency(null);
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
            List<UserEnteredCalendars> userEnteredCalendarses = currency.getUserEnteredCalendarses();
            for (UserEnteredCalendars enteredCalendars : userEnteredCalendarses) {
                QueryBuilder<UserEnteredCalendars> query = daoSession.queryBuilder(UserEnteredCalendars.class)
                        .where(UserEnteredCalendarsDao.Properties.Calendar.eq(format.format(enteredCalendars.getCalendar().getTime())),
                                UserEnteredCalendarsDao.Properties.CurrencyId.notEq(currency.getId()));
                if (query.list().isEmpty()) {
                    daoSession
                            .queryBuilder(CurrencyCostState.class)
                            .where(CurrencyCostStateDao.Properties.Day.eq(format.format(enteredCalendars.getCalendar().getTime())))
                            .buildDelete()
                            .executeDeleteWithoutDetachingEntities();
                }
            }
            for (UserEnteredCalendars userEnteredCalendars : currency.getUserEnteredCalendarses())
                daoSession.getUserEnteredCalendarsDao().delete(userEnteredCalendars);
            currencyDao.delete(currency);
        }
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public int insertAccount(Account account) {
        Query<Account> accountQuery = accountDao.queryBuilder()
                .where(AccountDao.Properties.Name.eq(account.getName())).build();
        if (!accountQuery.list().isEmpty())
            return LogicManagerConstants.SUCH_NAME_ALREADY_EXISTS;
        accountDao.insertOrReplace(account);
        return LogicManagerConstants.SAVED_SUCCESSFULL;
    }

    public int deleteAccount(List<Account> accounts) {
        List<Account> allAccounts = accountDao.loadAll();
        if (allAccounts.size() < 2 || allAccounts.size() == accounts.size())
            return LogicManagerConstants.MUST_BE_AT_LEAST_ONE_OBJECT;
        for (Account account : accounts) {
            for (FinanceRecord record : recordDao.loadAll()) {
                if (record.getAccount().getId().matches(account.getId())) {
                    recordDao.delete(record);
                }
            }
            for (DebtBorrow debtBorrow : debtBorrowDao.loadAll()) {
                if (debtBorrow.getAccount().getId().matches(account.getId()))
                    debtBorrowDao.delete(debtBorrow);
            }
            for (CreditDetials creditDetials : creditDetialsDao.loadAll()) {
                for (ReckingCredit reckingCredit : creditDetials.getReckings())
                    if (reckingCredit.getAccountId().matches(account.getId()))
                        reckingCreditDao.delete(reckingCredit);
            }
            for (SmsParseObject smsParseObject : smsParseObjectDao.loadAll()) {
                if (smsParseObject.getAccount().getId().matches(account.getId()))
                    smsParseObjectDao.delete(smsParseObject);
            }
            accountDao.delete(account);
        }
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public void generateCurrencyCosts(Calendar day, double amount, Currency adding) {
        final int EARLIEST = 0, MIDDLE = 1, LATEST = 2; // position of adding currency

        //defining the position
        int position = EARLIEST;
        List<CurrencyCostState> allStates = daoSession
                .getCurrencyCostStateDao()
                .queryBuilder()
                .where(CurrencyCostStateDao.Properties.MainCurId.eq(commonOperations.getMainCurrency().getId()))
                .list();
        Collections.sort(allStates, new Comparator<CurrencyCostState>() {
            @Override
            public int compare(CurrencyCostState currencyCostState, CurrencyCostState t1) {
                return currencyCostState.getDay().compareTo(t1.getDay());
            }
        });
        if (!allStates.isEmpty()) {
            if (allStates.get(0).getDay().compareTo(day) >= 0)
                position = EARLIEST;
            else if (allStates.get(allStates.size()-1).getDay().compareTo(day) <= 0)
                position = LATEST;
            else
                position = MIDDLE;
        }

        //after defining position, we consider all options of position of currency
        CurrencyCostState supplyState = null;
        switch (position) { //finding anchor state and generate day currency costs
            case EARLIEST:
                supplyState = allStates.get(0);
                break;
            case MIDDLE:
                for (CurrencyCostState state : allStates) {
                    if (state.getDay().compareTo(day) <= 0)
                        supplyState = state;
                    else
                        break;
                }
                break;
            case LATEST:
                supplyState = allStates.get(allStates.size() - 1);
                break;
        }
        generateCostForTheDay((Calendar) day.clone(), amount, adding, supplyState);
        if (position != LATEST)
            generateCostsForRestDays((Calendar) day.clone(), amount, adding);
    }

    private void generateCostForTheDay(Calendar day, double amount, Currency adding, CurrencyCostState supply) { //generating costs using supplying data
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        String requiredDate = format.format(day.getTime());
        List<CurrencyCostState> queryList = daoSession
                .queryBuilder(CurrencyCostState.class)
                .where(CurrencyCostStateDao.Properties.Day.eq(requiredDate))
                .list();
        if (!queryList.isEmpty()) {
            if (isNewCurrency(adding)) {
                for (CurrencyCostState state : queryList) {
                    CurrencyWithAmount withAmount = new CurrencyWithAmount();
                    if (state.getMainCurrency().getMain())
                        withAmount.setAmount(amount);
                    else
                        withAmount.setAmount(amount / getCoefficient(requiredDate, state.getMainCurrency()));
                    withAmount.setCurrency(adding);
                    withAmount.setParentId(state.getId());
                    daoSession.insertOrReplace(withAmount);
                    state.resetCurrencyWithAmountList();
                }
                CurrencyCostState newState = new CurrencyCostState();
                newState.setDay(day);
                newState.setMainCurrency(adding);
                daoSession.insertOrReplace(newState);
                CurrencyWithAmount mainWithAmount = new CurrencyWithAmount();
                mainWithAmount.setCurrency(commonOperations.getMainCurrency());
                mainWithAmount.setAmount(1/amount);
                mainWithAmount.setParentId(newState.getId());
                daoSession.insertOrReplace(mainWithAmount);
                List<Currency> restCurrencies = daoSession
                        .queryBuilder(Currency.class)
                        .where(CurrencyDao.Properties.Id.notEq(adding.getId()),
                                CurrencyDao.Properties.Id.notEq(commonOperations.getMainCurrency().getId()))
                        .list();
                for (Currency restCurrency : restCurrencies) {
                    List<CurrencyCostState> currentDayMainState =
                            daoSession.queryBuilder(CurrencyCostState.class)
                            .where(CurrencyCostStateDao.Properties.Day.eq(requiredDate),
                                    CurrencyCostStateDao.Properties.MainCurId.eq(commonOperations.getMainCurrency().getId()))
                            .list();
                    if (!currentDayMainState.isEmpty()) {
                        CurrencyCostState mainState = currentDayMainState.get(0);
                        CurrencyWithAmount temp = new CurrencyWithAmount();
                        temp.setCurrency(restCurrency);
                        for (CurrencyWithAmount withAmount : mainState.getCurrencyWithAmountList()) {
                            if (withAmount.getCurrencyId().equals(restCurrency.getId())) {
                                temp.setAmount(withAmount.getAmount());
                            }
                        }
                        temp.setParentId(newState.getId());
                        daoSession.insertOrReplace(temp);
                    }
                }
                newState.resetCurrencyWithAmountList();
            }
            else {
                for (CurrencyCostState state : queryList) {
                    if (state.getMainCurrency().getMain()) {
                        for (CurrencyWithAmount withAmount : state.getCurrencyWithAmountList()) {
                            if (withAmount.getCurrencyId().equals(adding.getId())) {
                                withAmount.setAmount(amount);
                                daoSession.insertOrReplace(withAmount);
                                break;
                            }
                        }
                    }
                    else if (state.getMainCurId().equals(adding.getId())) {
                        for (CurrencyWithAmount withAmount : state.getCurrencyWithAmountList()) {
                            if (withAmount.getCurrency().getMain()) {
                                withAmount.setAmount(1/amount);
                                daoSession.insertOrReplace(withAmount);
                                break;
                            }
                        }
                    }
                    else {
                        for (CurrencyWithAmount withAmount : state.getCurrencyWithAmountList()) {
                            if (withAmount.getCurrencyId().equals(adding.getId())) {
                                withAmount.setAmount(amount/getCoefficient(format.format(day.getTime()), state.getMainCurrency()));
                                daoSession.insertOrReplace(withAmount);
                                break;
                            }
                        }
                    }
                    state.resetCurrencyWithAmountList();
                }
            }
        }
        else {
            CurrencyCostState generatingState = new CurrencyCostState();
            generatingState.setMainCurrency(commonOperations.getMainCurrency());
            generatingState.setDay(day);
            daoSession.insertOrReplace(generatingState);
            List<Currency> notMainCurrencies = daoSession
                    .queryBuilder(Currency.class)
                    .where(CurrencyDao.Properties.IsMain.eq(false))
                    .list();
            for (Currency currency : notMainCurrencies) {
                CurrencyWithAmount withAmount = new CurrencyWithAmount();
                withAmount.__setDaoSession(daoSession);
                if (currency.getId().equals(adding.getId())) {
                    withAmount.setAmount(amount);
                }
                else {
                    withAmount.setAmount(getCoefficient(supply, currency));
                }
                withAmount.setCurrency(currency);
                withAmount.setParentId(generatingState.getId());
                daoSession.insertOrReplace(withAmount);
            }
            generatingState.resetCurrencyWithAmountList();
            List<CurrencyWithAmount> currencyWithAmounts = generatingState.getCurrencyWithAmountList();
            for (CurrencyWithAmount currencyWithAmount : currencyWithAmounts) {
                CurrencyCostState anotherState = new CurrencyCostState();
                anotherState.setDay((Calendar)day.clone());
                anotherState.setMainCurrency(currencyWithAmount.getCurrency());
                daoSession.getCurrencyCostStateDao().insertOrReplace(anotherState);
                CurrencyWithAmount anotherStatesAmount = new CurrencyWithAmount();
                anotherStatesAmount.setCurrency(generatingState.getMainCurrency());
                anotherStatesAmount.setAmount(1/currencyWithAmount.getAmount());
                anotherStatesAmount.setParentId(anotherState.getId());
                daoSession.getCurrencyWithAmountDao().insertOrReplace(anotherStatesAmount);
                for (CurrencyWithAmount amnt : currencyWithAmounts) {
                    if (!amnt.getCurrencyId().equals(currencyWithAmount.getCurrencyId())) {
                        CurrencyWithAmount anotherStatesRestAmounts = new CurrencyWithAmount();
                        anotherStatesRestAmounts.setCurrency(amnt.getCurrency());
                        anotherStatesRestAmounts.setAmount(amnt.getAmount()/currencyWithAmount.getAmount());
                        anotherStatesRestAmounts.setParentId(anotherState.getId());
                        daoSession.getCurrencyWithAmountDao().insertOrReplace(anotherStatesRestAmounts);
                    }
                }
            }
        }
    }

    private double getCoefficient(CurrencyCostState state, Currency currency) {
        for (CurrencyWithAmount withAmount : state.getCurrencyWithAmountList()) {
            if (withAmount.getCurrencyId().equals(currency.getId()))
                return withAmount.getAmount();
        }
        return 1.0d;
    }

    private double getCoefficient(String day, Currency currency) {
        String mainCurrencyId = commonOperations.getMainCurrency().getId();
        List<CurrencyCostState> list = daoSession
                .queryBuilder(CurrencyCostState.class)
                .where(CurrencyCostStateDao.Properties.MainCurId.eq(mainCurrencyId), CurrencyCostStateDao.Properties.Day.eq(day))
                .list();
        if (!list.isEmpty()) {
            List<CurrencyWithAmount> withAmounts = list.get(0).getCurrencyWithAmountList();
            for (CurrencyWithAmount withAmount : withAmounts) {
                if (withAmount.getCurrencyId().equals(currency.getId()))
                    return withAmount.getAmount();
            }
        }
        return 1.0d;
    }

    private boolean isNewCurrency(Currency currency) { //Used in generateCostForTheDay method
        return daoSession
                .queryBuilder(CurrencyCostState.class)
                .where(CurrencyCostStateDao.Properties.MainCurId.eq(currency.getId()))
                .list()
                .isEmpty(); // clarifying, is the currency new added or something else ...
    }

    private void generateCostsForRestDays(Calendar day, double amount, Currency adding) {
        day.set(Calendar.HOUR_OF_DAY, 23);
        day.set(Calendar.MINUTE, 59);
        day.set(Calendar.SECOND, 59);
        day.set(Calendar.MILLISECOND, 59);
        List<CurrencyCostState> allStates = daoSession.getCurrencyCostStateDao().loadAll();
        for (int i = 0; i < allStates.size(); i++) {
            if (allStates.get(i).getDay().compareTo(day) > 0) {
                if (i != 0)
                    generateCostForTheDay(day, amount, adding, allStates.get(i-1));
            }
        }
    }

    public int insertUserEnteredCalendars(Currency currency, Calendar day) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        List<UserEnteredCalendars> list = userEnteredCalendarsDao
                .queryBuilder()
                .where(UserEnteredCalendarsDao.Properties.CurrencyId.eq(currency.getId()),
                        UserEnteredCalendarsDao.Properties.Calendar.eq(format.format(day.getTime())))
                .list();
        if (!list.isEmpty()) return LogicManagerConstants.SUCH_NAME_ALREADY_EXISTS;
        UserEnteredCalendars userEnteredCalendars = new UserEnteredCalendars();
        userEnteredCalendars.setCalendar((Calendar)day.clone());
        userEnteredCalendars.setCurrencyId(currency.getId());
        userEnteredCalendarsDao.insertOrReplace(userEnteredCalendars);
        return LogicManagerConstants.SAVED_SUCCESSFULL;
    }

    public void setMainCurrency(Currency currency) {
        if (currency != null && currency.getMain()) return;
        List<Currency> currencies = currencyDao.loadAll();
        if (currency == null) {
            int pos = 0;
            for (int i = 0; i < currencies.size(); i++) {
                if (currencies.get(i).getMain()) {
                    pos = i;
                    break;
                }
            }
            currencies.get(pos).setMain(false);
            if (pos == currencies.size() - 1) {
                currencies.get(0).setMain(true);

            } else {
                currencies.get(pos + 1).setMain(true);
            }
        } else {
            int oldMainPos = 0;
            int currMainPos = 0;
            for (int i = 0; i < currencies.size(); i++) {
                if (currencies.get(i).getMain()) {
                    oldMainPos = i;
                }
                if (currencies.get(i).getId().matches(currency.getId())) {
                    currMainPos = i;
                }
            }
            currencies.get(oldMainPos).setMain(false);
            currencies.get(currMainPos).setMain(true);
        }
        currencyDao.insertOrReplaceInTx(currencies);
        daoSession.getCurrencyDao().detachAll();
    }

    //currency costs
    public int deleteCurrencyCosts(List<UserEnteredCalendars> userEnteredCalendarses, Currency currency) {
        List<UserEnteredCalendars> calendars = userEnteredCalendarsDao
                .queryBuilder()
                .where(UserEnteredCalendarsDao.Properties.CurrencyId.eq(currency.getId()))
                .list();
        if (userEnteredCalendarses.size() == calendars.size())
            return LogicManagerConstants.LIST_IS_EMPTY;
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        for (UserEnteredCalendars enteredCalendars : userEnteredCalendarses) {
            QueryBuilder<UserEnteredCalendars> query = daoSession.queryBuilder(UserEnteredCalendars.class)
                    .where(UserEnteredCalendarsDao.Properties.Calendar.eq(format.format(enteredCalendars.getCalendar().getTime())),
                            UserEnteredCalendarsDao.Properties.Calendar.notEq(currency.getId()));
            if (query.list().isEmpty()) {
                daoSession
                        .queryBuilder(CurrencyCostState.class)
                        .where(CurrencyCostStateDao.Properties.Day.eq(format.format(enteredCalendars.getCalendar().getTime())))
                        .buildDelete()
                        .executeDeleteWithoutDetachingEntities();
            }
        }
        userEnteredCalendarsDao.deleteInTx(userEnteredCalendarses);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public int insertSubCategory(List<SubCategory> subCategories) {
        subCategoryDao.insertOrReplaceInTx(subCategories);

        return LogicManagerConstants.SAVED_SUCCESSFULL;
    }

    public int deleteSubcategories(List<SubCategory> subCategories) {
        for (SubCategory subCategory : subCategories) {
            for (FinanceRecord financeRecord : recordDao.loadAll()) {
                if (financeRecord.getSubCategory() != null && financeRecord.getSubCategory().getId().equals(subCategory.getId()))
                    recordDao.delete(financeRecord);
            }
        }
        subCategoryDao.deleteInTx(subCategories);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public void changeBoardButton(int type, int pos, String categoryId) {
        int t = PocketAccounterGeneral.CATEGORY;
        if (categoryId != null) {
            List<RootCategory> categoryList = daoSession.getRootCategoryDao().loadAll();
            boolean categoryFound = false, operationFound = false, creditFound = false,
                    debtBorrowFound = false;
            for (RootCategory category : categoryList) {
                if (categoryId.matches(category.getId())) {
                    categoryFound = true;
                    t = PocketAccounterGeneral.CATEGORY;
                    break;
                }
            }
            if (!categoryFound) {
                String[] operationIds = context.getResources().getStringArray(R.array.operation_ids);
                for (String operationId : operationIds) {
                    if (operationId.matches(categoryId)) {
                        operationFound = true;
                        t = PocketAccounterGeneral.FUNCTION;
                        break;
                    }
                }
            }
            if (!operationFound) {
                List<CreditDetials> credits = daoSession.getCreditDetialsDao().loadAll();
                for (CreditDetials creditDetials : credits) {
                    if (Long.toString(creditDetials.getMyCredit_id()).matches(categoryId)) {
                        creditFound = true;
                        t = PocketAccounterGeneral.CREDIT;
                        break;
                    }
                }
            }
            if (!creditFound) {
                List<DebtBorrow> debtBorrows = daoSession.getDebtBorrowDao().loadAll();
                for (DebtBorrow debtBorrow : debtBorrows) {
                    if (debtBorrow.getId().matches(categoryId)) {
                        debtBorrowFound = true;
                        t = PocketAccounterGeneral.DEBT_BORROW;
                        break;
                    }
                }
            }
            if (!debtBorrowFound) {
                String[] pageIds = context.getResources().getStringArray(R.array.page_ids);
                for (int i = 0; i < pageIds.length; i++) {
                    if (pageIds[i].matches(categoryId)) {
                        t = PocketAccounterGeneral.PAGE;
                        break;
                    }
                }
            }
        }
        Query<BoardButton> query = boardButtonDao
                .queryBuilder()
                .where(BoardButtonDao.Properties.Table.eq(type),
                        BoardButtonDao.Properties.Pos.eq(pos))
                .build();
        List<BoardButton> list = query.list();
        BoardButton boardButton = null;
        if (!list.isEmpty()) {
            boardButton = list.get(0);
            boardButton.setCategoryId(categoryId);
        }
        if (boardButton != null)
            boardButton.setType(t);
        boardButtonDao.insertOrReplace(boardButton);
    }

    public int insertRootCategory(RootCategory rootCategory) {
        Query<RootCategory> query = rootCategoryDao
                .queryBuilder()
                .where(RootCategoryDao.Properties.Name.eq(rootCategory.getName()))
                .build();
        if (!query.list().isEmpty())
            return LogicManagerConstants.SUCH_NAME_ALREADY_EXISTS;
        rootCategoryDao.insertOrReplace(rootCategory);
        CommonOperations.generateRegexVoice(daoSession, voices, rootCategory.getName(), rootCategory.getId());
        return LogicManagerConstants.SAVED_SUCCESSFULL;
    }

    public int deleteRootCategory(RootCategory category) {
        for (FinanceRecord record : recordDao.loadAll())
            if (record.getCategory().getId().matches(category.getId()))
                recordDao.delete(record);
        for (BoardButton boardButton : boardButtonDao.loadAll())
            if (boardButton.getCategoryId() != null && boardButton.getCategoryId().matches(category.getId())) {
                boardButton.setCategoryId(null);
                boardButtonDao.insertOrReplace(boardButton);
                commonOperations.changeIconToNull(boardButton.getPos(), dataCache, boardButton.getTable());
            }
        rootCategoryDao.delete(category);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public int insertPurpose(Purpose purpose) {
        Query<Purpose> query = purposeDao
                .queryBuilder()
                .where(PurposeDao.Properties.Id.eq(purpose.getId()))
                .build();
        if (query.list().isEmpty()) {
            query = purposeDao
                    .queryBuilder()
                    .where(PurposeDao.Properties.Description.eq(purpose.getDescription()))
                    .build();
            if (!query.list().isEmpty())
                return LogicManagerConstants.SUCH_NAME_ALREADY_EXISTS;
        }
        purposeDao.insertOrReplace(purpose);
        return LogicManagerConstants.SAVED_SUCCESSFULL;
    }

    public int deletePurpose(Purpose purpose) {
        Query<Purpose> query = purposeDao
                .queryBuilder()
                .where(PurposeDao.Properties.Id.eq(purpose.getId()))
                .build();
        if (query.list().isEmpty()) {
            return LogicManagerConstants.REQUESTED_OBJECT_NOT_FOUND;
        }
        purposeDao.delete(purpose);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public int insertDebtBorrow(DebtBorrow debtBorrow) {
        debtBorrowDao.insertOrReplace(debtBorrow);
        CommonOperations.generateRegexVoice(daoSession, voices, debtBorrow.getPerson().getName(), debtBorrow.getId());
        return LogicManagerConstants.SAVED_SUCCESSFULL;
    }

    public int deleteDebtBorrow(DebtBorrow debtBorrow) {
        Query<DebtBorrow> query = debtBorrowDao.queryBuilder()
                .where(DebtBorrowDao.Properties.Id.eq(debtBorrow.getId()))
                .build();
        if (query.list().isEmpty()) {
            return LogicManagerConstants.REQUESTED_OBJECT_NOT_FOUND;
        }
        debtBorrowDao.delete(debtBorrow);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public int insertPerson(Person person) {
        personDao.insertOrReplace(person);
        return LogicManagerConstants.SAVED_SUCCESSFULL;
    }

    public int insertCredit(CreditDetials creditDetials) {
        creditDetialsDao.insertOrReplace(creditDetials);
        CommonOperations.generateRegexVoice(daoSession, voices, creditDetials.getCredit_name(), "" + creditDetials.getMyCredit_id());
        return LogicManagerConstants.SAVED_SUCCESSFULL;
    }

    public int deleteCredit(CreditDetials creditDetials) {
        Query<CreditDetials> query = creditDetialsDao.queryBuilder()
                .where(CreditDetialsDao.Properties.MyCredit_id.eq(creditDetials.getMyCredit_id()))
                .build();
        if (query.list().isEmpty()) {
            return LogicManagerConstants.REQUESTED_OBJECT_NOT_FOUND;
        }
        creditDetialsDao.delete(creditDetials);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public int insertReckingDebt(Recking recking) {
        reckingDao.insertOrReplace(recking);
        return LogicManagerConstants.SAVED_SUCCESSFULL;
    }

    public int deleteRecking(Recking recking) {
        reckingDao.delete(recking);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public int insertReckingCredit(ReckingCredit reckingCredit) {
        reckingCreditDao.insertOrReplace(reckingCredit);
        return LogicManagerConstants.SAVED_SUCCESSFULL;
    }

    public int deleteReckingCredit(ReckingCredit reckingCredit) {
        reckingCreditDao.delete(reckingCredit);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public int insertAccountOperation(AccountOperation accountOperation) {
        accountOperationDao.insertOrReplace(accountOperation);
        return LogicManagerConstants.SAVED_SUCCESSFULL;
    }

    public int deleteAccountOperation(AccountOperation accountOperation) {
        accountOperationDao.delete(accountOperation);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public int insertAutoMarket(AutoMarket autoMarket) {
        autoMarket.__setDaoSession(daoSession);
        Query<AutoMarket> query = autoMarketDao.queryBuilder()
                .where(autoMarketDao.queryBuilder()
                        .and(AutoMarketDao.Properties.CatId.eq(autoMarket.getCatId()),
                                AutoMarketDao.Properties.CatSubId.eq(autoMarket.getSubCategory() == null ? "" : autoMarket.getCatSubId()))).build();

        if (query.list() != null && query.list().isEmpty()) {
            autoMarketDao.insertOrReplace(autoMarket);
            return LogicManagerConstants.SAVED_SUCCESSFULL;
        }
        return LogicManagerConstants.SUCH_NAME_ALREADY_EXISTS;
    }

    public int deleteAutoMarket(AutoMarket autoMarket) {
        autoMarketDao.delete(autoMarket);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public int deleteSmsParseObject(SmsParseObject smsParseObject) {
        smsParseSuccessDao.deleteInTx(smsParseSuccessDao.queryBuilder().
                where(SmsParseSuccessDao.Properties
                        .SmsParseObjectId.eq(smsParseObject.getId())).list());
        smsParseObjectDao.delete(smsParseObject);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public int deleteSmsParseSuccess(SmsParseSuccess smsParseSuccess) {
        smsParseSuccessDao.delete(smsParseSuccess);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public double isLimitAccess(Account account, Calendar date) {
        double accounted = commonOperations.getCost(date, account.getStartMoneyCurrency(), account.getAmount());
        List<AccountOperation> operations = daoSession.getAccountOperationDao().loadAll();
        for (AccountOperation accountOperation : operations) {
            if (accountOperation.getSourceId().equals(account.getId())) {
                accounted -= commonOperations.getCost(date, accountOperation.getCurrency(), accountOperation.getAmount());
            }
            if (accountOperation.getTargetId().equals(account.getId())) {
                accounted += commonOperations.getCost(date, accountOperation.getCurrency(), accountOperation.getAmount());
            }
        }
        for (int i = 0; i < recordDao.queryBuilder().list().size(); i++) {
            FinanceRecord tempac = recordDao.queryBuilder().list().get(i);
            if (tempac.getAccount().getId().matches(account.getId())) {
                if (tempac.getCategory().getType() == PocketAccounterGeneral.INCOME)
                    accounted = accounted + commonOperations.getCost(tempac.getDate(), tempac.getCurrency(), tempac.getAmount());
                else
                    accounted = accounted - commonOperations.getCost(tempac.getDate(), tempac.getCurrency(), tempac.getAmount());
            }
        }
        for (DebtBorrow debtBorrow : debtBorrowDao.queryBuilder().list()) {
            if (debtBorrow.getCalculate()) {
                if (debtBorrow.getAccount().getId().matches(account.getId())) {
                    if (debtBorrow.getType() == DebtBorrow.BORROW) {
                        accounted = accounted - commonOperations.getCost(debtBorrow.getTakenDate(), debtBorrow.getCurrency(), debtBorrow.getAmount());
                    } else {
                        accounted = accounted + commonOperations.getCost(debtBorrow.getTakenDate(), debtBorrow.getCurrency(), debtBorrow.getAmount());
                    }
                    for (Recking recking : debtBorrow.getReckings()) {
                        Calendar cal = recking.getPayDate();

                        if (debtBorrow.getType() == DebtBorrow.DEBT) {
                            accounted = accounted - commonOperations.getCost(cal, debtBorrow.getCurrency(), recking.getAmount());
                        } else {
                            accounted = accounted + commonOperations.getCost(cal, debtBorrow.getCurrency(), recking.getAmount());
                        }
                    }
                } else {
                    for (Recking recking : debtBorrow.getReckings()) {
                        Calendar cal = recking.getPayDate();
                        if (recking.getAccountId().matches(account.getId())) {

                            if (debtBorrow.getType() == DebtBorrow.BORROW) {
                                accounted = accounted + commonOperations.getCost(cal, debtBorrow.getCurrency(), recking.getAmount());
                            } else {
                                accounted = accounted - commonOperations.getCost(cal, debtBorrow.getCurrency(), recking.getAmount());
                            }
                        }
                    }
                }
            }
        }
        for (CreditDetials creditDetials : creditDetialsDao.queryBuilder().list()) {
            if (creditDetials.getKey_for_include()) {
                for (ReckingCredit reckingCredit : creditDetials.getReckings()) {
                    if (reckingCredit.getAccountId().matches(account.getId())) {
                        accounted = accounted - commonOperations.getCost(reckingCredit.getPayDate(), creditDetials.getValyute_currency(), reckingCredit.getAmount());
                    }
                }
            }
        }
        for (SmsParseSuccess success: smsParseSuccessDao.loadAll()) {
            if (success.getType() == PocketAccounterGeneral.INCOME) {
                accounted += commonOperations.getCost(success.getDate(), success.getCurrency(), success.getAmount());
            } else {
                accounted -= commonOperations.getCost(success.getDate(), success.getCurrency(), success.getAmount());
            }
        }
        return accounted;
    }

    public int insertRecord(FinanceRecord record) {
        recordDao.insertOrReplace(record);
        return LogicManagerConstants.SAVED_SUCCESSFULL;
    }

    public int deleteRecord(FinanceRecord record) {
        recordDao.delete(record);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }
}