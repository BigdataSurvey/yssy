package com.zywl.app.defaultx.enmus;

/**
 *   提现类型
 * 
 * @author DOE
 *
 */
public enum CashOrderErrorMessageEnum {

	NOT_FOUND("NOT_FOUND","记录不存在"),
	
	NAME_NOT_CORRECT("NAME_NOT_CORRECT","微信实名与游戏实名不一致"),
	
	ACCOUNT_FROZEN("ACCOUNT_FROZEN","该账户被冻结"),
	
	REAL_NAME_CHECK_FAIL("REAL_NAME_CHECK_FAIL","微信未实名认证"),
	
	OPENID_INVALID("OPENID_INVALID","微信信息有误"),
	
	TRANSFER_QUOTA_EXCEED("TRANSFER_QUOTA_EXCEED","超过用户单笔收款额度"),
	
	DAY_RECEIVED_QUOTA_EXCEED("DAY_RECEIVED_QUOTA_EXCEED","超过用户单日收款额度"),

	MONTH_RECEIVED_QUOTA_EXCEED("MONTH_RECEIVED_QUOTA_EXCEED", "超过用户单月收款额度"), 
	
	DAY_RECEIVED_COUNT_EXCEED("DAY_RECEIVED_COUNT_EXCEED", "超过用户单日收款次数"),
	
	//未开通该权限或权限被冻结，请核实产品权限状态
	PRODUCT_AUTH_CHECK_FAIL("PRODUCT_AUTH_CHECK_FAIL","该笔转账可能存在风险，已被微信拦截，errorCode:005"),
	
	OVERDUE_CLOSE("OVERDUE_CLOSE","超过系统重试期，系统自动关闭"),
	
	ID_CARD_NOT_CORRECT("ID_CARD_NOT_CORRECT","收款人身份证校验不通过，请核实信息"),
	
	ACCOUNT_NOT_EXIST("ACCOUNT_NOT_EXIST","该用户账户不存在"),
	
	TRANSFER_RISK("TRANSFER_RISK","该笔转账可能存在风险，已被微信拦截"),
	
	OTHER_FAIL_REASON_TYPE("OTHER_FAIL_REASON_TYPE","其它失败原因"),
	
	REALNAME_ACCOUNT_RECEIVED_QUOTA_EXCEED("REALNAME_ACCOUNT_RECEIVED_QUOTA_EXCEED","用户账户收款受限，请在微信支付查看详情"),
	
	RECEIVE_ACCOUNT_NOT_PERMMIT("RECEIVE_ACCOUNT_NOT_PERMMIT","请联系客服"),
	
	PAYEE_ACCOUNT_ABNORMAL("PAYEE_ACCOUNT_ABNORMAL","用户账户收款异常，请完善在微信支付的身份信息以继续收款"),
	
	//商户账户付款受限，请联系客服
	PAYER_ACCOUNT_ABNORMAL("PAYER_ACCOUNT_ABNORMAL","该笔转账可能存在风险，已被微信拦截,errorCode:001"),
	
	//该转账场景暂不可用，请联系客服，errorCode:1
	TRANSFER_SCENE_UNAVAILABLE("TRANSFER_SCENE_UNAVAILABLE","该笔转账可能存在风险，已被微信拦截，errorCode:002"),
	
	//该转账场景暂不可用，请联系客服，errorCode:2
	TRANSFER_SCENE_INVALID("TRANSFER_SCENE_INVALID","该笔转账可能存在风险，已被微信拦截，errorCode:003"),
	
	TRANSFER_REMARK_SET_FAIL("TRANSFER_REMARK_SET_FAIL","转账备注设置失败， 请调整后重新再试"),
	
	//该转账场景暂不可用，请联系客服，errorCode:3
	RECEIVE_ACCOUNT_NOT_CONFIGURE("RECEIVE_ACCOUNT_NOT_CONFIGURE","该笔转账可能存在风险，已被微信拦截，errorCode:004"),
	
	BLOCK_B2C_USERLIMITAMOUNT_BSRULE_MONTH("BLOCK_B2C_USERLIMITAMOUNT_BSRULE_MONTH","超出用户单月转账收款限额，本月不支持继续提现"),
	
	BLOCK_B2C_USERLIMITAMOUNT_MONTH("BLOCK_B2C_USERLIMITAMOUNT_MONTH","用户账户存在风险收款受限，本月不支持继续提现"),
	
	MERCHANT_REJECT("MERCHANT_REJECT","账号存在风险，提现已驳回"),
	
	MERCHANT_NOT_CONFIRM("MERCHANT_NOT_CONFIRM","审核超时，请重新提交"),
	
	UNKNOW_ERROR("UNKNOW_ERROR","未知异常");
	
	private String name;

	private String value;
	
	private CashOrderErrorMessageEnum(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValie(String value) {
		this.value = value;
	}
	
	public static CashOrderErrorMessageEnum getOrderStatusEnum(String failCode) {
        for (CashOrderErrorMessageEnum cashFailMessageEnum: CashOrderErrorMessageEnum.values()) {
            if (cashFailMessageEnum.getName().equals(failCode)) {
                return cashFailMessageEnum;
            }
        }
        return CashOrderErrorMessageEnum.UNKNOW_ERROR;
    }
	
	
	
	
}
