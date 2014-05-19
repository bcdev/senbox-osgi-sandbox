package org.teo.gui.command;

import org.teo.gui.Gui;

import javax.swing.Action;


public interface Command extends Action {
    Gui getGui();

    String getId();

    String getName();

    void execute();

    void updateState();

}
