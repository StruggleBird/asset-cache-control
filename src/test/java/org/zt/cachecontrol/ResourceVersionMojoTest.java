/**
 * 
 */
package org.zt.cachecontrol;

import java.util.regex.Pattern;

import org.junit.Test;

import junit.framework.TestCase;

/**
 * @author Ternence
 * @create 2015年1月16日
 */
public class ResourceVersionMojoTest extends TestCase {

	private Pattern cssPattern = Pattern.compile(
			"<link[\\s\\S]+?href\\s*=\\s*[\"|\'](.+\\.css){1}",
			Pattern.CASE_INSENSITIVE);
	
	@Test
	public void test() {
		fail("Not yet implemented");
	}
	
	
	@Test
	public void testCssPattern() {
		assertTrue(
				cssPattern.matcher("<link type=\"text/css\" rel=\"stylesheet\"	href=\"/wiki/s/d41d8cd98f00b204e9800998ecf8427e/en_GB-1988229788/4731/0a2d13ba65b62df25186f4b87e6c642af1792689.1/86/_/download/superbatch/css/batch.css\" media=\"all\">").find());
		
	}

}
