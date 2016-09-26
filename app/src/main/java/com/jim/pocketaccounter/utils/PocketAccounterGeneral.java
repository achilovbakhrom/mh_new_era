package com.jim.pocketaccounter.utils;

import com.jim.pocketaccounter.database.Account;
import com.jim.pocketaccounter.database.RootCategory;

import java.util.Calendar;

public class PocketAccounterGeneral {
	public static final int NORMAL_MODE = 0;
	public static final int EDIT_MODE = 1;
	public static final int INCOME=0, EXPENSE =1, TRANSFER = 2;
	public static final int CATEGORY = 0, CREDIT = 1, DEBT_BORROW = 2, FUNCTION = 3, PAGE = 4;

	public static final int EXPENSE_BUTTONS_COUNT = 16;
	public static final int INCOME_BUTTONS_COUNT = 4;
	public static final int NO_MODE = 0, EXPANSE_MODE = 1, INCOME_MODE = 2;
	public static final int MAIN = 0, DETAIL = 1;
	public static final int SMS_ONLY_EXPENSE = 0, SMS_ONLY_INCOME = 1, SMS_BOTH = 2;
	public static final String EVERY_DAY="EVERY_DAY", EVERY_WEEK="EVERY_WEEK", EVERY_MONTH="EVERY_MONTH";
	public static final String TAG = "sss";
	public static final String DB_ONCREATE_ENTER = "DB_ONCREATE_ENTER";
	public static final String INCOMES = "INCOME", EXPENSES = "EXPENSE", BALANCE = "BALANCE";
	//top board buttons types
	public static final int UP_TOP_LEFT = 0, UP_SIMPLE = 1, UP_LEFT_SIMPLE = 2, UP_LEFT_BOTTOM = 3, UP_BOTTOM_SIMPLE = 4,
			UP_BOTTOM_RIGHT = 5, UP_TOP_RIGHT = 6, UP_TOP_SIMPLE = 7, UP_RIGHT_SIMPLE = 8,
			UP_TOP_LEFT_PRESSED = 9, UP_SIMPLE_PRESSED = 10, UP_LEFT_SIMPLE_PRESSED = 11, UP_LEFT_BOTTOM_PRESSED = 12,
			UP_BOTTOM_SIMPLE_PRESSED = 13,	UP_BOTTOM_RIGHT_PRESSED = 14, UP_TOP_RIGHT_PRESSED = 15, UP_TOP_SIMPLE_PRESSED = 16,
			UP_RIGHT_SIMPLE_PRESSED = 17, UP_WORKSPACE_SHADER = 18, ICONS_NO_CATEGORY = 19,
			DOWN_WORKSPACE_SHADER = 20, DOWN_MOST_LEFT = 21, DOWN_SIMPLE = 22, DOWN_MOST_RIGHT = 23,
			DOWN_MOST_LEFT_PRESSED = 24, DOWN_SIMPLE_PRESSED = 25, DOWN_MOST_RIGHT_PRESSED = 26;


	public static double calculateAction(RootCategory category, Calendar date) {
		double result = 0.0;
		if (category == null) return 0.0;
		Calendar begin = (Calendar) date.clone();
		begin.set(Calendar.HOUR_OF_DAY, 0);
		begin.set(Calendar.MINUTE, 0);
		begin.set(Calendar.SECOND, 0);
		begin.set(Calendar.MILLISECOND, 0);
		Calendar end = (Calendar) date.clone();
		end.set(Calendar.HOUR_OF_DAY, 23);
		end.set(Calendar.MINUTE, 59);
		end.set(Calendar.SECOND, 59);
		end.set(Calendar.MILLISECOND, 59);
//		for (int i=0; i<PocketAccounter.financeManager.getRecords().size(); i++) {
//			FinanceRecord record = PocketAccounter.financeManager.getRecords().get(i);
//			if (record.getDate().compareTo(begin) >= 0 && record.getDate().compareTo(end) <= 0 &&
//					record.getCategory().getId().matches(category.getId()))
//				result = result + getCost(record);
//		}
//		double totalAmount = 0.0;
//		for (int i=0; i<PocketAccounter.financeManager.getRecords().size(); i++) {
//			FinanceRecord record = PocketAccounter.financeManager.getRecords().get(i);
//			if (record.getDate().compareTo(begin) >= 0 && record.getDate().compareTo(end) <= 0
//					&& record.getCategory().getType() == category.getType())
//				totalAmount = totalAmount + getCost(record);
//		}
//		if (totalAmount == 0.0) return 0.0;
//		result = 100*result/totalAmount;
		return result;
	}

	public static Calendar getFirstExpenseDay(Account account) {
//		Calendar calendar = Calendar.getInstance();
//		ArrayList<FinanceRecord> records = new ArrayList<>();
//		for (FinanceRecord record : PocketAccounter.financeManager.getRecords()) {
//			if (record.getAccount().getId().matches(account.getId())) {
//				records.add(record);
//			}
//		}
//		ArrayList<DebtBorrow> debtBorrows = new ArrayList<>();
//		for (DebtBorrow debtBorrow : PocketAccounter.financeManager.getDebtBorrows()) {
//			if (debtBorrow.getAccount().getId().matches(account.getId())) {
//				debtBorrows.add(debtBorrow);
//			}
//			for (Recking recking : debtBorrow.getReckings()) {
//				if (recking.getAccountId().matches(account.getId())) {
//					debtBorrows.add(debtBorrow);
//				}
//			}
//		}
//		ArrayList<CreditDetials> creditDetialses = new ArrayList<>();
//		for (CreditDetials creditDetials: PocketAccounter.financeManager.getCredits()) {
//			for (ReckingCredit reckingCredit : creditDetials.getReckings()) {
//				creditDetialses.add(creditDetials);
//			}
//		}
//		for (CreditDetials creditDetials: creditDetialses) {
//			for (ReckingCredit reckingCredit : creditDetials.getReckings()) {
//				Calendar cal = Calendar.getInstance();
//				cal.setTimeInMillis(reckingCredit.getPayDate());
//				if (calendar.compareTo(cal) >= 0) {
//					calendar.setTimeInMillis(reckingCredit.getPayDate());
//				}
//			}
//		}
//		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
//		for (DebtBorrow debtBorrow : debtBorrows) {
//			if (calendar.compareTo(debtBorrow.getTakenDate()) >= 0) {
//				calendar.setTimeInMillis(debtBorrow.getTakenDate().getTimeInMillis());
//			}
//			for (Recking recking : debtBorrow.getReckings()) {
//				Calendar cal = Calendar.getInstance();
//				try {
//					cal.setTime(format.parse(recking.getPayDate()));
//				} catch (ParseException e) {
//					e.printStackTrace();
//				}
//				if (calendar.compareTo(cal) >= 0) {
//					calendar.setTimeInMillis(cal.getTimeInMillis());
//				}
//			}
//		}
//		for (FinanceRecord record : records) {
//			if (record.getDate().compareTo(calendar) <= 0) {
//				calendar.setTimeInMillis(record.getDate().getTimeInMillis());
//			}
//		}
//		return calendar;
		return  null;
	}


}