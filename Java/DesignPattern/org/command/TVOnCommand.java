package org.command;

/**
 * @author mozixun
 * @description
 * @date 2020/4/1 - 11:49 下午
 */
public class TVOnCommand implements ICommand {

    TVReceiver tvReceiver = null;

    public TVOnCommand(TVReceiver tvReceiver) {
        this.tvReceiver = tvReceiver;
    }

    @Override
    public void execute() {
        this.tvReceiver.on();
    }

    @Override
    public void undo() {
        this.tvReceiver.off();
    }
}
