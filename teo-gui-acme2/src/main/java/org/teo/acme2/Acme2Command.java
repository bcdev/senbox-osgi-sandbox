package org.teo.acme2;

import org.teo.gui.Gui;
import org.teo.gui.command.CommandBase;

class Acme2Command extends CommandBase {

    public static final String ID = "acme2Command";

    public Acme2Command(Gui gui) {
        super(gui, ID, "ACME Command #2...");
    }

    @Override
    public void execute() {
        getGui().showInfo("ACME Command #2 called.");
    }
}
