package org.teo.gui.command;

public interface CommandGroup extends Command {

    void addCommand(Command command);

    void removeCommand(Command command);

    void removeCommand(String id);

    Command getCommand(String id);

    Command[] getCommands();

    void addListener(Listener listener);

    void removeListener(Listener listener);

    public interface Listener {
        void commandAdded(CommandGroup commandGroup, Command command);

        void commandRemoved(CommandGroup commandGroup, Command command);
    }

}
