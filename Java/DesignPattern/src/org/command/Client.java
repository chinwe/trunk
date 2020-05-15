package org.command;

/**
 * @author mozixun
 * @description
 * @date 2020/4/1 - 11:20 下午
 */
public class Client {
    public static void main(String[] args) {
        RemoteController remoteController = new RemoteController();

        LightReceiver lightReceiver = new LightReceiver();
        ICommand lightOnCommand = new LightOnCommand(lightReceiver);
        ICommand lightOffCommand = new LightOffCommand(lightReceiver);
        remoteController.setCommand(0, lightOnCommand, lightOffCommand);
        remoteController.offButtonPresed(0);
        remoteController.onButtonPresed(0);
        remoteController.undoButtonPresed();

        TVReceiver tvReceiver = new TVReceiver();
        remoteController.setCommand(1, new TVOnCommand(tvReceiver), new TVOffCpmmand(tvReceiver));
        remoteController.onButtonPresed(1);
        remoteController.offButtonPresed(1);
        remoteController.undoButtonPresed();

    }
}
