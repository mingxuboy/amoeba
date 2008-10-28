/**
 * <pre>
 * Copyright 2004-2008 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com (&quot;Confidential
 * Information&quot;). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 * </pre>
 */
package com.meidusa.amoeba.oracle.net.packet.assist;

import com.meidusa.amoeba.oracle.net.packet.T4CPacketBuffer;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIMsgPacket;

/**
 * @author hexianmao
 * @version 2008-8-20 ÏÂÎç02:20:26
 */
public class T4CTTIfob {

    public T4CTTIfob(){
        // super(TTIFOB);
    }

    public void marshal(T4CPacketBuffer meg) {
        meg.marshalUB1(T4CTTIMsgPacket.TTIFOB);
    }

}
