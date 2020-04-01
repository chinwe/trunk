package org.command;

/**
 * @author mozixun
 * @description
 * @date 2020/4/1 - 11:18 下午
 */
public class LightOffCommand implements ICommand {

    private LightReceiver lightReceiver = null;

    public LightOffCommand(LightReceiver lightReceiver) {
        this.lightReceiver = lightReceiver;
    }

    @Override
    public void execute() {
        this.lightReceiver.off();
    }

    @Override
    public void undo() {
        this.lightReceiver.on();
    }
}
