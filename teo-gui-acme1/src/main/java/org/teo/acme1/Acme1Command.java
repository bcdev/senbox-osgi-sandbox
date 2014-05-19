package org.teo.acme1;

import org.teo.gui.Gui;
import org.teo.gui.command.CommandBase;

class Acme1Command extends CommandBase {

    public static final String ID = "acme1Command";

    public Acme1Command(Gui gui) {
        super(gui, ID, "ACME Command #1...");
    }

    @Override
    public void execute() {
        getGui().showInfo("ACME Command #1 called.");
    }
}
