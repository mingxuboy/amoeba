package com.meidusa.amoeba.aladdin.parser;

import java.io.StringReader;

import com.meidusa.amoeba.aladdin.parser.sql.AladdinParser;
import com.meidusa.amoeba.mysql.parser.MysqlQueryRouter;
import com.meidusa.amoeba.parser.Parser;

/**
 * @author struct
 */
public class AladdinQueryRouter extends MysqlQueryRouter {

    @Override
    public Parser newParser(String sql) {
        return new AladdinParser(new StringReader(sql));
    }

}
