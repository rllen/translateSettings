package link.zhidou.translator.utils;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Created by keetom on 2017/11/21.
 */

public class UtilXml {
    public static String analysis(String v_strXML) {
        Document doc = null;
        try {
            doc = DocumentHelper.parseText(v_strXML);
        } catch (DocumentException e2) {
            // TODO 自动生成 catch 块
            e2.printStackTrace();
        }
        Element root = doc.getRootElement();// 指向根节点
        return root.getData().toString();
    }
}
