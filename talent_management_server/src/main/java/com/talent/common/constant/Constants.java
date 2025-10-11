package com.talent.common.constant;

/**
 * 通用常量信息
 *
 * @author JamesRay
 */
public class Constants {

    /** 令牌 */
    public static final String TOKEN = "token";

    /** 令牌前缀 */
    public static final String LOGIN_USER_KEY = "login_user_key";

    /** 令牌前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** UTF-8 字符集 */
    public static final String UTF8 = "UTF-8";

    /** GBK 字符集 */
    public static final String GBK = "GBK";

    /** 登录成功 */
    public static final String LOGIN_SUCCESS = "Success";

    /** 注销 */
    public static final String LOGOUT = "Logout";

    /** 注册 */
    public static final String REGISTER = "Register";

    /** 登录失败 */
    public static final String LOGIN_FAIL = "Error";

    /** 通用成功标识 */
    public static final String SUCCESS = "0";

    /** 通用失败标识 */
    public static final String FAIL = "1";

    /** admin */
    public static final String ADMIN = "admin";

    /** 所有权限标识 */
    public static final String ALL_PERMISSION = "*:*:*";

    /** www主域 */
    public static final String WWW = "www.";

    /** 资源映射路径 前缀 */
    public static String RESOURCE_PREFIX = "/profile";
    public static final String RESOURCE_PREFIX_PORD = "/talent/profile";

    /** http请求 */
    public static final String HTTP = "http://";

    /** https请求 */
    public static final String HTTPS = "https://";

    /** 下划线 */
    public static final String SEPARATOR = "_";

    /** 权限标识分隔符 */
    public static final String PERMISSION_DELIMETER = ",";

    /** 管理员角色权限标识 */
    public static final String SUPER_ADMIN = "admin";

    /** 角色权限分隔符 */
    public static final String ROLE_DELIMETER = ",";

    public static final int RET_CODE_0_NUM = 0;
    public static final int RET_CODE_1_NUM = 1;
    public static final int RET_CODE_2_NUM = 2;
    public static final int RET_CODE_3_NUM = 3;
    public static final int RET_CODE_4_NUM = 4;
    public static final int RET_CODE_5_NUM = 5;
    public static final String RET_CODE_0 = "0";
    public static final String RET_CODE_1 = "1";
    public static final String RET_CODE_2 = "2";
    public static final String RET_CODE_3 = "3";
    public static final String RET_CODE_4 = "4";
    public static final String RET_CODE_5 = "5";
    public static final String IS_DEL_Y = "Y";
    public static final String IS_DEL_N = "N";


    // ********************************************财务区域********************************************
    public static final String budget_type_hardware = "hardware"; // 硬件清单
    public static final String budget_type_material = "material"; // 辅材清单
    public static final String budget_type_install = "install"; // 安装调试成本
    public static final String budget_type_projectRemarks = "projectRemarks"; // 工程情况备注
    public static final String data_contract_qy = "fin_contract_qy"; // 区域
    public static final String data_contract_htkflx = "fin_contract_htkflx"; // 合同开发类型
    public static final String data_budget_contract_type = "fin_budget_contract_type"; // 合同类型
    public static final String data_budget_model_type = "fin_budget_model_type"; // 清单设备类型
    public static final String data_contract_qdpt = "fin_contract_qdpt"; // 签订平台
    public static final String fin_delete_file_path = "/upload/FinFile/FinBudgetContractDELETE"; // 附件删除路径



    // ********************************************研发区域********************************************
    public static final String rd_file_project_id = "projectId"; // 附件 - 主表ID字段名
    public static final String rd_file_type_project = "project"; // 附件 - 项目管理
    public static final String rd_file_type_work_order = "work_order"; // 附件 - 任务管理
    public static final String rd_file_type_work_info = "work_info"; // 附件 - 研发支援/出差
    public static final String rd_delete_file_path = "/upload/RdFile/ProjectDocumentationDELETE"; // 附件删除路径
    public static final String rd_weekly_report_type = "report"; // 周报标识
    public static final String rd_weekly_plan_type = "plan"; // 计划标识
    public static final String rd_weekly_type_project = "project"; // 周报类型 - 项目
    public static final String rd_weekly_type_support = "support"; // 周报类型 - 支援
    public static final String rd_weekly_type_trip = "trip"; // 周报类型 - 出差
    public static final String rd_weekly_type_order = "order"; // 周报类型 - 任务

    // ********************************************工程区域********************************************
    public static final String prd_contract_id = "contractId"; // 主表ID字段名
//    public static final String prd_contract_type_preSales = "preSales"; // 售前
//    public static final String prd_contract_type_produce = "produce"; // 生产
//    public static final String prd_contract_type_maintenance = "maintenance"; // 投运
//    public static final String prd_contract_type_commissioning = "commissioning"; //维护
    public static final String prd_contract_type_logistics = "logistics"; // 附件 - 物流管理
    public static final String prd_contract_delete_file_path = "/upload/PrdFile/PrdContractDELETE"; // 附件删除路径
    public static final String prd_production_type = "prd_production_type"; // 工程种类
    public static final String prd_yes_no = "prd_yes_no"; // 有无
    public static final String prd_works_category = "prd_works_category"; // 工程类别
    public static final String prd_production_state = "prd_production_state"; // 工程状态

}
