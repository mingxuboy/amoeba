package com.meidusa.amoeba.route;

import java.util.Map;

import com.meidusa.amoeba.parser.dbobject.Table;

public interface TableRuleLoader {
	Map<Table, TableRule> loadRule();
	boolean needLoad();
}
