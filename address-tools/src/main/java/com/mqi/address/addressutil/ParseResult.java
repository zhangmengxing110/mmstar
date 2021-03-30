package com.mqi.address.addressutil;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.io.FileUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;


public class ParseResult {



    /**
     * area : 盐田区
     * city : 深圳市
     * detail : 山海四季城F栋2f
     * name : 太阳鲜鲜
     * phone : 13111111111
     * postalCode :
     * province : 广东省
     */

    @JsonProperty("area")
    private String area;
    @JsonProperty("city")
    private String city;
    @JsonProperty("detail")
    private String detail;
    @JsonProperty("name")
    private String name;
    @JsonProperty("phone")
    private String phone;
    @JsonProperty("postalCode")
    private String postalCode;
    @JsonProperty("province")
    private String province;
    @JsonProperty("provinceId")
    private Long provinceId;
    @JsonProperty("cityId")
    private Long cityId;
    @JsonProperty("areaId")
    private Long areaId;

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public Long getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Long provinceId) {
        this.provinceId = provinceId;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParseResult)) return false;
        ParseResult that = (ParseResult) o;
        return Objects.equals(area, that.area) &&
                Objects.equals(city, that.city) &&
                Objects.equals(detail, that.detail) &&
                Objects.equals(name, that.name) &&
                Objects.equals(phone, that.phone) &&
                Objects.equals(postalCode, that.postalCode) &&
                Objects.equals(province, that.province) &&
                Objects.equals(provinceId, that.provinceId) &&
                Objects.equals(cityId, that.cityId) &&
                Objects.equals(areaId, that.areaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(area, city, detail, name, phone, postalCode, province, provinceId, cityId, areaId);
    }
}
