package com.meidusa.amoeba.oracle.context;

/**
 * <pre>
 * Oracle数据库认证会话过程
 * 1.&gt;&gt;发送connect数据包
 * 2.&lt;&lt;返回resend数据包
 * 3.&gt;&gt;发送connect数据包
 * 4.&lt;&lt;返回accept数据包
 * 5.&gt;&gt;发送Ano数据包
 * 6.&lt;&lt;返回Ano数据包
 * 7.&gt;&gt;发送协议版本数据包
 * 8.&lt;&lt;返回协议验证数据包
 * 9.&gt;&gt;发送数据类型数据包
 * 10.&lt;&lt;返回数据类型验证数据包
 * 11.&gt;&gt;发送数据库版本数据包
 * 12.&lt;&lt;返回数据库版本验证数据包
 * 13.&gt;&gt;发送请求加密key数据包
 * 14.&lt;&lt;返回加密key数据包
 * 15.&gt;&gt;发送用户和密码加密信息数据包
 * 16.&lt;&lt;返回用户和密码验证结果数据包
 * </pre>
 * 
 * @author hexianmao
 * @version 2008-8-11 下午01:45:37
 */
public class OracleAuthSession {

}
