package org.command;

/**
 * @author mozixun
 * @description
 * @date 2020/4/1 - 11:15 下午
 */
public interface ICommand {

    /**
     * 执行命令
     */
    void execute();

    /**
     * 撤销命令
     */
    void undo();
}
