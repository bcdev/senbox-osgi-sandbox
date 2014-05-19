package org.teo.gui.command;

import org.teo.gui.Gui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.util.ArrayList;


public abstract class CommandBase extends AbstractAction implements Command {


    protected CommandBase(Gui gui, String id, String name) {
        putValue(ACTION_COMMAND_KEY, id);
        putValue(Action.NAME, name);
        putValue("gui", gui);
    }

    @Override
    public Gui getGui() {
        return (Gui) getValue("gui");
    }

    @Override
    public String getId() {
        return (String) getValue(ACTION_COMMAND_KEY);
    }

    @Override
    public String getName() {
        return (String) getValue(NAME);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        execute();
    }

    @Override
    public void updateState() {
    }
}
