package com.talent.common.utils.exception.user;

import com.talent.common.utils.exception.base.BaseException;

/**
 * 用户信息异常类
 * 
 * @author JamesRay
 */
public class UserException extends BaseException
{
    private static final long serialVersionUID = 1L;

    public UserException(String code, Object[] args)
    {
        super("user", code, args, null);
    }
}
