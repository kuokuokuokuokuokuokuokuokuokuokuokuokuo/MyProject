package com.ikats.scheduler.entity.enumerate;

/**
 * @Author : liu kuo
 * @Date : 2017/12/5 10:30.
 * @Description : Indulge in study , wasting away
 */
public enum GYStatus
{
    INIT("0"),
    SEND_OK("1"),
    SEND_ERROR("2"),
    SEND_MDBZ("3");

    private String value;

    private GYStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
