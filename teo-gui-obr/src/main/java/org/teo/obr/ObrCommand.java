package org.teo.obr;

import org.apache.felix.bundlerepository.Reason;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.bundlerepository.Requirement;
import org.apache.felix.bundlerepository.Resolver;
import org.apache.felix.bundlerepository.Resource;
import org.teo.gui.Gui;
import org.teo.gui.command.CommandBase;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;


class ObrCommand extends CommandBase {

    private ObrManager obrManager;

    public ObrCommand(Gui gui, ObrManager obrManager) {
        super(gui, "obrManager", "OSGi Bundle Repository...");
        this.obrManager = obrManager;
        setEnabled(false);
    }

    @Override
    public void updateState() {
        setEnabled(obrManager.getRepositoryAdmin() != null);
    }

    @Override
    public void execute() {
        final RepositoryAdmin repositoryAdmin = obrManager.getRepositoryAdmin();

        if (repositoryAdmin == null) {
            getGui().showInfo("No repository admin found.");
            return;
        }

        final Cursor oldCursor = getGui().getMainFrame().getCursor();
        getGui().getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<Resource[], String> swingWorker = new SwingWorker<Resource[], String>() {
            @Override
            protected Resource[] doInBackground() throws Exception {
                String filterExpr = null;
                Resource[] resources = repositoryAdmin.discoverResources(filterExpr);
                HashMap<String, Resource> latestVersionResources = new HashMap<>();
                for (Resource resource : resources) {
                    Resource existingResource = latestVersionResources.get(resource.getSymbolicName());
                    if (existingResource == null
                            || existingResource.getVersion().compareTo(resource.getVersion()) < 0) {
                        latestVersionResources.put(resource.getSymbolicName(), resource);
                    }
                }
                Collection<Resource> values = latestVersionResources.values();
                Resource[] sortedResources = values.toArray(new Resource[values.size()]);
                Arrays.sort(sortedResources, new Comparator<Resource>() {
                    @Override
                    public int compare(Resource o1, Resource o2) {
                        return o1.getPresentationName().compareTo(o2.getPresentationName());
                    }
                });
                return sortedResources;
            }

            @Override
            protected void done() {
                getGui().getMainFrame().setCursor(oldCursor);
                Resource[] resources;
                try {
                    resources = get();
                    showDialog(resources);
                } catch (Exception e) {
                    getGui().showError("Repository access failed", e);
                }
            }
        };

        swingWorker.run();

    }

    private void showDialog(final Resource[] resources) {

        TableModel tableModel = new ResourcesTableModel(resources);

        final JTable resourcesTable = new JTable(tableModel);

        final JButton installButton = new JButton("Install");
        installButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = resourcesTable.getSelectedRow();
                if (selectedRow >= 0) {
                    Resource resource = resources[selectedRow];
                    installAndStart(resource);
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(installButton);
        JPanel panel = new JPanel(new BorderLayout(2, 2));
        panel.add(new JLabel(resources.length + " resource(s) found:"), BorderLayout.NORTH);
        panel.add(new JScrollPane(resourcesTable, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        getGui().showDialog(panel, "Available Resources");
    }

    private void installAndStart(Resource resource) {
        final RepositoryAdmin repositoryAdmin = obrManager.getRepositoryAdmin();

        String resourceURI = resource.getURI();
        getGui().showInfo(String.format("Installing <b>%s</b> from<br>%s", resource.getSymbolicName(), resourceURI));

        Resolver resolver = repositoryAdmin.resolver();
        resolver.add(resource);
        if (resolver.resolve()) {
            resolver.deploy(Resolver.NO_SYSTEM_BUNDLE + Resolver.START);
            getGui().showInfo(String.format("Installed and started <b>%s</b>", resource.getSymbolicName()));
        } else {
            Reason[] reasons = resolver.getUnsatisfiedRequirements();
            StringBuilder message = new StringBuilder("There are unresolved requirements:<br/>");
            for (Reason reason : reasons) {
                Resource resource1 = reason.getResource();
                Requirement requirement = reason.getRequirement();
                message.append(String.format("  <b>%s</b>: %s<br/>", resource1.getSymbolicName(), requirement));
            }
            getGui().showError(message.toString(), null);
        }
    }

    private static class ResourcesTableModel extends AbstractTableModel {
        private final Resource[] resources;
        String[] columnNames;

        public ResourcesTableModel(Resource[] resources) {
            this.resources = resources;
            columnNames = new String[]{"Name", "Version", "Type", "Size"};
        }

        @Override
        public int getRowCount() {
            return resources.length;
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
                return String.class;
            } else if (columnIndex == 3) {
                return Long.class;
            } else {
                return null;
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return resources[rowIndex].getPresentationName();
            } else if (columnIndex == 1) {
                return resources[rowIndex].getVersion().toString();
            } else if (columnIndex == 2) {
                return Arrays.toString(resources[rowIndex].getCategories());
            } else if (columnIndex == 3) {
                return resources[rowIndex].getSize();
            } else {
                return null;
            }
        }
    }
}
