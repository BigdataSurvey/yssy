package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;

public class ChannelIncome extends BaseBean {

    private int tier;

    private BigDecimal a1;

    private BigDecimal a2;

    private BigDecimal a3;

    private BigDecimal a4;

    private BigDecimal a5;

    private BigDecimal a6;

    private BigDecimal a7;

    private BigDecimal a8;

    private BigDecimal a9;

    private BigDecimal a10;

    private BigDecimal a11;

    private BigDecimal a12;

    private BigDecimal a13;

    private BigDecimal a14;

    private BigDecimal a15;


    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public BigDecimal getA1() {
        return a1;
    }

    public void setA1(BigDecimal a1) {
        this.a1 = a1;
    }

    public BigDecimal getA2() {
        return a2;
    }

    public void setA2(BigDecimal a2) {
        this.a2 = a2;
    }

    public BigDecimal getA3() {
        return a3;
    }

    public void setA3(BigDecimal a3) {
        this.a3 = a3;
    }

    public BigDecimal getA4() {
        return a4;
    }

    public void setA4(BigDecimal a4) {
        this.a4 = a4;
    }

    public BigDecimal getA5() {
        return a5;
    }

    public void setA5(BigDecimal a5) {
        this.a5 = a5;
    }

    public BigDecimal getA6() {
        return a6;
    }

    public void setA6(BigDecimal a6) {
        this.a6 = a6;
    }

    public BigDecimal getA7() {
        return a7;
    }

    public void setA7(BigDecimal a7) {
        this.a7 = a7;
    }

    public BigDecimal getA8() {
        return a8;
    }

    public void setA8(BigDecimal a8) {
        this.a8 = a8;
    }

    public BigDecimal getA9() {
        return a9;
    }

    public void setA9(BigDecimal a9) {
        this.a9 = a9;
    }

    public BigDecimal getA10() {
        return a10;
    }

    public void setA10(BigDecimal a10) {
        this.a10 = a10;
    }

    public BigDecimal getA11() {
        return a11;
    }

    public void setA11(BigDecimal a11) {
        this.a11 = a11;
    }

    public BigDecimal getA12() {
        return a12;
    }

    public void setA12(BigDecimal a12) {
        this.a12 = a12;
    }

    public BigDecimal getA13() {
        return a13;
    }

    public void setA13(BigDecimal a13) {
        this.a13 = a13;
    }

    public BigDecimal getA14() {
        return a14;
    }

    public void setA14(BigDecimal a14) {
        this.a14 = a14;
    }

    public BigDecimal getA15() {
        return a15;
    }

    public void setA15(BigDecimal a15) {
        this.a15 = a15;
    }

    public BigDecimal getIncomeByNum(int num){
        BigDecimal income ;
        if (num==1){
            income = getA1();
        } else if (num==2) {
            income=getA2();
        }else if (num==3) {
            income=getA3();
        }else if (num==4) {
            income=getA4();
        }else if (num==5) {
            income=getA5();
        }else if (num==6) {
            income=getA6();
        }else if (num==7) {
            income=getA7();
        }else if (num==8) {
            income=getA8();
        }else if (num==9) {
            income=getA9();
        }else if (num==10) {
            income=getA10();
        }else if (num==11) {
            income=getA11();
        }else if (num==12) {
            income=getA12();
        }else if (num==13) {
            income=getA13();
        }else if (num==14) {
            income=getA14();
        }else {
            income=getA15();
        }
        return income;
    }
}
