package com.objectfrontier.invoice.excel.client.ui;

import com.objectfrontier.invoice.excel.ExcelInvoiceReader;
import com.objectfrontier.invoice.excel.reports.sales.ExcelSalesReportWriter;
import com.objectfrontier.invoice.excel.system.Utils;
import com.objectfrontier.job.Task;
import com.objectfrontier.model.ClientAccount;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
  private JProgressBar overallReportProgressBar;
  private JTextArea logTextArea;
  private JScrollPane logScrollPane;
  private JButton saveReportAsButton;
  private JButton clearLogButton;
  private JButton saveLogButton;
  private JProgressBar currentTaskProgress;

  private int currentYear;
  private int currentMonth;
  private ExcelInvoiceReader invoiceReader;


  boolean completed = false;
  boolean anyError = false;

  private XSSFWorkbook reportWorkbook;

  private Utils utils;

  public ReportUI() {

    utils = Utils.getInstance();
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
    DefaultCaret caret = (DefaultCaret)logTextArea.getCaret();
    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    initializeProgressBars();
  }

  private void initializeProgressBars() {
    overallReportProgressBar.setStringPainted(true);
    currentTaskProgress.setStringPainted(true);
    overallReportProgressBar.setBorderPainted(true);
    currentTaskProgress.setBorderPainted(true);
  }

  private void initializeLogHandler() {

    Handler handler = new Handler() {
      @Override public void publish(LogRecord record) {
        StringBuilder buffer = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss.SSS");
        String time = dateFormat.format(new Date(record.getMillis()));
        buffer.append(time).append("\t");
        buffer.append(record.getLevel().getName()).append("\t");
        buffer.append("[").append(String.format("%s.%s", record.getLoggerName(), record.getSourceMethodName())).append(
                        "]\t");
        buffer.append(record.getMessage());
        log(buffer.toString());
      }

      @Override public void flush() {}

      @Override public void close() throws SecurityException {}
    };

    utils.setHandler(handler);
  }

  private void populateMonths() {
    DefaultListModel<String> listModel = new DefaultListModel<String>();
    for(MONTH value : MONTH.values()) {
      listModel.addElement(value.toString());
    }
    monthList.setModel(listModel);
    monthList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    monthList.setSelectedIndex(currentMonth - 1);
  }

  private void populateYears() {
    for (int i=2015;i <= currentYear; i++)
      yearComboBox.addItem(i);
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
        try {
          onGenerateReport();
        } catch (Exception ex) {
          log(ex);
        }
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

    clearLogButton.addActionListener(new ActionListener() {
      @Override public void actionPerformed(ActionEvent e) {
        onClearLog();
      }
    });

    logTextArea.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void insertUpdate(DocumentEvent e) {
        toggleClearLogButton();
      }

      @Override public void removeUpdate(DocumentEvent e) {
        toggleClearLogButton();
      }

      @Override public void changedUpdate(DocumentEvent e) {
        toggleClearLogButton();
      }
    });

    saveReportAsButton.addActionListener(new ActionListener() {
      @Override public void actionPerformed(ActionEvent e) {
        onSaveReportAs();
      }
    });

    saveLogButton.addActionListener(new ActionListener() {
      @Override public void actionPerformed(ActionEvent e) {
        onSaveLog();
      }
    });
  }

  private void onSaveReportAs() {
    String location;
    JFileChooser outputFileChooser = new JFileChooser();
    outputFileChooser.setDialogTitle("Select Invoice Home Folder Location");
    outputFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    outputFileChooser.setFileFilter(new FileNameExtensionFilter("Excel 2010 and above", "xlsx"));
    if (outputFileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      location = outputFileChooser.getSelectedFile().getAbsolutePath();
      if(!location.endsWith(".xlsx")) location = location + ".xlsx";
      saveReport(new File(location));
      JOptionPane.showMessageDialog(this, "Report Saved.");
    }
  }

  private void onSaveLog() {
    String str = logTextArea.getText();
    String location ;
    try {
      JFileChooser outputFileChooser = new JFileChooser();
      outputFileChooser.setDialogTitle("Select Invoice Home Folder Location");
      outputFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      outputFileChooser.setFileFilter(new FileNameExtensionFilter("CompressedLog File", "zip"));
      if (outputFileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        location = outputFileChooser.getSelectedFile().getAbsolutePath();
        if(!location.endsWith(".zip")) location = location + ".zip";

        JOptionPane.showMessageDialog(this, "Log Saved.");
      } else {
        return;
      }

      ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(location ));
      zipOutputStream.putNextEntry(new ZipEntry(outputFileChooser.getSelectedFile().getName() + ".log"));
      zipOutputStream.write(str.getBytes());
      zipOutputStream.flush();
      zipOutputStream.closeEntry();
      zipOutputStream.close();
    } catch (Exception ex) {
      log(ex);
      JOptionPane.showMessageDialog(this, "Something went wrong :  when saving log");
    }
  }

  private void onClearLog() {
    logTextArea.setText("");
  }

  private void toggleClearLogButton() {
    clearLogButton.setEnabled(logTextArea.getText().length() > 0);
    saveLogButton.setEnabled(logTextArea.getText().length() > 0);
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
    toggleSaveReportAsButton();

  }

  private void toggleSaveReportAsButton() {
    saveReportAsButton.setEnabled(reportWorkbook != null && generateReportButton.isEnabled());
  }

  private void onGenerateReport() throws Exception{
    completed = false;
    final ListModel listModel = monthList.getModel();
    toggleButtonEnablement();
    SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {

      @Override
      protected Boolean doInBackground() throws Exception {
        int counter = 0;
        overallReportProgressBar.setValue(0);
        overallReportProgressBar.setMaximum(monthList.getSelectedIndices().length * 2);
        log("I'm working for you to generate the report you requested");
        Task task = new Task() {
          boolean running;
          @Override public void add() {
            int max = currentTaskProgress.getMaximum() + 1;
            currentTaskProgress.setMaximum(max);
          }

          @Override public void done() {
            int current = currentTaskProgress.getValue() + 1;
            currentTaskProgress.setValue(current);
          }

          @Override public void restart() {
            currentTaskProgress.setValue(0);
            currentTaskProgress.setMaximum(0);
            running = true;
          }

        };

        invoiceReader = new ExcelInvoiceReader(task);
        clearLogButton.setEnabled(false);
        saveLogButton.setEnabled(false);
        logTextArea.setText("");
        long totalDuration = System.currentTimeMillis();
        List<Long> reportConstructionDurations = new ArrayList();
        for(int index : monthList.getSelectedIndices()) {
          long duration = System.currentTimeMillis();
          try {
            MONTH month = MONTH.valueOf(listModel.getElementAt(index).toString());
            int year = Integer.parseInt(yearComboBox.getSelectedItem().toString());
            currentTaskProgress.setString(String.format("Parsing invoices for %s %d...", month.toString(), year));
            Map<String, ClientAccount> clientAccounts = invoiceReader.parseAllClientInvoice(year, month);
            ExcelSalesReportWriter reportWriter = new ExcelSalesReportWriter(clientAccounts, task);
            publish(++counter);
            currentTaskProgress.setString(String.format("Writing report for %s %d...", month.toString(), year));
            reportWorkbook = reportWriter.getSalesReport(loadWorkbook(), year, month);
            if (reportWorkbook != null) {
              if (reportOutputField.getText().length() > 0) {saveReport(getOutputFile());}
              toggleSaveReportAsButton();
            }
            publish(++counter);
          } catch (Exception ex) {
            log(ex);
            log("Something went wrong :  " + ex.getMessage());
            anyError = true;
          } finally {
            task.restart();
            duration = System.currentTimeMillis() - duration;
            reportConstructionDurations.add(duration);
            duration = getAverage(reportConstructionDurations);
            float seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60;
            float min = TimeUnit.MILLISECONDS.toMinutes(duration);
            overallReportProgressBar.setString(String.format("Agv Time taken / monthly report: %2.0f min, %2.2f sec ", min, seconds));
          }
        }
        currentTaskProgress.setString("Completed!!!");
        clearLogButton.setEnabled(true);
        saveLogButton.setEnabled(true);
        completed = true;
        totalDuration = System.currentTimeMillis() - totalDuration;
        float seconds = TimeUnit.MILLISECONDS.toSeconds(totalDuration) % 60;
        float min = TimeUnit.MILLISECONDS.toMinutes(totalDuration);
        String totalTimeTaken = String.format("Completed in  %2.0f min, %2.2f sec ", min, seconds);
        log(totalTimeTaken);
        overallReportProgressBar.setString(totalTimeTaken);
        return true;
      }

      @Override protected void process(List<Integer> chunks) {
        overallReportProgressBar.setValue(chunks.get(0));
      }

      @Override protected void done() {
        overallReportProgressBar.setValue(monthList.getSelectedIndices().length * 2);
        toggleButtonEnablement();
        toggleSaveReportAsButton();

        String progressMessage = anyError ? "Completed with few errors" : "Hurry!!! Report generated successfully";
        String additional = anyError ? " with few errors" : "";
//        overallReportProgressBar.setString(progressMessage);
        JOptionPane.showMessageDialog(overallReportProgressBar.getRootPane(),
                        progressMessage + additional
                                        + ".\nLet me know what you think.");
      }
    };
    worker.execute();
  }

  private long getAverage(List<Long> values) {
    long total = 0;
    for(long value : values) {
      total += value;
    }
    return (total/values.size());
  }

  private void saveIndividualReport(File file) {

  }

  private void saveReport(File file) {
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(file);
      reportWorkbook.write(fos);
      fos.flush();
    } catch (Exception ex) {
      ex.printStackTrace();
      log("Something went wrong :  " + ex.getMessage());
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException e) {
          log("Something went wrong :  on closing output stream");
          log(e.getMessage());
          e.printStackTrace();
        }
      }
    }
  }

  private void toggleButtonEnablement() {
    homeBrowseButton.setEnabled(!homeBrowseButton.isEnabled());
    outputBrowseButton.setEnabled(!outputBrowseButton.isEnabled());
    generateReportButton.setEnabled(!generateReportButton.isEnabled());
    quitButton.setEnabled(!quitButton.isEnabled());
  }

  private XSSFWorkbook loadWorkbook() {
    if (reportOutputField.getText().length() == 0) return null;
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

  private void log(Object content) {
    if (content instanceof Exception) {
      content = "\nERROR: " + utils.getStackTrace((Exception)content);
    }
    logTextArea.append(content + "\n");
  }

  public static void main(String[] args) {
    ReportUI dialog = new ReportUI();
    dialog.pack();
    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);
  }
}
