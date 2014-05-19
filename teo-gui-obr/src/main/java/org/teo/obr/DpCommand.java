package org.teo.obr;

import org.osgi.service.deploymentadmin.DeploymentAdmin;
import org.osgi.service.deploymentadmin.DeploymentException;
import org.osgi.service.deploymentadmin.DeploymentPackage;
import org.teo.gui.Gui;
import org.teo.gui.command.CommandBase;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.prefs.Preferences;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

/**
 * Created by Norman on 20.02.14.
 */
class DpCommand extends CommandBase {

    private ObrManager obrManager;

    public DpCommand(Gui gui, ObrManager obrManager) {
        super(gui, "packageManager", "Package Manager...");
        this.obrManager = obrManager;
        setEnabled(false);
    }

    @Override
    public void updateState() {
        setEnabled(obrManager.getDeploymentAdmin() != null);
    }

    @Override
    public void execute() {

        final DeploymentAdmin deploymentAdmin = obrManager.getDeploymentAdmin();

        if (deploymentAdmin == null) {
            getGui().showInfo("No deployment admin found.");
            return;
        }

        final Cursor oldCursor = getGui().getMainFrame().getCursor();
        getGui().getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<DeploymentPackage[], String> swingWorker = new SwingWorker<DeploymentPackage[], String>() {
            @Override
            protected DeploymentPackage[] doInBackground() throws Exception {
                return deploymentAdmin.listDeploymentPackages();
            }

            @Override
            protected void done() {
                getGui().getMainFrame().setCursor(oldCursor);
                DeploymentPackage[] deploymentPackages;
                try {
                    deploymentPackages = get();
                    showDialog(deploymentPackages);
                } catch (Exception e) {
                    getGui().showError("Failed get installed packages", e);
                    e.printStackTrace();
                }
            }
        };

        swingWorker.run();

    }

    private void showDialog(final DeploymentPackage[] deploymentPackages) {


        TableModel tableModel = new DeploymentPackageTableModel(deploymentPackages);

        final JTable deploymentPackagesTable = new JTable(tableModel);

        final JButton installButton = new JButton("Install...");
        installButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                installFromFile();
            }
        });


        final JButton uninstallButton = new JButton("Uninstall");
        uninstallButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = deploymentPackagesTable.getSelectedRow();
                if (selectedRow >= 0) {
                    DeploymentPackage deploymentPackage = deploymentPackages[selectedRow];
                    uninstall(deploymentPackage);
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(installButton);
        buttonPanel.add(uninstallButton);
        JPanel panel = new JPanel(new BorderLayout(2, 2));
        panel.add(new JLabel(deploymentPackages.length + " packages(s) found:"), BorderLayout.NORTH);
        panel.add(new JScrollPane(deploymentPackagesTable, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        getGui().showDialog(panel, "Installed Deployment Packages");
    }

    private void installFromFile() {
        JFileChooser fileChooser = new JFileChooser(Preferences.userNodeForPackage(DpCommand.class).get("lastDpDir", "."));
        fileChooser.setAcceptAllFileFilterUsed(true);
        FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter("OSGi Deployment Package", "dp", "jar", "zip");
        fileChooser.addChoosableFileFilter(extensionFilter);
        fileChooser.setFileFilter(extensionFilter);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setDialogTitle("Install OSGi Deployment Package");
        int result = fileChooser.showDialog(getGui().getMainFrame(), "Install");
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File selectedFile = fileChooser.getSelectedFile();
        if (selectedFile == null) {
            return;
        }
        File currentDirectory = fileChooser.getCurrentDirectory();
        if (currentDirectory != null) {
            Preferences.userNodeForPackage(DpCommand.class).put("lastDpDir", currentDirectory.getPath());
        }
        final DeploymentAdmin deploymentAdmin = obrManager.getDeploymentAdmin();
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(selectedFile), 256 * 1024)) {
            deploymentAdmin.installDeploymentPackage(inputStream);
        } catch (DeploymentException e) {
            getGui().showError("Installing failed, error #" + e.getCode(), e);
        } catch (IOException e) {
            getGui().showError("I/O error", e);
        }
    }

    private void uninstall(DeploymentPackage deploymentPackage) {
        try {
            deploymentPackage.uninstall();
        } catch (DeploymentException e) {
            getGui().showError("Uninstalling failed", e);
        }
    }

    private static class DeploymentPackageTableModel extends AbstractTableModel {
        private final DeploymentPackage[] deploymentPackages;
        String[] columnNames;

        public DeploymentPackageTableModel(DeploymentPackage[] deploymentPackages) {
            this.deploymentPackages = deploymentPackages;
            columnNames = new String[]{"Name", "Version", "Icon"};
        }

        @Override
        public int getRowCount() {
            return deploymentPackages.length;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return String.class;
            } else if (columnIndex == 1) {
                return String.class;
            } else if (columnIndex == 2) {
                return Icon.class;
            }
            return null;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return deploymentPackages[rowIndex].getDisplayName();
            } else if (columnIndex == 1) {
                return deploymentPackages[rowIndex].getVersion().toString();
            } else if (columnIndex == 2) {
                URL url = deploymentPackages[rowIndex].getIcon();
                if (url != null) {
                    return new ImageIcon(url);
                }
            }
            return null;
        }
    }
}
