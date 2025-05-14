package com.zywl.app.base.bean.vo;

import com.zywl.app.base.BaseBean;

public class TempVo extends BaseBean {

    private Long id;

    private Long parentId;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
