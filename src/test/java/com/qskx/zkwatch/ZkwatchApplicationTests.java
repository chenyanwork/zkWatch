package com.qskx.zkwatch;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ZkwatchApplicationTests {

	@Test
	public void contextLoads() {
		new MethodWrite().TestMethodInvoke();
	}

}
 class MyBean {
	private String id = null;
	private String userName = null;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}

}

 class MethodWrite {

	private MyBean beanObj = null;

	private BeanInfo bBeanObjInfo = null;

	public void TestMethodInvoke() {
		Map userMap = new HashMap();
		userMap.put("id", "1001");
		userMap.put("userName", "isoftstone");

		try {
			//实例化一个Bean
			beanObj = new MyBean();
			//依据Bean产生一个相关的BeanInfo类
			bBeanObjInfo = Introspector.getBeanInfo(beanObj.getClass());

			PropertyDescriptor[] propertyDesc = bBeanObjInfo.getPropertyDescriptors();
			for (int i = 0; i < propertyDesc.length; i++) {
				if (propertyDesc[i].getName().compareToIgnoreCase("class") == 0) continue;
				//System.out.print(propertyDesc[i].getName());
				String strValue = (String) userMap.get((String) propertyDesc[i].getName());
				//System.out.println(strValue);

				if (strValue != null) {
					Object[] oParam = new Object[]{};
					Method mr = propertyDesc[i].getWriteMethod();
					if (mr != null) {
						oParam = new String[]{(strValue)};
						try {
							//注意这里的参数。
							mr.invoke(beanObj, oParam);
						} catch (IllegalArgumentException iea) {
							System.out.println("参数错误。");
							iea.printStackTrace();
						}

					}
				}

			}

			System.out.println(beanObj.getId());
			System.out.println(beanObj.getUserName());

		} catch (IntrospectionException e) {
			System.out.println("Java Bean 内省异常。");
		} catch (IllegalAccessException ia) {
			System.out.println("参数异常。");
			ia.printStackTrace();
		} catch (InvocationTargetException ie) {
			System.out.println("invode异常。");
			ie.printStackTrace();
		}

	}
}
