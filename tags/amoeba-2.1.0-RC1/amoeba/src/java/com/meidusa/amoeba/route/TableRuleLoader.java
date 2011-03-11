package com.meidusa.amoeba.route;

import java.util.Map;

import com.meidusa.amoeba.parser.dbobject.Table;

public interface TableRuleLoader {
	String _CURRENT_QUERY_OBJECT_ = "_CURRENT_STATEMENT_";
	Map<Table, TableRule> loadRule();
	boolean needLoad();
}
