package com.learn.springboot.controller.vo;

import lombok.*;

/**
 * @author chinwe
 * 2022/8/27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommonResponse {

    private String code;

    private String msg;

    private Object data;
}
