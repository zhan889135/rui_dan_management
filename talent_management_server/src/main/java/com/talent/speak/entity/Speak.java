package com.talent.speak.entity;

import lombok.Data;

/**
 * key:value实体类
 */
@Data
public class Speak {

    /** id */
    private String key;

    /** 显示 */
    private String name;

    /** 总数 */
    private String count;

}
