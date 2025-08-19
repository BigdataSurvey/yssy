package com.zywl.app.base.bean.shoop;

import com.zywl.app.base.BaseBean;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;


/**
 * RShopManager 实体类
 */
@Data
public class ShopManager extends BaseBean {



    private Long id;
    /**
     *
     */
    private Long userId;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 头像
     */
    private String headImageUrl;

    private String userNo;

    /**
     * 微信
     */
    private String wechat;

    /**
     * qq号
     */
    private String qq;

    /**
     * 用户地址
     */
    private String userAddress;

    /**
     * 1启用 0禁用 2申请
     */
    private Integer status;





}
