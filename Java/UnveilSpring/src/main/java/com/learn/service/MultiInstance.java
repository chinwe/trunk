package com.learn.service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * 多实例 @Scope("prototype")
 * 单实例 @Scope("singleton")
 *
 * @author chinwe
 * 2021/12/21
 */
@Service
@Scope("prototype")
public class MultiInstance {

}
