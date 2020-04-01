package org.command;

/**
 * @author mozixun
 * @description
 * @date 2020/4/1 - 11:50 下午
 */
public class TVOffCpmmand implements ICommand {

    TVReceiver tvReceiver = null;

    public TVOffCpmmand(TVReceiver tvReceiver) {
        this.tvReceiver = tvReceiver;
    }

    @Override
    public void execute() {
        this.tvReceiver.off();
    }

    @Override
    public void undo() {
        this.tvReceiver.on();
    }
}
