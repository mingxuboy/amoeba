package com.meidusa.amoeba.manager.landscape.lifecycle;

import java.util.Date;

/**
 * User: Sun Ning <Classicning@gmail.com>
 * <p/>
 * Date: 1/11/11
 * Time: 8:52 PM
 */
public class LifeCycleStateInstallFailed implements LifeCycleState {

    private String reason;

    private Date failedDate;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String getStatusName() {
        return "INSTALL_FAILED";
    }

    public Date getFailedDate() {
        return failedDate;
    }

    public void setFailedDate(Date failedDate) {
        this.failedDate = failedDate;
    }
}
