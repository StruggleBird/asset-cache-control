package org.zt.cachecontrol;

import java.io.File;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * 给指定的资源文件打上版本号
 * 
 * @author Ternence
 * @date 2015年1月15日
 */
public class ResourceMojo extends AbstractMojo {

	/**
	 * @parameter expression="${version.resourcesURL}" alias="resourcesURL"
	 *            default-value=""
	 */
	protected String resourcesURL = ""; // 静态资源URL

	/**
	 * @parameter expression="${version.webappDir}" alias="webappDir"
	 *            default-value="src/main/webapp"
	 */
	protected String webappDir; // webapp根目录

	/**
	 * @parameter expression="${version.pageDirs}" alias="pageDirs"
	 *            default-value="/"
	 */
	protected String[] pageDirs; // 需要替换静态URL的文件目录
	/**
	 * @parameter expression="${version.resourcesDirs}" alias="resourcesDirs"
	 */
	protected String[] resourcesDirs; // 需要打包的静态资源目录
	/**
	 * @parameter expression="${version.version}" alias="version"
	 */
	protected String version;

	/**
	 * @parameter expression="${version.suffixs}" alias="suffixs"
	 */
	protected String[] suffixs;

	public ResourceMojo() {

	}

	static final int BUFFER = 2048;

	protected Log log = getLog();

	public static Pattern IMAGES_PATTERN = Pattern
			.compile(
					"<img[\\s\\S]+?src\\s*=\\s*[\"|\']\\s*([/images|images].+\\.[(png)|(jpg)|(gif)|(bmp)|(jpeg)]+.*?)[\"|\']{1}",
					Pattern.CASE_INSENSITIVE);

	public static Pattern JS_PATTERN = Pattern.compile(
			"<script[\\s\\S]+?src\\s*=\\s*[\"|\'](.+\\.js.*?)[\"|\']{1}",
			Pattern.CASE_INSENSITIVE);

	public static Pattern CSS_PATTERN = Pattern.compile(
			"<link[\\s\\S]+?href\\s*=\\s*[\"|\'](.+\\.css.*?)[\"|\']{1}",
			Pattern.CASE_INSENSITIVE);

	/**
	 * @parameter default-value="${basedir}"
	 */
	protected String baseDir;

	/**
	 * 构建输出目录，缺省为target
	 * 
	 * @parameter default-value="${project.build.directory}"
	 */
	protected String target;

	/**
	 * 静态包名称
	 * 
	 * @parameter default-value="${project.artifactId}"
	 */
	protected String statifFile;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		init();
	}

	/**
	 * 
	 * @create 2015年1月16日
	 */
	private void init() {
		// 设置版本默认值
		if (StringUtils.isEmpty(version)) {
			version = new Date().getTime() + "";
		}

		// 设置处理文件后缀
		if (ArrayUtils.isEmpty(suffixs)) {
			suffixs = new String[] { "jsp", "htm", "html", "ftl" };
		}
		
		webappDir = baseDir + File.separator + webappDir;

	}

	/**
	 * @return the resourcesURL
	 */
	public String getResourcesURL() {
		return resourcesURL;
	}

	/**
	 * @param resourcesURL
	 *            the resourcesURL to set
	 */
	public void setResourcesURL(String resourcesURL) {
		this.resourcesURL = resourcesURL;
	}

	/**
	 * @return the webappDir
	 */
	public String getWebappDir() {
		return webappDir;
	}

	/**
	 * @param webappDir
	 *            the webappDir to set
	 */
	public void setWebappDir(String webappDir) {
		this.webappDir = webappDir;
	}

	/**
	 * @return the pageDirs
	 */
	public String[] getPageDirs() {
		return pageDirs;
	}

	/**
	 * @param pageDirs
	 *            the pageDirs to set
	 */
	public void setPageDirs(String[] pageDirs) {
		this.pageDirs = pageDirs;
	}

	/**
	 * @return the resourcesDirs
	 */
	public String[] getResourcesDirs() {
		return resourcesDirs;
	}

	/**
	 * @param resourcesDirs
	 *            the resourcesDirs to set
	 */
	public void setResourcesDirs(String[] resourcesDirs) {
		this.resourcesDirs = resourcesDirs;
	}

	/**
	 * @return the target
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * @param target
	 *            the target to set
	 */
	public void setTarget(String target) {
		this.target = target;
	}

	/**
	 * @return the statifFile
	 */
	public String getStatifFile() {
		return statifFile;
	}

	/**
	 * @param statifFile
	 *            the statifFile to set
	 */
	public void setStatifFile(String statifFile) {
		this.statifFile = statifFile;
	}

	/**
	 * @return the suffixs
	 */
	public String[] getSuffixs() {
		return suffixs;
	}

	/**
	 * @param suffixs
	 *            the suffixs to set
	 */
	public void setSuffixs(String[] suffixs) {
		this.suffixs = suffixs;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

}
