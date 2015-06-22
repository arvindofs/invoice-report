package com.objectfrontier.invoice.excel.client.ui;

import com.objectfrontier.invoice.excel.reports.sales.ExcelInvoiceReader;
import com.objectfrontier.invoice.excel.reports.sales.ExcelSalesReportWriter;
import com.objectfrontier.invoice.excel.system.Progress;
import com.objectfrontier.invoice.excel.system.Utils;
import com.objectfrontier.model.ClientAccount;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static com.objectfrontier.invoice.excel.system.InvoiceUtil.DROPBOX_HOME;
import static com.objectfrontier.invoice.excel.system.InvoiceUtil.MONTH;

public class ReportUI extends JFrame {

  private JPanel contentPane;
  private JButton quitButton;
  private JTextField invoiceHomeField;
  private JButton homeBrowseButton;
  private JTextField reportOutputField;
  private JButton outputBrowseButton;
  private JButton generateReportButton;
  private JComboBox yearComboBox;
  private JList monthList;
  private JProgressBar reportProgressBar;
  private JTextArea logTextArea;
  private JScrollPane logScrollPane;

  private int currentYear;
  private int currentMonth;
  private ExcelInvoiceReader invoiceReader;

  private Progress progress = Progress.instance();
  boolean completed = false;

  private Handler handler;

  public ReportUI() {
    initializeLogHandler();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
    currentYear = Integer.parseInt(dateFormat.format(new Date()));
    dateFormat= new SimpleDateFormat("MM");
    currentMonth = Integer.parseInt(dateFormat.format(new Date()));


    setContentPane(contentPane);
    setTitle("Sales Report Utility");
    getRootPane().setDefaultButton(quitButton);
    populateMonths();
    populateYears();
    toggleGenerateReportButton();
    addListeners();
  }

  private void initializeLogHandler() {

    handler = new Handler() {
      @Override public void publish(LogRecord record) {
        log(record.getMessage());
      }

      @Override public void flush() {

      }

      @Override public void close() throws SecurityException {

      }
    };

    Utils.getInstance().setHandler(handler);
  }

  private void populateMonths() {
    DefaultListModel<String> listModel = new DefaultListModel<String>();
    for(MONTH value : MONTH.values()) {
      listModel.addElement(value.toString());
    }
    monthList.setModel(listModel);
    monthList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    monthList.setSelectedIndex(currentMonth-1);
  }

  private void populateYears() {
    for (int i=2015;i <= currentYear; i++) {
      yearComboBox.addItem(i);
    }
    yearComboBox.setSelectedItem(currentYear);
  }

  private void addListeners() {
    homeBrowseButton.addActionListener(new ActionListener() {
      @Override public void actionPerformed(ActionEvent e) {
        onHomeBrowse();
      }
    });

    outputBrowseButton.addActionListener(new ActionListener() {
      @Override public void actionPerformed(ActionEvent e) {
        onOutputBrowse();
      }
    });

    generateReportButton.addActionListener(new ActionListener() {
      @Override public void actionPerformed(ActionEvent e) {
        onGenerateReport();
      }
    });
    quitButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onCancel();
      }
    });

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        onCancel();
      }
    });

    monthList.addListSelectionListener(new ListSelectionListener() {
      @Override public void valueChanged(ListSelectionEvent e) {
        toggleGenerateReportButton();
      }
    });
  }

  private void toggleGenerateReportButton() {
    boolean enabled = true;
    if (invoiceHomeField.getText().length() == 0) {
      enabled = false;
    }

    if (reportOutputField.getText().length() == 0) {
     enabled = false;
    }

    if (yearComboBox.getSelectedObjects().length == 0) {
      enabled = false;
    }
    if (monthList.getSelectedIndices().length == 0) {
      enabled = false;
    }

    generateReportButton.setEnabled(enabled);

  }

  private void onGenerateReport() {

    completed = false;
    final ListModel listModel = monthList.getModel();
    invoiceReader = new ExcelInvoiceReader();
    toggleButtonEnablement();
    SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {

      @Override
      protected Boolean doInBackground() throws Exception {
        int counter = 0;
        reportProgressBar.setValue(0);;
        reportProgressBar.setMaximum(monthList.getSelectedIndices().length * 2);

        for(int index : monthList.getSelectedIndices()) {
          try {
            reportProgressBar.setStringPainted(true);
            MONTH month = MONTH.valueOf(listModel.getElementAt(index).toString());
            int year = Integer.parseInt(yearComboBox.getSelectedItem().toString());
            Map<String, ClientAccount> clientAccounts = invoiceReader.buildSalesReport(year,month);
            publish(++counter);
            ExcelSalesReportWriter reportWriter = new ExcelSalesReportWriter(clientAccounts);
            XSSFWorkbook workbook = reportWriter.getSalesReport(loadWorkbook(), year, month);
            FileOutputStream fos = new FileOutputStream(getOutputFile());
            workbook.write(fos);;
            fos.flush();
            fos.close();
            publish(++counter);
          } catch (Exception ex) {
            ex.printStackTrace();
            log("Exception occured " + ex.getMessage());
          }
        }
        completed = true;
        return true;
      }

      @Override protected void process(List<Integer> chunks) {
        reportProgressBar.setValue(chunks.get(0).intValue());
      }

      @Override protected void done() {
        reportProgressBar.setValue(monthList.getSelectedIndices().length * 2);
        toggleButtonEnablement();
        JOptionPane.showMessageDialog(reportProgressBar.getRootPane(), "Sales report generation completed.");
      }
    };

    worker.execute();

  }

  private void toggleButtonEnablement() {
    homeBrowseButton.setEnabled(!homeBrowseButton.isEnabled());
    outputBrowseButton.setEnabled(!outputBrowseButton.isEnabled());
    generateReportButton.setEnabled(!generateReportButton.isEnabled());
    quitButton.setEnabled(!quitButton.isEnabled());
  }

  private XSSFWorkbook loadWorkbook() {
    try {
      File file = getOutputFile();
      if (!file.exists()) return null;
      return new XSSFWorkbook(new FileInputStream(file));
    } catch (Exception ex) {
      return null;
    }
  }

  private File getOutputFile() {
    return  new File(reportOutputField.getText() + File.separator + "Sales-Report.xlsx");
  }

  private void onHomeBrowse() {
    JFileChooser homeChooser = new JFileChooser();
    homeChooser.setDialogTitle("Select Invoice Home Folder Location");
    homeChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    if (homeChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      invoiceHomeField.setText(homeChooser.getSelectedFile().getAbsolutePath());
      System.setProperty(DROPBOX_HOME, invoiceHomeField.getText());
    }
    toggleGenerateReportButton();
  }

  private void onOutputBrowse() {
    JFileChooser outputFolderChooser = new JFileChooser();
    outputFolderChooser.setDialogTitle("Select Invoice Home Folder Location");
    outputFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    if (outputFolderChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      reportOutputField.setText(outputFolderChooser.getSelectedFile().getAbsolutePath());
    }
    toggleGenerateReportButton();
  }


  private void onCancel() {
    System.exit(0);
  }

  private void log(Object o) {
    logTextArea.append(o + "\n");
  }

  public static void main(String[] args) {
    ReportUI dialog = new ReportUI();
    dialog.pack();
    dialog.setVisible(true);
//    System.exit(0);
  }
}
