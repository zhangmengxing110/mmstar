package com.mqi.address.addressutil;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 市级JSON实体类
 * @author zmx
 */
public class CityJson {

    /**
     * cId : 1101
     * cName : 市辖区
     * pId : 11
     */

    @JsonProperty("cId")
    private String cId;
    @JsonProperty("cName")
    private String cName;
    @JsonProperty("pId")
    private String pId;

    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }

    public String getcName() {
        return cName;
    }

    public void setcName(String cName) {
        this.cName = cName;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }
}
