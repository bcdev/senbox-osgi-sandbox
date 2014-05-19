package org.teo.gui;

import org.teo.gui.command.CommandGroup;

import javax.swing.JComponent;
import javax.swing.JFrame;

public interface Gui {

    JFrame getMainFrame();

    CommandGroup getCommandGroup(String id);

    CommandGroup addCommandGroup(String id, String text);

    CommandGroup removeCommandGroup(String id);

    void shutDown();

    void showInfo(String message);

    void showError(String message, Throwable e);

    int showConfirm(String message, int options);

    void showDialog(JComponent contentPane, String title);
}
