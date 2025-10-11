package com.talent.common.utils.exception.file;

import com.talent.common.utils.exception.base.BaseException;

/**
 * 文件信息异常类
 * 
 * @author JamesRay
 */
public class FileException extends BaseException
{
    private static final long serialVersionUID = 1L;

    public FileException(String code, Object[] args)
    {
        super("file", code, args, null);
    }

}
