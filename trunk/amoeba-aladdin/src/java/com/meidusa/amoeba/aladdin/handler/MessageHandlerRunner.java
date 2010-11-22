package com.meidusa.amoeba.aladdin.handler;

import com.meidusa.amoeba.net.MessageHandler;

public interface MessageHandlerRunner extends Runnable, Cloneable {

    /**
     * 初始化内部一些变量
     */
    public void init(MessageHandler handler);

    /**
     * 清理与初始化的handler相关的东西
     */
    public void reset();

}
