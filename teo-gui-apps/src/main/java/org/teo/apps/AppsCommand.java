package org.teo.apps;

import org.osgi.service.application.ApplicationDescriptor;
import org.osgi.service.application.ApplicationException;
import org.teo.gui.Gui;
import org.teo.gui.command.CommandBase;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

/**
 * Created by Norman on 20.02.14.
 */
class AppsCommand extends CommandBase {

    public static final String ID = "listapps";

    AppsCommand(Gui gui) {
        super(gui, ID, "Executables...");
    }

    @Override
    public void execute() {

        DefaultTableModel tableModel = new DefaultTableModel();

        tableModel.setColumnCount(4);
        tableModel.setColumnIdentifiers(new Object[]{
                "Name", "Visible", "Launchable", "Locked"
        });
        final ApplicationDescriptor[] applicationDescriptors = AppsActivator.getAppsTracker().getLaunchableApps();
        for (ApplicationDescriptor applicationDescriptor : applicationDescriptors) {
            Map properties = applicationDescriptor.getProperties("en");
            tableModel.addRow(new Object[]{
                    applicationDescriptor.getApplicationId(),
                    //properties.get(ApplicationDescriptor.APPLICATION_NAME),
                    properties.get(ApplicationDescriptor.APPLICATION_VISIBLE),
                    properties.get(ApplicationDescriptor.APPLICATION_LAUNCHABLE),
                    properties.get(ApplicationDescriptor.APPLICATION_LOCKED),
            });
        }

        final JTable table = new JTable(tableModel);

        JButton launchButton = new JButton("Launch");
        launchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    try {
                        applicationDescriptors[selectedRow].launch(null);
                    } catch (ApplicationException e1) {
                        getGui().showError("Application launch failure", e1);
                    }
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(launchButton);
        JPanel panel = new JPanel(new BorderLayout(2, 2));
        panel.add(new JLabel(applicationDescriptors.length + " app(s) found:"), BorderLayout.NORTH);
        panel.add(new JScrollPane(table, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        getGui().showDialog(panel, "Registered Applications");
    }
}
