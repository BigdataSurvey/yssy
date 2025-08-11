package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

public class PayBean extends BaseBean {

    private String merchantId;

    private String managerId;

    private String merchantType;

    private String businessImg;
    private String identityImgz;
    private String identityImgf;
    private String telephone;
    private String email;
    private String countryAbbr2;
    private String bankCardNo;
    private String bankName;
    private String bankAccountType;
    private String accountName;
    private String bankBranchName;
    private String bankBindMobile;
    private String provinceCode;
    private String cityCode;
    private String countyCode;
    private String bankCardImg;
    private String merchantCategory;
    private String industryTypeCode;
    private String merAddr;
    private String settlementPeriod;
    private String settlementMode;
    private String settleMode;
    private String idType;
    private String entryImg;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getMerchantType() {
        return merchantType;
    }

    public void setMerchantType(String merchantType) {
        this.merchantType = merchantType;
    }

    public String getBusinessImg() {
        return businessImg;
    }

    public void setBusinessImg(String businessImg) {
        this.businessImg = businessImg;
    }

    public String getIdentityImgz() {
        return identityImgz;
    }

    public void setIdentityImgz(String identityImgz) {
        this.identityImgz = identityImgz;
    }

    public String getIdentityImgf() {
        return identityImgf;
    }

    public void setIdentityImgf(String identityImgf) {
        this.identityImgf = identityImgf;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountryAbbr2() {
        return countryAbbr2;
    }

    public void setCountryAbbr2(String countryAbbr2) {
        this.countryAbbr2 = countryAbbr2;
    }

    public String getBankCardNo() {
        return bankCardNo;
    }

    public void setBankCardNo(String bankCardNo) {
        this.bankCardNo = bankCardNo;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccountType() {
        return bankAccountType;
    }

    public void setBankAccountType(String bankAccountType) {
        this.bankAccountType = bankAccountType;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getBankBranchName() {
        return bankBranchName;
    }

    public void setBankBranchName(String bankBranchName) {
        this.bankBranchName = bankBranchName;
    }

    public String getBankBindMobile() {
        return bankBindMobile;
    }

    public void setBankBindMobile(String bankBindMobile) {
        this.bankBindMobile = bankBindMobile;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCountyCode() {
        return countyCode;
    }

    public void setCountyCode(String countyCode) {
        this.countyCode = countyCode;
    }

    public String getBankCardImg() {
        return bankCardImg;
    }

    public void setBankCardImg(String bankCardImg) {
        this.bankCardImg = bankCardImg;
    }

    public String getMerchantCategory() {
        return merchantCategory;
    }

    public void setMerchantCategory(String merchantCategory) {
        this.merchantCategory = merchantCategory;
    }

    public String getIndustryTypeCode() {
        return industryTypeCode;
    }

    public void setIndustryTypeCode(String industryTypeCode) {
        this.industryTypeCode = industryTypeCode;
    }

    public String getMerAddr() {
        return merAddr;
    }

    public void setMerAddr(String merAddr) {
        this.merAddr = merAddr;
    }

    public String getSettlementPeriod() {
        return settlementPeriod;
    }

    public void setSettlementPeriod(String settlementPeriod) {
        this.settlementPeriod = settlementPeriod;
    }

    public String getSettlementMode() {
        return settlementMode;
    }

    public void setSettlementMode(String settlementMode) {
        this.settlementMode = settlementMode;
    }

    public String getSettleMode() {
        return settleMode;
    }

    public void setSettleMode(String settleMode) {
        this.settleMode = settleMode;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getEntryImg() {
        return entryImg;
    }

    public void setEntryImg(String entryImg) {
        this.entryImg = entryImg;
    }

    public PayBean() {
    }

    public PayBean(String merchantId, String managerId, String merchantType, String businessImg, String identityImgz, String identityImgf, String telephone, String email, String countryAbbr2, String bankCardNo, String bankName, String bankAccountType, String accountName, String bankBranchName, String bankBindMobile, String provinceCode, String cityCode, String countyCode, String bankCardImg, String merchantCategory, String industryTypeCode, String merAddr, String settlementPeriod, String settlementMode, String settleMode, String idType, String entryImg) {
        this.merchantId = merchantId;
        this.managerId = managerId;
        this.merchantType = merchantType;
        this.businessImg = businessImg;
        this.identityImgz = identityImgz;
        this.identityImgf = identityImgf;
        this.telephone = telephone;
        this.email = email;
        this.countryAbbr2 = countryAbbr2;
        this.bankCardNo = bankCardNo;
        this.bankName = bankName;
        this.bankAccountType = bankAccountType;
        this.accountName = accountName;
        this.bankBranchName = bankBranchName;
        this.bankBindMobile = bankBindMobile;
        this.provinceCode = provinceCode;
        this.cityCode = cityCode;
        this.countyCode = countyCode;
        this.bankCardImg = bankCardImg;
        this.merchantCategory = merchantCategory;
        this.industryTypeCode = industryTypeCode;
        this.merAddr = merAddr;
        this.settlementPeriod = settlementPeriod;
        this.settlementMode = settlementMode;
        this.settleMode = settleMode;
        this.idType = idType;
        this.entryImg = entryImg;
    }
}
