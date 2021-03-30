package com.mqi.address.addressutil;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * 省级JSON实体类
 * @author zmx
 */
public class ProvinceJson {

    /**
     * pId : 11
     * pName : 北京市
     */

    @JsonProperty("pId")
    private String pId;
    @JsonProperty("pName")
    private String pName;

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getpName() {
        return pName;
    }

    public void setpName(String pName) {
        this.pName = pName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProvinceJson that = (ProvinceJson) o;
        return pId.equals(that.pId) &&
                pName.equals(that.pName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pId, pName);
    }
}
