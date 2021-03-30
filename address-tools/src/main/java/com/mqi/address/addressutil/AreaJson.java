package com.mqi.address.addressutil;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 县级JSON实体类
 * @author zmx
 */

public class AreaJson {


    /**
     * aId : 110101
     * aName : 东城区
     * cId : 1101
     * pId : 11
     */

    @JsonProperty("aId")
    private String aId;
    @JsonProperty("aName")
    private String aName;
    @JsonProperty("cId")
    private String cId;
    @JsonProperty("pId")
    private String pId;


    public String getaId() {
        return aId;
    }

    public void setaId(String aId) {
        this.aId = aId;
    }

    public String getaName() {
        return aName;
    }

    public void setaName(String aName) {
        this.aName = aName;
    }

    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }
}
