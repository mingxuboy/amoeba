/*
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * 	You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.meidusa.amoeba.config;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class DocumentUtil {

	/**
	 * 得到唯一的一个节点，多于一个节点将抛出 ConfigurationException 异常。
	 * 没有节点的时候返回null
	 * 
	 * @param current
	 * @param tagName
	 * @return
	 */
	public static Element getTheOnlyElement(Element current, String tagName){
		NodeList nodeList = current.getElementsByTagName(tagName);
		if(nodeList.getLength() >1){
			throw new ConfigurationException(tagName +" elements length  over one!");
		}
		
		if(nodeList.getLength() ==1){
			return (Element)nodeList.item(0);
		}else{
			return null;
		}
	}
	
	public static BeanObjectEntityConfig loadBeanConfig(Element current) {
		if(current == null){
			return null;
		}
		BeanObjectEntityConfig beanConfig = new BeanObjectEntityConfig();
		NodeList children = current.getChildNodes();
        int childSize = children.getLength();
        beanConfig.setName(current.getAttribute("name"));
        Element element = DocumentUtil.getTheOnlyElement(current,"className");
        if(element != null){
        	beanConfig.setClassName(element.getTextContent());
        }
        Map<String,String> map = new HashMap<String,String>();
        for (int i = 0; i < childSize; i++) {
            Node childNode = children.item(i);
            if (childNode instanceof Element) {
                Element child = (Element) childNode;
                final String nodeName = child.getNodeName();
	            if (nodeName.equals("property")) {
	            	String key = child.getAttribute("name");
	            	String value = child.getTextContent();
	            	map.put(key, value);
	            }
            }
        }
        beanConfig.setParams(map);
		return beanConfig;
	}
}
