package qiaomuer;

import org.junit.Test;

import java.net.URL;
import java.util.Enumeration;

/**
 * <p>
 * 类详细描述
 * </p>
 * @author kun.yi
 * @since 1.0
 */
public class ResourceLoadTest {

	public static void main(String[] args) throws  Exception{
		Enumeration<URL> resources = ResourceLoadTest.class.getClassLoader().getResources("");
		while (resources.hasMoreElements()) {
			URL url =  resources.nextElement();
			System.out.println("url:"+url.getPath());
		}
	}

}
