package org.teo.gui.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.teo.gui.Gui;
import org.teo.gui.command.Command;
import org.teo.gui.command.CommandGroup;
import org.teo.gui.command.CommandGroupImpl;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiImpl implements Gui {

    private BundleContext bundleContext;
    private ExitCommand exitCommand;

    public GuiImpl(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public static void main(String[] args) {
        new GuiImpl(null).run();
    }

    JFrame mainFrame;
    JMenuBar menuBar;
    List<CommandGroup> commandGroups = new ArrayList<>();
    Map<CommandGroup, JMenu> commandMenus = new HashMap<>();

    @Override
    public JFrame getMainFrame() {
        return mainFrame;
    }


    private void updateCommandsState() {
        for (CommandGroup commandGroup : commandGroups) {
            commandGroup.updateState();
        }
    }

    public void init() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        addCommandGroup("file", "File");
        addCommandGroup("edit", "Edit");
        addCommandGroup("view", "View");
        addCommandGroup("tools", "Tools");
        addCommandGroup("window", "Window");
        addCommandGroup("help", "Help");

        exitCommand = new ExitCommand(this);
        getCommandGroup("file").addCommand(exitCommand);
    }

    public void run() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // ok
        }

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                menuBar = new JMenuBar();
                menuBar.setVisible(true);
                for (CommandGroup commandGroup : commandGroups) {
                    JMenu menu = createMenu(commandGroup);
                    addCommands(menu, commandGroup.getCommands(), false);
                    menuBar.add(menu);
                }

                mainFrame = new JFrame("Teo OSGi Sandbox");
                mainFrame.setContentPane(new JLabel(new ImageIcon(GuiImpl.class.getResource("gui-background.jpg"))));
                mainFrame.setJMenuBar(menuBar);
                mainFrame.pack();
                mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                mainFrame.addWindowListener(new AppWindowListener());

                Dimension windowSize = mainFrame.getSize();
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                mainFrame.setLocation((screenSize.width - windowSize.width) / 2, (screenSize.height - windowSize.height) / 2);
                mainFrame.setVisible(true);
                mainFrame.requestFocus();
                mainFrame.toFront();
            }
        });
    }

    @Override
    public void shutDown() {
        mainFrame.dispose();
        if (bundleContext != null) {
            try {
                bundleContext.getBundle(0).stop();
            } catch (BundleException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    @Override
    public void showInfo(String message) {
        JOptionPane.showMessageDialog(getMainFrame(), "<html>" + message, "Teo Info", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void showError(String message, Throwable e) {
        String m = "<html>" + message;
        if (e != null) {
            e.printStackTrace();
            if (e.getMessage() != null) {
                m += ":<br/>Message: <b>" + e.getMessage() + "</b>";
            }
        }
        JOptionPane.showMessageDialog(getMainFrame(), m, "Teo Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public int showConfirm(String message, int options) {
        return JOptionPane.showConfirmDialog(getMainFrame(), "<html>" + message, "Teo Question", options, JOptionPane.QUESTION_MESSAGE);
    }

    @Override
    public void showDialog(JComponent contentPane, String title) {
        JOptionPane.showMessageDialog(getMainFrame(), contentPane, title, JOptionPane.PLAIN_MESSAGE);
    }

    private void addCommands(JMenu parentMenu, Command[] commands, boolean check) {
        for (final Command command : commands) {

            if (check) {
                String commandId = command.getId();
                JMenuItem menuItem = getMenuItem(parentMenu, commandId);
                if (menuItem != null) {
                    menuItem.setAction(command);
                    return;
                }
            }

            addMenuItemsForCommand(parentMenu, command);
        }
    }

    private void addMenuItemsForCommand(JMenu parentMenu, Command command) {
        JMenuItem menuItem;
        if (command instanceof CommandGroup) {
            CommandGroup subGroup = (CommandGroup) command;
            JMenu menu = createMenu(subGroup);
            addCommands(menu, subGroup.getCommands(), false);
            menuItem = menu;
        } else {
            menuItem = createMenuItem(command);
        }
        parentMenu.add(menuItem);
    }

    private JMenuItem createMenuItem(Command command) {
        JMenuItem menuItem = new JMenuItem();
        menuItem.setName(command.getId());
        menuItem.setAction(command);
        return menuItem;
    }

    private JMenu createMenu(CommandGroup commandGroup) {
        JMenu menu = new JMenu(commandGroup.getName());
        menu.setName(commandGroup.getId());
        menu.setMnemonic(commandGroup.getName().charAt(0));
        commandMenus.put(commandGroup, menu);
        return menu;
    }



    private JMenuItem getMenuItem(JMenu parentMenu, String commandId) {
        int itemCount = parentMenu.getItemCount();
        for (int i = 0; i < itemCount; i++) {
            JMenuItem menuItem = parentMenu.getItem(i);
            if (commandId.equals(menuItem.getName())) {
                return menuItem;
            }
        }
        return null;
    }

    private JMenu getMenu(String commandId) {
        if (menuBar == null) {
            return null;
        }
        int itemCount = menuBar.getMenuCount();
        for (int i = 0; i < itemCount; i++) {
            JMenu menu = menuBar.getMenu(i);
            if (commandId.equals(menu.getName())) {
                return menu;
            }
        }
        return null;
    }

    @Override
    public CommandGroup getCommandGroup(String id) {
        for (CommandGroup commandGroup : commandGroups) {
            if (id.equals(commandGroup.getId())) {
                return commandGroup;
            }
        }
        return null;
    }

    @Override
    public CommandGroup addCommandGroup(String id, String text) {
        CommandGroup commandGroup = new CommandGroupImpl(this, id, text);
        commandGroup.addListener(new CommandGroupListener());
        commandGroups.add(commandGroup);
        return commandGroup;
    }

    @Override
    public CommandGroup removeCommandGroup(String id) {
        CommandGroup commandGroup = getCommandGroup(id);
        if (commandGroup != null) {
            commandGroups.remove(commandGroup);
            commandMenus.remove(commandGroup);
            JMenu menu = getMenu(id);
            if (menu != null) {
                menuBar.remove(menu);
            }
        }
        return commandGroup;
    }

    private class AppWindowListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            exitCommand.execute();
        }
    }

    private class CommandGroupListener implements CommandGroup.Listener {
        @Override
        public void commandAdded(CommandGroup commandGroup, Command command) {
            JMenu parentMenu = commandMenus.get(commandGroup);
            if (parentMenu != null){
                addMenuItemsForCommand(parentMenu, command);
            }

            if (command instanceof CommandGroup) {
                CommandGroup subGroup = (CommandGroup) command;
                subGroup.addListener(this);
            }
        }

        @Override
        public void commandRemoved(CommandGroup commandGroup, Command command) {
            if (command instanceof CommandGroup) {
                CommandGroup subGroup = (CommandGroup) command;
                subGroup.removeListener(this);
                commandMenus.remove(subGroup);
            }

            JMenu parentMenu = commandMenus.get(commandGroup);
            if (parentMenu != null) {
                JMenuItem menuItem = getMenuItem(parentMenu, command.getId());
                if (menuItem != null) {
                    parentMenu.remove(menuItem);
                }
            }
        }
    }
}
