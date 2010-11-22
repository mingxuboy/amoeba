package com.meidusa.amoeba.mysql.test.route;

import com.meidusa.amoeba.config.ConfigUtil;
import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.parser.ParseException;
import com.meidusa.amoeba.route.QueryRouter;
import com.meidusa.amoeba.route.SqlQueryObject;
import com.meidusa.amoeba.util.InitialisationException;

public class TestQueryRouter {
	@SuppressWarnings("unchecked")
	public static void main(String [] args) throws ParseException, InitialisationException{
		ProxyRuntimeContext context = new ProxyRuntimeContext();
		ProxyRuntimeContext.setInstance(context);
		context.init(ConfigUtil.filter("${amoeba.home}/conf/amoeba.xml"));
		QueryRouter router = ProxyRuntimeContext.getInstance().getQueryRouter();
		SqlQueryObject query = new SqlQueryObject();
		query.sql = "update UDB_PROFILE.USER_PROFILE_BASIC set aa='qwerqwer' where sdid=198231792";
		router.doRoute(null, query);
	}
}
