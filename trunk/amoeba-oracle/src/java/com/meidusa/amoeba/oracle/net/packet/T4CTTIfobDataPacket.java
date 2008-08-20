/**
 * <pre>
 * Copyright 2004-2008 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com (&quot;Confidential
 * Information&quot;). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 * </pre>
 */
package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-8-20 ÏÂÎç02:20:26
 */
public class T4CTTIfobDataPacket extends T4CTTIMsgPacket {

    public T4CTTIfobDataPacket(){
        this.msgCode = TTIFOB;
    }

    @Override
    protected void init(AbstractPacketBuffer buffer) {
        // TODO Auto-generated method stub
        super.init(buffer);
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        // TODO Auto-generated method stub
        super.write2Buffer(buffer);
    }

}
