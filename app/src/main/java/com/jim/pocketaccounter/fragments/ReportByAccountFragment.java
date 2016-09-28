package com.jim.pocketaccounter.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.Account;
import com.jim.pocketaccounter.database.AutoMarket;
import com.jim.pocketaccounter.database.CreditDetials;
import com.jim.pocketaccounter.database.Currency;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.DebtBorrow;
import com.jim.pocketaccounter.database.FinanceRecord;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.managers.ReportManager;
import com.jim.pocketaccounter.managers.ToolbarManager;
import com.jim.pocketaccounter.report.AccountDataRow;
import com.jim.pocketaccounter.report.FilterSelectable;
import com.jim.pocketaccounter.report.ReportByAccount;
import com.jim.pocketaccounter.report.ReportObject;
import com.jim.pocketaccounter.report.TableView;
import com.jim.pocketaccounter.utils.FilterDialog;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ReportByAccountFragment extends Fragment implements View.OnClickListener {
    @Inject
    PAFragmentManager paFragmentManager;
    @Inject
    DaoSession daoSession;
    @Inject
    FilterDialog filterDialog;
    @Inject
    ToolbarManager toolbarManager;
    @Inject
    @Named(value = "display_formatter")
    SimpleDateFormat simpleDateFormat;
    @Inject
    ReportManager reportManager;

    private String[] titles;
    private int pos_account = 0, pos_currency = 0;
    private Calendar begin, end;
    private DecimalFormat decimalFormat;
    private Account account;
    private Currency currency;
    private TableView tbReportByAccount;
    //    private ReportByAccount reportByAccount;
    private ArrayList<ReportObject> sortReportByAccount;
    private LinearLayout linLayReportByAccountInfo;
    private TextView tvReportByAccountNoDatas;
    private TextView tvReportbyAccountTotalIncome, tvReportbyAccountTotalExpanse, tvReportbyAccountTotalProfit, tvReportbyAccountAverageProfit;
    private final int PERMISSION_READ_STORAGE = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        final View rootView = inflater.inflate(R.layout.report_by_account, container, false);

        rootView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (PocketAccounter.keyboardVisible) {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
                }
            }
        }, 100);

        rootView.setBackgroundColor(Color.WHITE);
        linLayReportByAccountInfo = (LinearLayout) rootView.findViewById(R.id.linLayReportByAccountInfo);
        tvReportbyAccountTotalIncome = (TextView) rootView.findViewById(R.id.tvReportbyAccountTotalIncome);
        tvReportbyAccountTotalExpanse = (TextView) rootView.findViewById(R.id.tvReportbyAccountTotalExpanse);
        tvReportbyAccountTotalProfit = (TextView) rootView.findViewById(R.id.tvReportbyAccountTotalProfit);
        tvReportbyAccountAverageProfit = (TextView) rootView.findViewById(R.id.tvReportbyAccountAverageProfit);

        toolbarManager.setToolbarIconsVisibility(View.GONE, View.VISIBLE, View.VISIBLE);
        toolbarManager.setImageToSecondImage(R.drawable.ic_excel);
        toolbarManager.setImageToFirstImage(R.drawable.ic_filter);
//        ivToolbarExcel = (ImageView) PocketAccounter.toolbar.findViewById(R.id.ivToolbarExcel);
//        ivToolbarExcel.setImageDrawable(null);
//        ivToolbarExcel.setImageResource(R.drawable.ic_excel);
        toolbarManager.setOnSecondImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permission = ContextCompat.checkSelfPermission(getContext(),
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(((PocketAccounter) getContext()),
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("Permission to access the SD-CARD is required for this app to Download PDF.")
                                .setTitle("Permission required");

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ActivityCompat.requestPermissions((PocketAccounter) getContext(),
                                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSION_READ_STORAGE);
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();

                    } else {
                        ActivityCompat.requestPermissions((PocketAccounter) getContext(),
                                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSION_READ_STORAGE);
                    }
                } else {
                    saveExcel();
                }
            }
        });
//        ivToolbarExcel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int permission = ContextCompat.checkSelfPermission(getContext(),
//                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
//                if (permission != PackageManager.PERMISSION_GRANTED) {
//                    if (ActivityCompat.shouldShowRequestPermissionRationale(((PocketAccounter) getContext()),
//                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                        builder.setMessage("Permission to access the SD-CARD is required for this app to Download PDF.")
//                                .setTitle("Permission required");
//
//                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                ActivityCompat.requestPermissions((PocketAccounter) getContext(),
//                                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                                        PERMISSION_READ_STORAGE);
//                            }
//                        });
//                        AlertDialog dialog = builder.create();
//                        dialog.show();
//
//                    } else {
//                        ActivityCompat.requestPermissions((PocketAccounter) getContext(),
//                                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                                PERMISSION_READ_STORAGE);
//                    }
//                } else {
//                    saveExcel();
//                }
//
//            }
//        });

//        ivToolbarMostRight = (ImageView) PocketAccounter.toolbar.findViewById(R.id.ivToolbarMostRight);
//        ivToolbarMostRight.setVisibility(View.VISIBLE);
//        ivToolbarMostRight.setImageResource(R.drawable.ic_filter);
//        ivToolbarMostRight.setOnClickListener(this);

//        PocketAccounter.toolbar.setTitle("");
//        PocketAccounter.toolbar.setSubtitle("");
        //        spToolbar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                pos_account = (int) (position / daoSession.getAccountDao().loadAll().size());
//                pos_currency = position % daoSession.getCurrencyDao().loadAll().size();
//                account = daoSession.getAccountDao().loadAll().get(pos_account);
//                currency = daoSession.getCurrencyDao().loadAll().get(pos_currency);
//
//                begin.set(Calendar.DAY_OF_YEAR, end.get(Calendar.DAY_OF_YEAR) - 2);
//                begin.set(Calendar.HOUR_OF_DAY, 0);
//                begin.set(Calendar.MINUTE, 0);
//                begin.set(Calendar.SECOND, 0);
//                begin.set(Calendar.MILLISECOND, 0);
//                PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("report_shp",account.getName()+", "+currency.getAbbr()).apply();
//                onCreateReportbyAccount(getContext(), begin, end, account, currency);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });

        toolbarManager.setOnFirstImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterDialog.show();
                filterDialog.setOnDateSelectedListener(new FilterSelectable() {
                    @Override
                    public void onDateSelected(Calendar begin, Calendar end) {
                        onCreateReportbyAccount(getContext(), begin, end, account, currency);
                    }
                });
            }
        });
        toolbarManager.setTitle("");
        toolbarManager.setSubtitle("");

        begin = Calendar.getInstance();
        end = Calendar.getInstance();

//        filterDialog = new FilterDialog(getContext());
//          spToolbar = (Spinner) PocketAccounter.toolbar.findViewById(R.id.spToolbar);
//        spToolbar.setVisibility(View.VISIBLE);
//        financeManager = PocketAccounter.financeManager;

        toolbarManager.setSpinnerVisibility(View.VISIBLE);
        final ArrayList<String> result = new ArrayList<>();

        for (int i = 0; i < daoSession.getAccountDao().loadAll().size(); i++) {
            for (int j = 0; j < daoSession.getCurrencyDao().loadAll().size(); j++) {
                result.add(daoSession.getAccountDao().loadAll().get(i).getName() + ", "
                        + daoSession.getCurrencyDao().loadAll().get(j).getAbbr());
            }
        }

        begin.set(Calendar.DAY_OF_YEAR, end.get(Calendar.DAY_OF_YEAR) - 2);
        begin.set(Calendar.HOUR_OF_DAY, 0);
        begin.set(Calendar.MINUTE, 0);
        begin.set(Calendar.SECOND, 0);
        begin.set(Calendar.MILLISECOND, 0);

        List<ReportObject> reportObjects = reportManager.getReportObjects(false, begin, end,
                Account.class, DebtBorrow.class, CreditDetials.class,
                FinanceRecord.class, AutoMarket.class);

        for (int i = result.size() - 1; i >= 0 ; i--) {
            pos_account = i / daoSession.getCurrencyDao().loadAll().size();
            pos_currency = i % daoSession.getCurrencyDao().loadAll().size();
            account = daoSession.getAccountDao().loadAll().get(pos_account);
            currency = daoSession.getCurrencyDao().loadAll().get(pos_currency);
            boolean bor = false;
            for (ReportObject reportObject : reportObjects) {
                if (reportObject.getCurrency().getId().matches(currency.getId()) &&
                        reportObject.getAccount().getId().matches(account.getId())) {
                    bor = true;
                    break;
                }
            }
            if (!bor)
                result.remove(i);
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getContext(), R.layout.spiner_gravity_right2, result);

        toolbarManager.getSpinner().setAdapter(arrayAdapter);
        toolbarManager.getSpinner().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pos_account = position / daoSession.getCurrencyDao().loadAll().size();
                pos_currency = position % daoSession.getCurrencyDao().loadAll().size();
                account = daoSession.getAccountDao().loadAll().get(pos_account);
                currency = daoSession.getCurrencyDao().loadAll().get(pos_currency);

                begin.set(Calendar.DAY_OF_YEAR, end.get(Calendar.DAY_OF_YEAR) - 2);
                begin.set(Calendar.HOUR_OF_DAY, 0);
                begin.set(Calendar.MINUTE, 0);
                begin.set(Calendar.SECOND, 0);
                begin.set(Calendar.MILLISECOND, 0);
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("report_shp", account.getName() + ", " + currency.getAbbr()).apply();
                onCreateReportbyAccount(getContext(), begin, end, account, currency);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        tbReportByAccount = (TableView) rootView.findViewById(R.id.tbReportByAccount);
        tvReportByAccountNoDatas = (TextView) rootView.findViewById(R.id.tvReportByAccountNoDatas);

        titles = rootView.getResources().getStringArray(R.array.report_by_account_titles);
        String tekwir = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("report_shp", "");

        if (!tekwir.matches("")) {
            int position = 0;
            for (String temp : result) {
                if (temp.equals(tekwir)) {
                    try {
                        toolbarManager.getSpinner().setSelection(position);
                    } catch (Exception o) {
                        o.printStackTrace();
                    }
                    break;
                }
                position++;
            }
        }
        tbReportByAccount.setTitle(titles, true);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivToolbarMostRight:
                filterDialog.show();
                filterDialog.setOnDateSelectedListener(new FilterSelectable() {
                    @Override
                    public void onDateSelected(Calendar begin, Calendar end) {
                        onCreateReportbyAccount(getContext(), begin, end, account, currency);
                    }
                });
                break;
            case R.id.ivToolbarExcel:
                exportToExcelFile();
                break;
        }
    }

    void onCreateReportbyAccount(Context context, Calendar begin, Calendar end, Account account, Currency currency) {
//        reportByAccount = new ReportByAccount(context, begin, end, account, currency);
        sortReportByAccount = (ArrayList<ReportObject>) reportManager.getReportObjects(false, begin, end,
                Account.class, DebtBorrow.class, CreditDetials.class, FinanceRecord.class, AutoMarket.class);

        Log.d("sss", currency.getAbbr() + " " + account.getName() + " " + sortReportByAccount.size());

        if (!sortReportByAccount.isEmpty())
            for (int i = sortReportByAccount.size() - 1; i >= 0; i--) {
                if (!sortReportByAccount.get(i).getAccount().getId().equals(
                        account.getId()) ||
                        !sortReportByAccount.get(i).getCurrency().getAbbr().equals(currency.getAbbr())) {
                    Log.d("sss2", sortReportByAccount.get(i).getAccount().getName() +
                            " " + sortReportByAccount.get(i).getCurrency().getAbbr());
                    sortReportByAccount.remove(i);
                }
            }

        Calendar current_begin = (Calendar) Calendar.getInstance().clone(), current_end = (Calendar) Calendar.getInstance().clone();
        Collections.sort(sortReportByAccount, new MyComparator());
        long countOfDays = 0;
        if (sortReportByAccount != null && sortReportByAccount.size() >= 2) {
            current_begin = (Calendar) sortReportByAccount.get(0).getDate().clone();
            current_end = (Calendar) sortReportByAccount.get(sortReportByAccount.size() - 1).getDate().clone();
            while (current_begin.getTime().compareTo(current_end.getTime()) <= 0) {
                countOfDays++;
                current_begin.add(Calendar.DAY_OF_MONTH, 1);
            }
        } else countOfDays = 1;
        if (sortReportByAccount.isEmpty()) {
            tbReportByAccount.setVisibility(View.GONE);
            linLayReportByAccountInfo.setVisibility(View.GONE);
            toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
            tvReportByAccountNoDatas.setVisibility(View.VISIBLE);
            return;
        } else {
            tbReportByAccount.setVisibility(View.VISIBLE);
            linLayReportByAccountInfo.setVisibility(View.VISIBLE);
            toolbarManager.setToolbarIconsVisibility(View.GONE, View.VISIBLE, View.VISIBLE);
//            ivToolbarExcel.setVisibility(View.VISIBLE);
            tvReportByAccountNoDatas.setVisibility(View.GONE);
        }
        decimalFormat = new DecimalFormat("0.00##");
        double totalIncome = 0.0, totalExpanse = 0.0, totalProfit = 0.0, averageProfit = 0.0;
        String abbr = "";
        for (int i = 0; i < sortReportByAccount.size(); i++) {
            abbr = sortReportByAccount.get(i).getCurrency().getAbbr();
            if (sortReportByAccount.get(i).getType() == 0)
                totalIncome = totalIncome + sortReportByAccount.get(i).getAmount();
            else
                totalExpanse = totalExpanse + sortReportByAccount.get(i).getAmount();
        }
        tbReportByAccount.setDatas(sortReportByAccount);
        totalProfit = totalIncome - totalExpanse;
        averageProfit = totalProfit / countOfDays;
        tvReportbyAccountTotalIncome.setText(getResources().getString(R.string.report_income_expanse_total_income) + decimalFormat.format(totalIncome) + abbr);
        tvReportbyAccountTotalExpanse.setText(getResources().getString(R.string.report_income_expanse_total_expanse) + decimalFormat.format(totalExpanse) + abbr);
        tvReportbyAccountTotalProfit.setText(getResources().getString(R.string.report_income_expanse_total_profit) + decimalFormat.format(totalProfit) + abbr);
        tvReportbyAccountAverageProfit.setText(getResources().getString(R.string.report_income_expanse_aver_profit) + decimalFormat.format(averageProfit) + abbr);
    }

    class MyComparator implements Comparator<ReportObject> {
        @Override
        public int compare(ReportObject o1, ReportObject o2) {
            return o1.getDate().compareTo(o2.getDate());
        }
    }

    private void saveExcel() {
        File direct = new File(Environment.getExternalStorageDirectory() + "/Pocket Accounter");
        if (!direct.exists()) {
            if (direct.mkdir()) {
                exportToExcelFile();
            }
        } else {
            exportToExcelFile();
        }
    }

    public void exportToExcelFile() {
        final Dialog dialog = new Dialog(getContext());
        View dialogView = ((PocketAccounter) getContext()).getLayoutInflater().inflate(R.layout.warning_dialog, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        TextView tvWarningText = (TextView) dialogView.findViewById(R.id.tvWarningText);
        tvWarningText.setText(R.string.save_excel);
        Button ok = (Button) dialogView.findViewById(R.id.btnWarningYes);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File sd = Environment.getExternalStorageDirectory();
                String fname = sd.getAbsolutePath() + "/" +
                        "Pocket Accounter/" +
                        "ra_" + simpleDateFormat.format(Calendar.getInstance().getTime());
                File temp = new File(fname + ".xlsx");
                while (temp.exists()) {
                    fname = fname + "_copy";
                    temp = new File(fname);
                }
                fname = fname + ".xlsx";
                try {
                    File exlFile = new File(fname);
                    WritableWorkbook writableWorkbook = Workbook.createWorkbook(exlFile);
                    WritableSheet writableSheet = writableWorkbook.createSheet(getContext().getResources().getString(R.string.app_name), 0);
                    String[] labels = getResources().getStringArray(R.array.report_by_account_titles);

                    for (int i = 0; i < labels.length; i++) {
                        Label label = new Label(i, 0, labels[i]);
                        writableSheet.addCell(label);
                    }
                    for (int i = 0; i < sortReportByAccount.size(); i++) {
                        Number type = new Number(0, i + 1, sortReportByAccount.get(i).getType());
                        Label date = new Label(1, i + 1, simpleDateFormat.format(sortReportByAccount.get(i).getDate().getTime()));
                        Number amount = new Number(2, i + 1, sortReportByAccount.get(i).getAmount());
                        Label category = new Label(3, i + 1, sortReportByAccount.get(i).getDescription());
                        writableSheet.addCell(type);
                        writableSheet.addCell(date);
                        writableSheet.addCell(amount);
                        writableSheet.addCell(category);
                    }
                    writableWorkbook.write();
                    writableWorkbook.close();
                    Toast.makeText(getContext(), fname + ": saved...", Toast.LENGTH_SHORT).show();
                } catch (IOException | WriteException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        Button cancel = (Button) dialogView.findViewById(R.id.btnWarningNo);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}