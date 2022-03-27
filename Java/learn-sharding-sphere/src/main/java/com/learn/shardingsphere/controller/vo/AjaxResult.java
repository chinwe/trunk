package com.learn.shardingsphere.controller.vo;

import lombok.Data;

/**
 * @author chinwe
 * 2022/3/19
 */
@Data
public class AjaxResult {
    private int code;
    private String msg;
    private Object data;
}
