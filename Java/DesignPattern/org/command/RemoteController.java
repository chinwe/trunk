package org.command;

/**
 * @author mozixun
 * @description
 * @date 2020/4/1 - 11:20 下午
 */
public class RemoteController {

    ICommand[] onCommands;
    ICommand[] offCommands;

    ICommand undoCommand;

    public final int MAXCOMMAND = 5;

    public RemoteController() {
        onCommands = new ICommand[MAXCOMMAND];
        offCommands = new ICommand[MAXCOMMAND];

        for (int i = 0; i < MAXCOMMAND; i++) {
            onCommands[i] = new NoCommand();
            offCommands[i] = new NoCommand();
        }
    }

    public void setCommand(int no, ICommand onCommand, ICommand offCommand) {
        onCommands[no] = onCommand;
        offCommands[no] = offCommand;
    }

    public void onButtonPresed(int no) {
        onCommands[no].execute();

        undoCommand = onCommands[no];
    }

    public void offButtonPresed(int no) {
        offCommands[no].execute();

        undoCommand = offCommands[no];
    }

    public void undoButtonPresed() {
        undoCommand.undo();
    }
}
