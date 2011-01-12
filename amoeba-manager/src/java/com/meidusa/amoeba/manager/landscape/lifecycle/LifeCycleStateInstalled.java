package com.meidusa.amoeba.manager.landscape.lifecycle;

import java.util.Date;

/**
 * Amoeba software is installed on this server. All tests passed.
 * <p/>
 * User: Sun Ning <Classicning@gmail.com>
 * Date: 1/11/11
 * Time: 8:15 PM
 */
public class LifeCycleStateInstalled implements LifeCycleState {

    private String softwareVersion;

    private String amoebaHome;

    private String username;

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public String getAmoebaHome() {
        return amoebaHome;
    }

    public void setAmoebaHome(String amoebaHome) {
        this.amoebaHome = amoebaHome;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getInstalledDate() {
        return installedDate;
    }

    public void setInstalledDate(Date installedDate) {
        this.installedDate = installedDate;
    }

    private Date installedDate;

    @Override
    public String getStatusName() {
        return "INSTALLED";
    }
}
