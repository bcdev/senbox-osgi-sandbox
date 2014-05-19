package org.teo.gui.command;

import org.teo.gui.Gui;

import java.util.ArrayList;
import java.util.HashMap;


public class CommandGroupImpl extends CommandBase implements CommandGroup {

    HashMap<String, Command> commandMap = new HashMap<>();
    ArrayList<Command> commandList = new ArrayList<>();
    ArrayList<CommandGroup.Listener> listeners = new ArrayList<CommandGroup.Listener>();

    public CommandGroupImpl(Gui gui, String id, String name) {
        super(gui, id, name);
    }

    @Override
    public void execute() {
    }

    @Override
    public void updateState() {
        for (Command command : commandList) {
            command.updateState();
        }
    }

    @Override
    public Command getCommand(String id) {
        return commandMap.get(id);
    }

    @Override
    public Command[] getCommands() {
        return commandList.toArray(new Command[commandList.size()]);
    }

    @Override
    public void addCommand(Command command) {
        Command oldCommand = commandMap.put(command.getId(), command);
        if (oldCommand != null) {
            commandList.remove(oldCommand);
        }
        commandList.add(command);

        for (Listener listener : listeners) {
            listener.commandAdded(this, command);
        }
    }

    @Override
    public void removeCommand(Command command) {
        commandMap.remove(command.getId());
        commandList.remove(command);

        for (Listener listener : listeners) {
            listener.commandRemoved(this, command);
        }
    }

    @Override
    public void removeCommand(String id) {
        Command command = getCommand(id);
        if (command != null) {
            removeCommand(command);
        }
    }

    @Override
    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }
}