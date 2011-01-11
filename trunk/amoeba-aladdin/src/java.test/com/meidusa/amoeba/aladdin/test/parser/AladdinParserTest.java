package com.meidusa.amoeba.aladdin.test.parser;

import java.io.StringReader;
import java.util.Map;

import com.meidusa.amoeba.aladdin.parser.sql.AladdinParser;
import com.meidusa.amoeba.parser.Parser;
import com.meidusa.amoeba.parser.expression.Expression;
import com.meidusa.amoeba.parser.function.Function;
import com.meidusa.amoeba.parser.statement.DMLStatement;
import com.meidusa.amoeba.parser.statement.PropertyStatement;
import com.meidusa.amoeba.parser.statement.Statement;
import com.meidusa.amoeba.route.AbstractQueryRouter;

public class AladdinParserTest {

    static String                file   = "./build/build-aladdin/conf/functionMap.xml";                 // TODO 测试文件，修改为本地路径的文件。
    static Map<String, Function> funMap = AbstractQueryRouter.loadFunctionMap(file);

    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("select * from aaa where id = 12 AND (upper(subject) like upper(?) OR upper(keywords) like upper(?))");


        String sql = sb.toString();
        Parser parser = new AladdinParser(new StringReader(sql));
        parser.setFunctionMap(funMap);

        try {
            Statement statment = parser.doParse();
            if (statment instanceof DMLStatement) {
                DMLStatement dmlStatment = (DMLStatement) statment;
                Expression expression = dmlStatment.getExpression();

                System.out.println(sql + " =[ " + expression + "], evaluated = {" + dmlStatment.evaluate(null) + "}");
            } else if (statment instanceof PropertyStatement) {
                PropertyStatement proStatment = (PropertyStatement) statment;

                System.out.println(proStatment.getProperties());
            }
        } catch (com.meidusa.amoeba.parser.ParseException e) {
            e.printStackTrace();
        }
    }

}
