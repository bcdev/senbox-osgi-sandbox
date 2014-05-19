package org.teo.gui.impl;

import org.teo.gui.command.CommandBase;

import javax.swing.JOptionPane;

/**
 * Created by Norman on 15.05.2014.
 */
class ExitCommand extends CommandBase {

    public ExitCommand(GuiImpl gui) {
        super(gui, "exit", "Exit");
    }

    @Override
    public void execute() {
        shutDown(false);
    }

    private void shutDown(boolean prompt) {
        boolean ok = true;
        if (prompt) {
            int resp = getGui().showConfirm("Really exit?", JOptionPane.YES_NO_OPTION);
            ok = resp == JOptionPane.YES_OPTION;
        }
        if (ok) {
            getGui().shutDown();
        }
    }
}
