package org.command;

/**
 * @author mozixun
 * @description
 * @date 2020/4/1 - 11:15 下午
 */
public class LightOnCommand implements ICommand {

    private LightReceiver lightReceiver = null;

    public LightOnCommand(LightReceiver lightReceiver) {
        this.lightReceiver = lightReceiver;
    }

    @Override
    public void execute() {
        this.lightReceiver.on();
    }

    @Override
    public void undo() {
        this.lightReceiver.off();
    }
}
