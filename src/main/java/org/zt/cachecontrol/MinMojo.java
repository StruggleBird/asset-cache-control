package org.zt.cachecontrol;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.mindview.util.TextFile;
import net_alchim31_maven_yuicompressor.ErrorReporter4Mojo;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.IOUtil;

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * @goal min
 * 
 * @author Ternence
 * @date 2015年1月15日
 */
public class MinMojo extends AbstractMojo {
	private Log log = getLog(); //LogFactory.getLog(JmMinMojo.class);
	/**
	 * Location of page output the file.
	 * 该目录在 ${project.build.directory}下
	 * @parameter expression="${min.buildTargetDir}" default-value="${project.build.directory}"
	 */
	private String buildTargetDir;

	/**
	 *
	 * @parameter expression="${min.projectName}" default-value="${project.build.finalName}"
	 */
	private String projectOutputName;


	/**
	 * js，css的版本
	 * @parameter expression="${min.ver}" alias="ver"  default-value="DATE"
	 */
	private String ver;

	/**
	 * @parameter expression="${min.staticAddr}" alias="staticAddr"  default-value="http://localhost/"
	 */
	private String staticAddr;

	/**
	 * jsp缓存目录，作为处理过的jsp的中间输出目录,用于缓存已处理过的jsp
	 * @parameter expression="${min.jspCacheDir}" default-value="jspCacheDir"
	 */
	private String jspCacheDir;

	/**
	 * 静态文件目录/文件集合，它们都是${webAppSourceDirectory}的直接子目录或文件
	 * @parameter expression="${min.staticDirs}"
	 */
	private String[] staticDirs;

	/**
	 * 需要从给定静态文件/目录中排除的文件
	 * @parameter 
	 */
	private String[] excludeStaticDirs;
	
	/**
	 * 静态文件集合的缓存目录,如果是相对路径，将以工程目录为上级目录。
	 * @parameter expression="${min.staticCacheDir}" default-value="staticFilesCacheDir"
	 */
	private String staticFilesCacheDir;
	
	/**
	 * 静态文件的最终位置,所有静态文件处理完后，将从缓存目录复制到此目录
	 * @parameter default-value="${project.build.finalName}_static"
	 * */
	private String targetStaticFilesDir;

	/**
	 * 动态文件的最终位置，所有动态文件处理完后，将从缓存目录复制到此目录
	 * @parameter default-value="${project.build.directory}/${project.build.finalName}"
	 */
	private String targetWebAppDirectory;
	
	/**
	 * 所有动态文件集合 ,它们都应该在${basedir}/${webAppSourceDirectory}/下<b>
	 * @parameter expression="${min.pageSourceDir}"
	 */
	private String[] pageSourceDirs;

	/**
	 * 需要排除的动态文件路径,这些文件（夹）不会复制到目标webapp目录
	 * @parameter expression="${min.pageSourceExcludes}"
	 */
	private String[] pageSourceExcludes;
	
	/**
	 * Insert line breaks in output after the specified column number.
	 *
	 * @parameter expression="${maven.yuicompressor.linebreakpos}" default-value="-1"
	 */
	private int linebreakpos;

	/**
	 * [js only] No compression
	 *
	 * @parameter expression="${maven.yuicompressor.nocompress}" default-value="false"
	 */
	private boolean nocompress;

	/**
	 * [js only] Minify only, do not obfuscate.
	 *
	 * @parameter expression="${maven.yuicompressor.nomunge}" default-value="false"
	 */
	private boolean nomunge;

	/**
	 * [js only] Preserve unnecessary semicolons.
	 *
	 * @parameter expression="${maven.yuicompressor.preserveAllSemiColons}" default-value="false"
	 */
	private boolean preserveAllSemiColons;

	/**
	 * [js only] disable all micro optimizations.
	 *
	 * @parameter expression="${maven.yuicompressor.disableOptimizations}" default-value="false"
	 */
	private boolean disableOptimizations;

	/**
	 * [js only] Display possible errors in the code
	 *
	 * @parameter expression="${maven.yuicompressor.jswarn}" default-value="true"
	 */
	protected boolean jswarn;

	/**
	 * Whether to skip execution.
	 *
	 * @parameter expression="${maven.yuicompressor.skip}" default-value="false"
	 */
	private boolean skip;

	/**
	 * define if plugin must stop/fail on warnings.
	 *
	 * @parameter expression="${maven.yuicompressor.failOnWarning}" default-value="false"
	 */
	protected boolean failOnWarning;

	/**
	 * @parameter default-value="${basedir}"
	 */
	private  String baseDir ;//= new File("").getAbsolutePath() + File.separator;

	/**
	 * web目录,jiemai-web的web目录为：webapp，默认的maven工程web目录为：src/main/webapp
	 * @parameter  alias="webAppSourceDirectory"  expression="${min.webAppSourceDirectory}" default-value="src/main/webapp"
	 */
	private String webAppSourceDirectory;

	/**
	 *  
	 * @parameter default-value="UTF-8"
	 */
	private String encoding;

	/**
	 * 是否缓存动态文件，如果值为否，则动态文件（如jsp文件）将会每次编译都解析
	 * @parameter default-value=false
	 */
	private boolean cacheJsps;
	
	/***
	 *  是否忽略隐藏文件
	 * @parameter default-value=true
	 */
	private boolean ignoreHide;
	
	/*
	 * 要排除的动态文件夹/文件夹，用于处理这样的目录，它里面主要是静态文件，但是有少量动态文件，设置此参数以排除这些动态文件/文件夹
	 * @parameter 
	 */
//	private String[] excludeStaticFiles;
	
	private ErrorReporter4Mojo jsErrorReporter_;

	public void execute() throws MojoExecutionException {
		if(skip){
			return;
		}
		log.info("ver : " + ver);
		if ("DATE".equals(ver)) {

			ver = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());//Long.toString(new Date().getTime());
		}
		ver = "?ver=" + ver;

		baseDir += File.separator;
		log.info("projectName: " + projectOutputName);
		log.info("basedir: " + baseDir);
		log.info("staticDirs: " + Arrays.toString(staticDirs));
		log.info("staticFilesCacheDir : " + staticFilesCacheDir);
		log.info("pageSourceDirs: " + Arrays.toString(pageSourceDirs));
		log.info("cacheJsps: " + cacheJsps);
		log.info("webAppSourceDirectory: " + webAppSourceDirectory);
		log.info("targetWebAppDirectory: " + targetWebAppDirectory);
//		if(excludeStaticDirs != null && excludeStaticDirs.length != 0){
//			for(int i = 0;i < excludeStaticDirs.length ;i ++){
//				excludeStaticDirs[i] = excludeStaticDirs[i].replaceAll("\\\\", File.separator);
//				log.info("excludeStaticDirs[i]: " + excludeStaticDirs[i]);
//			}
//		}
		
		jspCacheDir   = baseDir + jspCacheDir;
		webAppSourceDirectory = baseDir + webAppSourceDirectory;
		staticFilesCacheDir		   = baseDir + staticFilesCacheDir;
//		if(staticDirs != null && staticDirs.length > 0){
//			for(int i = 0; i < staticDirs.length; i ++){
//				staticDirs[i] 	= baseDir + staticDirs[i];
//			}
//		}
//		if(pageSourceExcludes != null && pageSourceExcludes.length > 0){
//			for(int i= 0; i < pageSourceExcludes.length; i ++){
//				pageSourceExcludes[i] = baseDir + pageSourceExcludes[i];
//			}
//		}
		if(StringUtils.isNotBlank(targetStaticFilesDir)){
			targetStaticFilesDir = baseDir + targetStaticFilesDir;
		}
		if(! StringUtils.isBlank(targetWebAppDirectory)){
			if(! targetWebAppDirectory.startsWith("/")){
				targetWebAppDirectory  = baseDir + targetWebAppDirectory;
			}
		}
		
		
		if(! cacheJsps){
			jspCacheDir = baseDir + "jspCacheDir.tmp";
			File cacheDir = new File(jspCacheDir);
//			jspCacheDir += ".tmp"+ new Date().getTime();
//			if(new File(jspCacheDir).exists()){
			if( ! cacheDir.exists()){
				cacheDir.mkdirs();
			}
			else{
//			if(new File("jspCacheDir.tmp").exists() && new File("jspCacheDir.tmp").isDirectory() ){
//				String[] tmpfiles = new File("jspCacheDir.tmp").list();
				log.info("jspCacheDir.tmp path: " + cacheDir.getAbsolutePath());
//				if(cacheDir.isDirectory()){
				try{
					FileUtils.deleteDirectory(cacheDir);
					cacheDir.mkdirs();
				}catch(Exception e){
//					if(! new File(jspCacheDir).delete()){
					log.warn("删除动态文件缓存文件夹 失败：" + cacheDir.getAbsolutePath());
				}
			}
			jspCacheDir +=  File.separator + new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
			if( ! new File(jspCacheDir).mkdir()){
				throw  new IllegalStateException("创建临时缓存文件夹失败：" + jspCacheDir);
			}
//			}
		}
		
		jsErrorReporter_ = new ErrorReporter4Mojo(getLog(), jswarn);
//		webappDir.
//		Set<String> staticset = new HashSet<String>();
		Set<String> excludeSet = new HashSet<String>();
		
		if(pageSourceExcludes != null && pageSourceExcludes.length != 0){
			for(String exclude : pageSourceExcludes){
				if(checkSourceFile(exclude)){
					excludeSet.add(exclude);
				}
			}
		}
		Queue<File> jspQueue = new LinkedList<File>();
		Queue<File> otherQueue = new LinkedList<File>();
		File webappDir = new File(webAppSourceDirectory);
		if(! webappDir.exists() || ! webappDir.isDirectory()){
			throw new IllegalArgumentException("无效的webAppSourceDirectory参数：" + webAppSourceDirectory + " ！");
		}
//		log.debug("**webAppSourceDirectory: " + webappDir.getAbsolutePath());
		LinkedList<File> webList = new LinkedList<File>();
		if(pageSourceDirs != null && pageSourceDirs.length != 0){
			for(String ps : pageSourceDirs){
				File file = new File(getJspAbsolutePath(ps));//new File(webAppSourceDirectory + ps);
				webList.add(file);
			}
		}
		else{
			log.info("在webapp中排除静态文件以获取动态文件！");
			if(staticDirs != null && staticDirs.length != 0){
				for(String staticDir : staticDirs){
					if(checkSourceFile(staticDir)){
						excludeSet.add(staticDir);
					}
				}
			}
			File[] files  = listFiles(webappDir);
			for(File wa : files){
//				if(checkSourceFile(wa)){
				if(! "WEB-INF".equalsIgnoreCase(wa.getName())){
					webList.add(wa);
				}
//				}
			}	
		}
		
		removeExcludeFiles(webList,excludeSet.toArray(new String[]{}),new PathFilter(){	
			@Override public String getRelativePath(File file) {
				return getJspRelativePath(file);
			}
		});
	/*	if(! excludeSet.isEmpty()){
			//排除需要排除的文件/目录
			 for(String sta : excludeSet){	
					for(Iterator<File> iterator = webList.iterator();iterator.hasNext();){
	//				while(! webList.isEmpty()){
						File wa = iterator.next();
						if(sta.equals(getJspRelativePath(wa))){
							iterator.remove();
							break;
						}
						else if(wa.isDirectory() &&  sta.startsWith(getJspRelativePath(wa))){//如果要排除文件在当前目录之下(不一定是直接子文件)
							iterator.remove();
							recuriseCheckStaticFile(webList,wa,sta);
							break;
						}
				}
	//			pageSourceDirs = new String[] { "page" };
			}
		}*/
		for(Iterator<File> iterator = webList.iterator();iterator.hasNext();){
			File file = iterator.next();
			otherQueue.add(new File(getStaticFileOutputPath(file)));
		}
		jspQueue = webList;
		/*else if(pageSourceDirs != null && pageSourceDirs.length > 0){
			log.info("根据pageSourceDirs来设置动态文件 ！");
			for(String pageSourceDir : pageSourceDirs){
				jspQueue.add(new File(getJspAbsolutePath(pageSourceDir)));
				otherQueue.add(new File(getStaticFileOutputPath(pageSourceDir)));
			}
		}*/
		
		//缓存的静态文件的文件路径与修改时间映射
		Map<String,Long> otherMap =  getFileLastmodifiedMap(otherQueue,new PathFilter(){
			@Override
			public String getRelativePath(File file) {
				return getCacheStaticFileRelativePath(file);
			}
		});
		
		List<File> others = new ArrayList<File>();
		//遍历动态文件目录，处理其中的jsp，html等并输出到指定目录，其余文件直接输出到指定目录，
		//css，js等静态文件则既输出到指定的目录，也输出到指定的静态文件缓存目录
		while (!jspQueue.isEmpty()) {
			File dynafile = jspQueue.poll();
			
			File[] files = null ;
			File targetDynamicOutputDir = null; 
			if(dynafile.isDirectory()){
				files = listFiles(dynafile);
				if(files == null || files.length == 0){
					continue;
				}
				targetDynamicOutputDir = new File(getJspOutputPath(dynafile));
//				log.info("动态文件输出目录：" + targetDynamicOutputDir.getAbsolutePath());
			}
			else{
				files = new File[]{dynafile};
				targetDynamicOutputDir = new File(getJspOutputPath(dynafile.getParentFile()));
			}
			if (! targetDynamicOutputDir.exists()) {
				targetDynamicOutputDir.mkdirs();
			}
			log.info("动态文件输出目录：" + targetDynamicOutputDir.getAbsolutePath());
			others.clear();
			

			for (File file : files) {
				if (file.isDirectory()) {
					if (file.list().length > 0) {
//						log.debug("子文件/文件夹数量：" + file.list().length);
						jspQueue.add(file);
					}
				}
				else if(acceptDynamicFile(file)){
					 if (acceptJspFile(file)) {
						 log.debug("///////////////  开始处理: " + getJspRelativePath(file));
						 
							String sjsp = TextFile.read(file);
							sjsp = handleFile(sjsp, ver);
//							log.info("处理后的jsp：");
//							log.info(sjsp);
							TextFile.write(new File(getJspOutputPath(file)), sjsp);
							log.debug(getJspRelativePath(file) + " 处理结束！\\\\\\\\\\\\\\\\\\  ");
						}
					 else{
						 try{
							 FileUtils.copyFileToDirectory(file, targetDynamicOutputDir, false);
							 
//						 FileUtils.copyFile(new File(getJspOutputPath(file)), file);
						 }catch(Exception e){
//							 e.printStackTrace();
//							 log.info("目标路径：" + new File(getJspOutputPath(file)).getAbsolutePath());
							 log.error("*复制动态文件失败*：" + file.getAbsolutePath());
						 }
					 }
				}
				else {
					/*try{
						FileUtils.copyFileToDirectory(file, targetOutputDir, false);
//						 FileUtils.copyFile(new File(getJspOutputPath(file)), file);
						 }catch(Exception e){
							 log.info("*复制动态文件失败*：" + file.getAbsolutePath());
						 }*/
//					if()
					log.info("静态文件x：" + getJspRelativePath(file));
					others.add(file);//把静态文件统一处理,复制到静态缓存目录
				}
			}
			
			//统一移动静态文件,others只包含一个文件夹下的直接子文件
			if (! others.isEmpty()) {
				File targetStaticOutputDir = new File(getStaticFileOutputPath(dynafile));
				File file = null;
				Iterator<File> iterator = others.iterator();
//				try {
					//        			for(File file : others){
					//        			for(int i = 0 ; i < others.size() ; i ++){
					File targetStaticFile = null;
//					File targetDynaFile = null;
					/*if(nocompress){
						targetFile = new File(getStaticFileOutputPath(dir));
					}*/
//					File targetDir = new File(getJspOutputPath(dir));
					while (iterator.hasNext()) {
						file = iterator.next();
						
						Long lm = otherMap.remove(getJspRelativePath(file));
						if(lm == null || lm < file.lastModified() ){
//							targetDynaFile = new File(getJspOutputPath(file));
							
//							        FileUtils.copyFile(file, new File(getJspOutputPath(file)),false);
							try{
//								if(nocompress){
//								
//								}else{
								log.info("动态输出目录：" + targetDynamicOutputDir.getAbsolutePath());
								if((file.getName().endsWith(".js") || file.getName().endsWith(".css")) && ! nocompress){
										targetStaticFile = new File(getStaticFileOutputPath(file));
										log.debug("page中的静态文件的目标地址：" + targetStaticFile);
										compressFile(file, targetStaticFile);
										FileUtils.copyFileToDirectory(targetStaticFile, targetDynamicOutputDir, false);
								  }
								else{
									log.info("复制文件到动态输出目录:" + file.getAbsolutePath());
									FileUtils.copyFileToDirectory(file, targetStaticOutputDir, false);
//										FileUtils.copyFile(file, targetStaticOutputDir, false);
									FileUtils.copyFileToDirectory(file, targetDynamicOutputDir, false);
								}
//								}
								log.debug("复制文件 成功：" + getJspRelativePath(file) + "   to: " + targetStaticOutputDir.getAbsolutePath());
							}catch(Exception e){
								if(file != null)
									log.error("复制文件 *失败*：" + getJspRelativePath(file));
								e.printStackTrace();
							}
						}
					}
			/*	} catch (Exception e) {
					e.printStackTrace();
					
					while (iterator.hasNext()) {
						file = iterator.next();
						log.info("由于之前复制出现异常，此文件 *没有* 复制：" + getJspRelativePath(file));
					}
				}*/
			}
		}
//		if(! others.isEmpty()){
//		if(true)
//			return ;
//		}
		//删除剩余的文件
		deleteStaticCacheRestFile(otherMap);
		otherMap = null;
		File targetwebDir = new File(targetWebAppDirectory);
		if(targetwebDir.exists()){
//			if(! targetwebDir.delete()){
			try{
				FileUtils.deleteDirectory(targetwebDir);
			}catch(Exception e){
				log.info("删除动态文件目标输出文件夹失败："+targetWebAppDirectory + "，原因：" + e.getMessage());
			}
//			}
		}
		try {
			/**把缓存目录中的jsp复制到target目录*/
			File jspCache = new File(jspCacheDir);
			if(jspCache.exists() && ! jspCache.isDirectory()){
				throw new Exception("动态文件没有复制到发布目录：指定的缓存目录【"+jspCacheDir +"】应该是一个目录！");
			}else{
				if (jspCache.isDirectory() &&  listFiles(jspCache).length > 0) {
//					FileUtils.copyDirectory(jspCache, new File(buildTargetDir + File.separator + projectOutputName), false);
					FileUtils.copyDirectory(jspCache, new File(targetWebAppDirectory), false);
				}
//					FileUtils.copyFileToDirectory(jspCache, new File(buildTargetDir + File.separator 	+ projectOutputName), false);
			}
				/*for (File file : jspCache.listFiles()) {
					File targetDir = new File(buildTargetDir + File.separator + projectOutputName + File.separator
							+ getJspRelativePath(file));
					if (targetDir.exists()) {
						if (targetDir.delete()) {
							log.info("删除旧目录成功：" + targetDir.getAbsolutePath());
						} else {
							log.info("删除旧目录失败：" + targetDir.getAbsolutePath());
						}
					}
					if(file.isDirectory()){
						FileUtils.copyDirectory(file, new File(buildTargetDir + File.separator + projectOutputName), false);
					}else{
						FileUtils.copyFileToDirectory(file, new File(buildTargetDir + File.separator 	+ projectOutputName), false);
					}
				}*/
//			}
		} catch (Exception e) {
			throw new MojoExecutionException("Error creating file ", e);
		} finally {

		}

		//--------------------------------------------------------------------------------------------------------------------------------------------\\
		// 开始处理静态文件
		// 处理静态文件目录staticFilesCacheDir    staticDirs
		try {
			/**已在静态文件缓存目录中的文件的相对路径(键)及其最后修改时间(值)*/
			Map<String, Long> staticFileMap = null;
			/**把静态文件复制到指定的目录*/
			File staticCacheDir = new File(staticFilesCacheDir);
			log.info("staticCacheDir : " + staticCacheDir.getAbsolutePath());
			//给定静态文件对应的缓存目录下的目录
			Queue<File> staticCacheQueue = new LinkedList<File>();
			//给定静态文件目录
			Queue<File> staticQueue = new LinkedList<File>();
			//遍历webapp目录中给定的需要缓存的静态文件/文件夹
			if (staticDirs != null && staticDirs.length > 0) {
				//获取给定的静态文件的目录
				for (String staticDir : staticDirs) {
					File file = new File(getJspAbsolutePath(staticDir));
					if (!file.exists()) {
						continue;
					}
					if (new File(staticDir).isAbsolute()) {
						throw new IllegalArgumentException("插件不知道应该把绝对路径的静态文件缓存到哪里：" + staticDir);
					} 
					if (file.isDirectory()) {
						if (listFiles(file).length > 0) {
							staticQueue.add(file);
						}
					} else {
							if(checkSourceFile(file)){
								staticQueue.add(file);
							}
					}
				}
			/*	removeExcludeFiles(staticQueue,excludeStaticDirs , new PathFilter(){
					@Override
					public String getRelativePath(File file) {
						return getJspRelativePath(file);
					}
					
				});*/
				//根据给定的静态文件目录,获取静态文件缓存下的对应目录
				for (String staticDir : staticDirs) {
					
					File file = new File(getStaticFileOutputPath(staticDir));
					if (!file.exists()) {
						continue;
					}
					if (file.isDirectory()) {
						if (listFiles(file).length > 0) {
							staticCacheQueue.add(file);
						}
					} else {
							if(checkSourceFile(file)){
								staticCacheQueue.add(file);
							}
					}
				}
			}

			if (!staticCacheDir.exists()) {
				staticCacheDir.mkdirs();
				staticFileMap = Collections.emptyMap();
			} else {//遍历静态文件缓存目录，取出所有文件的相对路径和修改时间

				//				if (staticCacheDir.listFiles().length > 0) {
				//					staticQueue.add(staticCacheDir);
				/*while (!staticQueue.isEmpty()) {
					File dir = staticQueue.poll();
					for (File file : dir.listFiles()) {
						if (file.isDirectory()) {
							if (file.listFiles().length > 0) {
								staticQueue.add(file);
							}
						} else {
							staticFileMap.put(getCacheStaticFileRelativePath(file), file.lastModified());
						}
					}
				}*/
//				if(! cacheJsps){
					staticCacheDir.delete();
					staticCacheDir.mkdirs();
					staticFileMap = Collections.emptyMap();
//				}else{
				staticFileMap = getFileLastmodifiedMap(staticCacheQueue,new PathFilter(){
					@Override
					public String getRelativePath(File file) {
						return getCacheStaticFileRelativePath(file);
					}
				});
//				}
				//				}
			}

			//检查webapp目录中的静态文件是否比缓存的文件新，如果是则覆盖，否则略过。检查过后删除该文件的文件与最后修改日期映射关系
			while (!staticQueue.isEmpty()) {
				File staticFile = staticQueue.poll();
				/*if (excludeStaticFiles != null && excludeStaticFiles.length != 0) {
					for (String exclu : excludeStaticFiles) {
						if (staticFile.getAbsolutePath().indexOf(exclu) != -1) {
							continue;
						}
					}
				}*/
				if (staticFile.isDirectory()) {
					if (listFiles(staticFile).length > 0) {
						File targetDir = new File(getStaticFileOutputPath(staticFile));
						for (File file : listFiles(staticFile)) {

							if (file.isDirectory() && listFiles(file).length > 0) {
								staticQueue.add(file);
								continue;
							} else if (!file.isDirectory()) {
								Long lastm = staticFileMap.remove(getJspRelativePath(file));
								if (lastm == null || lastm < file.lastModified()) {
									File targetfile = new File(getStaticFileOutputPath(file));
//									FileUtils.copyFile(staticFile, targetfile);
									log.info("处理文件："+getJspRelativePath(file));
									if(checkExcludeOrCompress(getJspRelativePath(file).replaceAll("\\\\", "/"))){
										compressFile(file, targetfile);
									}
									else{
										FileUtils.copyFileToDirectory(file, targetDir);
									}
								}
							}
						}
					}
				}
				//如果当前文件不是一个目录,则判断这个文件是否应该写到缓存目录中
				else {
					Long lastm = staticFileMap.remove(getJspRelativePath(staticFile));
					if (lastm == null || lastm < staticFile.lastModified()) {
						//		        			FileUtils.copyFileToDirectory(file, targetDir);
						File targetfile = new File(getStaticFileOutputPath(staticFile));
//						FileUtils.copyFile(staticFile, targetfile);
						log.info("处理文件："+getJspRelativePath(staticFile));
//						String suffix = file.getName().lastIndexOf(".") > 0 ? file.getName().substring(	file.getName().lastIndexOf(".")) : "";
						if(checkExcludeOrCompress(getJspRelativePath(staticFile).replaceAll("\\\\", "/"))){
							compressFile(staticFile, targetfile);
						}
						else{
							FileUtils.copyFile(staticFile, targetfile);
						}
					}
				}
			}
			deleteStaticCacheRestFile(staticFileMap);
			staticFileMap = null;
			File targetStaticDir = new File(targetStaticFilesDir);
			File targetStaticDir_tmp = new File(targetStaticFilesDir+new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()));
			FileUtils.copyDirectory(staticCacheDir,targetStaticDir_tmp);
			
			
			//   静态文件处理完成   \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
			////////////////////////////////////////////把静态文件移动（重命名）到指定目录下
			if(! targetStaticDir.exists()){
				
//				targetStaticDir.mkdirs();
				 if(! targetStaticDir_tmp.renameTo(targetStaticDir)){
					 FileUtils.moveDirectory(targetStaticDir_tmp,targetStaticDir);
//					 FileUtils.deleteDirectory(targetStaticDir_tmp);
				 }
//				log.info("result:" + re);
//				FileUtils.moveDirectory(targetStaticDir_tmp, targetStaticDir);
			}
			else{
				File baktarget = new File(targetStaticDir_tmp.getName()+"1");
				if( targetStaticDir.renameTo(baktarget)){
					log.info("target renameto baktarget 成功!");
					targetStaticDir_tmp.renameTo(targetStaticDir);
	//					if(baktarget.isDirectory()){
	//						FileUtils.deleteDirectory(baktarget);
	//					}else{
	//						baktarget.delete();
	//					}
				}
				else{
					log.info("target renameto baktarget 失败!");
					/*if(targetStaticDir.isDirectory()){
						FileUtils.deleteDirectory(targetStaticDir);
					}else{
						targetStaticDir.delete();
					}*/
					if(! deleteFile(targetStaticDir)){
						log.info("目标静态文件夹删除失败：" + targetStaticDir.getCanonicalPath());
						return ;
					}
					else{
						targetStaticDir_tmp.renameTo(targetStaticDir);
					}
				}
				FileUtils.deleteDirectory(baktarget);
			}
			
		} catch (Exception e) {
			throw new MojoExecutionException("Error creating file ", e);
		} finally {

		}
	}

	private boolean checkExcludeOrCompress(String relativePath) throws Exception{
		String suffix = relativePath.lastIndexOf(".") > 0 ? relativePath.substring(	relativePath.lastIndexOf(".")) : "";
		if(suffix.equalsIgnoreCase(".js") || suffix.equalsIgnoreCase(".css")){
//			for(String exclu : excludeStaticDirs){
			if(excludeStaticDirs == null){
				return true;
			}
			for(int i = 0;i < excludeStaticDirs.length;i ++){
				String exclu = excludeStaticDirs[i];
				if(exclu == null){
					continue;
				}
				if(relativePath.equals(exclu)){
					
					excludeStaticDirs[i] = null;
					return false;
				}
				if(relativePath.startsWith(exclu)){
					return false;
				}
			}
			return true;
		}
		return false;
	}
	/**
	 * 映射中剩下来的这些文件在webapp目录下都是已经被删除了的
	 * @author Feng.Xu
	 * @since 2013.12.5
	 * @param map
	 */
	private void deleteStaticCacheRestFile(Map<String,Long> staticFileMap){
		if ( staticFileMap.isEmpty()) {
			return;
		}
		for (String r : staticFileMap.keySet()) {
			File file = new File(getStaticFileOutputPath(r));
			if(! file.exists()){
				log.info("文件不存在：" + file.getPath());
				continue;
			}
			if (deleteFile(file)) {
				log.info("删除缓存文件成功：" + getStaticFileOutputPath(r));
			} else {
				log.info("删除已过期的缓存文件失败：" + getStaticFileOutputPath(r));
			}
		}
	}
	
	
	public boolean deleteFile(File file){
		boolean result = true;
		if(file.isDirectory()){
			try{
				FileUtils.deleteDirectory(file);
			}catch(Exception e){
				result = false;
//				log.info("删除文件夹失败：" + getStaticFileOutputPath(file)) ;
				e.printStackTrace();
			}
		}
		else{
			result = file.delete();
		}
		return result;
	}
	
	public boolean acceptDynamicFile(File file) {
//		if (file.getName().endsWith(".jsp") || file.getName().endsWith(".html") || file.getName().endsWith(".htm")) {
		if(		! file.getName().toLowerCase().endsWith(".js") &&
				! file.getName().toLowerCase().endsWith(".css") && 
				! file.getName().toLowerCase().endsWith(".jpg") &&
				! file.getName().toLowerCase().endsWith(".jpeg") && 
				! file.getName().toLowerCase().endsWith(".png") &&
				! file.getName().toLowerCase().endsWith(".gif") &&
				! file.getName().toLowerCase().endsWith(".bmp") 
				){
			return true;
		}
		return false;
	}

	public boolean acceptJspFile(File file){
		if (file.getName().endsWith(".jsp") || file.getName().endsWith(".html") || file.getName().endsWith(".htm")) {
			return true;
		}
		return false;
	}
	/**获取jsp的输出路径*/
	private String getJspOutputPath(File file) {
		//    	String dir = file.getAbsolutePath();
		//    	return dir.replace(webAppSourceDirectory, jspCacheDir);
		return /*baseDir +*/  jspCacheDir + File.separator + getJspRelativePath(file);
	}

	private String getStaticFileOutputPath(File file) {
		//    	return baseDir + File.separator + staticFilesCacheDir + File.separator + getJspRelativePath(file);
		return getStaticFileOutputPath(getJspRelativePath(file));
	}

	private String getStaticFileOutputPath(String relative) {
		return  staticFilesCacheDir + File.separator + relative;
	}

	/***/
	private String getJspRelativePath(File file) {
		String dir = getCanonicalPath(file);//file.getAbsolutePath();
//		log.debug("dir: " + dir) ;
//		log.debug("prefix: " + (webAppSourceDirectory + File.separator));
		if(dir.equals(webAppSourceDirectory)){
			return "";
		}
		dir = dir.substring((webAppSourceDirectory + File.separator).length(), dir.length());
//		log.debug("replace result: " + dir);
		return dir;
	}

	private String getCacheStaticFileRelativePath(File file) {
		String dir = getCanonicalPath(file);//file.getAbsolutePath();
		dir = dir.substring((staticFilesCacheDir + File.separator).length(), dir.length());
//		log.info("replace result: " + dir);
		return dir;
	}

	private String getCanonicalPath(File file){
		String dir = file.getAbsolutePath();
		if(dir.lastIndexOf("/") != -1 && dir.substring(0,dir.lastIndexOf("/")).indexOf(".") != -1){
			try{
				return file.getCanonicalPath();
			}catch(Exception e){
				log.info("获取文件规范路径失败**：" + dir);
				//e.printStackTrace();
			}
		}
		return dir;
	
	}
	private Pattern pattern = Pattern.compile("\\w");
	
	private String getCanonicalRelativePath(String dir){
		Matcher matcher = pattern.matcher(dir);
		if(! matcher.find())//如果dir中没有一个字符则为非法路径 
		{
			log.info("非法的路径：" + dir);
			return dir;
		}
		int startIndex = matcher.start();
//		log.info("start: " + startIndex + " 路径："+dir);
		return  /*File.separator +*/  dir.substring(startIndex);
//		return dir;
	
	}
	
	private String getJspAbsolutePath(String filename) {
		String dir =  webAppSourceDirectory + File.separator + filename;
		/*if(dir.lastIndexOf("/") != -1 && dir.substring(0,dir.lastIndexOf("/")).indexOf(".") != -1){
			try{
				return new File(dir).getCanonicalPath();
			}catch(Exception e){
				log.info("获取文件规范路径失败**：" + dir);
				//e.printStackTrace();
			}
		}*/
		return dir;
	}

	public String handleFile(String jsp, String ver) {
		jsp = resolveImagesRefer(jsp,ver);
		jsp = resolveJsRefer(jsp, ver);
		jsp = resolveCssRefer(jsp, ver);
		return jsp;
	}

	
	private String resolveStaticAddr(String addr){
		if(addr.startsWith("/")){
//			log.info("start with / : " + addr);
			return staticAddr + addr.substring(1);
		}
		else if(addr.indexOf("http://") != -1){ 
			return addr;
		}
		else{
//			log.info("addr: " + addr);
//			log.info("absolute: " + new File(getJspAbsolutePath(addr)).getAbsolutePath());
//			log.info("relative: " +  getJspRelativePath(new File(getJspAbsolutePath(addr))).replace("\\","/"));
//			String s = staticAddr + getJspRelativePath(new File(getJspAbsolutePath(addr))).replace("\\","/");
			String s = staticAddr + getCanonicalRelativePath(addr).replace("\\","/");
//			log.info("加上静态地址：" + s);
			return s;
		}
//		return "";
	}
	
	/**
	 *
	 * @lastModifier Feng.Xu
	 * @lastModificationDate 2014-1-28
	 * @remark 
	 * @param excludeSet 待移除文件集合
	 * @param webList 目标文件集合
	 * @param pathFilter
	 */
	public void removeExcludeFiles(Collection<File> webList,String[] excludeSet,PathFilter pathFilter){
		if( excludeSet == null || excludeSet.length == 0){
			return ;
		}
		//排除需要排除的文件/目录
		 for(String exclu : excludeSet){	
				for(Iterator<File> iterator = webList.iterator();iterator.hasNext();){
//				while(! webList.isEmpty()){
					File wa = iterator.next();
					if(exclu.equals(pathFilter.getRelativePath(wa))){
						iterator.remove();
						break;
					}
					else if(wa.isDirectory() &&  exclu.startsWith(pathFilter.getRelativePath(wa))){//如果要排除文件在当前目录之下(不一定是直接子文件)
						iterator.remove();
						recuriseCheckStaticFile(webList,wa,exclu,pathFilter);
						break;
					}
			}
//			pageSourceDirs = new String[] { "page" };
		}
		
	}
	
	public void recuriseCheckStaticFile(Collection<File> webList,File wa,String sta,PathFilter pathFilter){
//		forstatic: for(String sta : staticset){	
//			for(Iterator<File> iterator = webList.iterator();iterator.hasNext();){
//			while(! webList.isEmpty()){
//				File wa = iterator.next();
//				for(;;){ 
//					if(wa == null)break;
//						if(sta.startsWith(getJspRelativePath(wa))) {
							/*if(sta.equals(getJspRelativePath(wa))){
//								iterator.remove();
//								break forstatic;
								return;
							}
							else*/
//							if(wa.isDirectory()){
			if(listFiles(wa) == null || listFiles(wa).length == 0){
				return ;
			}
			for (File subfile : listFiles(wa)) {
				/*if(! checkSourceFile(subfile)){
					continue;
				}
				else*/
				if (sta.equals(pathFilter.getRelativePath(subfile))) {
					continue;
				} else if (sta.startsWith(pathFilter.getRelativePath(subfile))) {
					// if(subfile.isDirectory() ){
					// webList.poll();
					// break forstatic;
					// return ;
					recuriseCheckStaticFile(webList, subfile, sta,pathFilter);
				} else {
					webList.add(subfile);
				}
			}
		}
//						}
//	}
	
	private static Pattern imagesPattern = Pattern.compile("[\\(|\'|\"]([\\./]*images/)\\S*", Pattern.CASE_INSENSITIVE); // | Pattern.DOTALL
//	private static Pattern imagesPattern = Pattern.compile("[\\(|\'|\"]([\\./]*images/)\\S*[\\)|\'|\"]", Pattern.CASE_INSENSITIVE); // | Pattern.DOTALL
//	private static Pattern imagesPattern = Pattern.compile("[\\(|\'|\"](\\W*images/)\\S*[\\)|\'|\"]", Pattern.CASE_INSENSITIVE); // | Pattern.DOTALL
	private static Pattern imagesPattern_check = Pattern.compile("images/", Pattern.CASE_INSENSITIVE); // | Pattern.DOTALL
	
	public String resolveImagesRefer(String sjsp,String ver){
		Matcher matcher = imagesPattern.matcher(sjsp);
//		Matcher matcher_back = imagesPattern_check.matcher(sjsp);
		int backs = 0;
//		while(matcher_back.find()){
//			backs ++;
//		}
		int matchCount = 0;
		StringBuffer sb = new StringBuffer();
		while (true) {
			if(! matcher.find()){
//				log.info("no Mathcher!!!");
				break;
			}
			matchCount ++;
			String pfull = matcher.group();
			String jsdirPart = matcher.group(1);
//			String versionPart = matcher.group(2);
//			log.info("js version part: " + versionPart) ;
//			log.info("images path part :" + jsdirPart);
//			log.info("full images : " + pfull);
//			if(StringUtils.isNotBlank(versionPart)){
//				pfull = pfull.replace(versionPart, ver);
//			}
			pfull = pfull.substring(0,matcher.start(1)-matcher.start())  + resolveStaticAddr(jsdirPart.substring(jsdirPart.indexOf("images/"))) 
//						+ pfull.substring(matcher.end(1)-matcher.start(),matcher.start(2)-matcher.start()) 
						 + pfull.substring(matcher.end(1)-matcher.start());
//			pfull = pfull.replace(jsdirPart, resolveStaticAddr(jsdirPart.trim()));
			//			log.info("match: " + src) ;
//			log.info("匹配数量:  " + matchCount);
			matcher.appendReplacement(sb, pfull);//matcher.group(1) + "2");
		}
		matcher.appendTail(sb);
//		matcher.
//		if(backs != matchCount ){
//			log.info("images匹配有问题：all: " + backs + "，精确:" + matchCount);
//		}
			
//		log.info("images results: " + sb.toString());
		return sb.toString();
//		return jsp;
	}
	
//	private static Pattern jsPattern = Pattern.compile("<script.*src.*(\"|\'){1}\\s*(.+\\.js){1}([.*|\\s*]&[^\"&^\'])(\"|\'){1}.*>.*</script>", Pattern.CASE_INSENSITIVE); // | Pattern.DOTALL
//	private static Pattern jsPattern = Pattern.compile("<script.*src.*[\"|\']{1}(\\s*.+\\.js){1}(.*)[\"|\']{1}.*>.*</script>", Pattern.CASE_INSENSITIVE); // | Pattern.DOTALL
	private static Pattern jsPattern = Pattern.compile("<script.*\\ssrc\\s*=(\\s*[\"|\'].+\\.js){1}(\\S*[\'|\"]\\s*){1}.*>.*</script>", Pattern.CASE_INSENSITIVE); // | Pattern.DOTALL

	/**
	 * 处理文件中的js包含声明
	 * 
	 * @author Feng.Xu
	 * @param sjsp
	 * @param ver
	 * @return
	 */
	public String resolveJsRefer(String sjsp, String ver) {
		//		String sjsp = TextFile.read("test.jsp");
		/*
		Matcher matcher = Pattern.compile("<script\\s.*src=.*(\"|\'){1}(.+\\.js)+(\"|\'){1}.*>.*</script>",
																	Pattern.CASE_INSENSITIVE  ) // | Pattern.DOTALL
														.matcher(sjsp);
														*/
		Matcher matcher = jsPattern.matcher(sjsp);
		StringBuffer sb = new StringBuffer();
		
		while (true) {
			if(! matcher.find()){
//				log.info("no Mathcher!!!");
				break;
			}
			String pfull = matcher.group();
			String jsdirPart = matcher.group(1);
			String versionPart = matcher.group(2);
//			log.info("js version part: " + versionPart) ;
//			log.info("js path part :" + jsdirPart);
//			log.info("declare js: " + pfull);
//			if(StringUtils.isNotBlank(versionPart)){
//				pfull = pfull.replace(versionPart, ver);
//			}
//			log.info("start 0 :" + matcher.start(0));
//			log.info("end 0: " + matcher.end(0));
//			log.info("start 1: " + matcher.start(1));
//			log.info("end 1: " + matcher.end(1));
//			log.info(pfull.substring(0,matcher.start(1)-matcher.start()));
//			log.info(pfull.substring(matcher.end(1)-matcher.start(),matcher.start(2)-matcher.start()));
//			log.info(pfull.substring(matcher.end(2)-matcher.start()));
			pfull = pfull.substring(0,matcher.start(1)-matcher.start()) + "\"" +resolveStaticAddr(jsdirPart.trim().substring(1)) 
//						+ pfull.substring(matcher.end(1)-matcher.start(),matcher.start(2)-matcher.start()) 
						+ ver + "\"" + pfull.substring(matcher.end(2)-matcher.start());
//			pfull = pfull.replace(jsdirPart, resolveStaticAddr(jsdirPart.trim()));
			//			log.info("match: " + src) ;
			matcher.appendReplacement(sb, pfull);//matcher.group(1) + "2");
		}
		matcher.appendTail(sb);
		//log.info("results: " + sb.toString());
		return sb.toString();
	}
	
//	private static Pattern cssPattern = Pattern.compile("<link.*href.*[\"|\']{1}(\\s*|.+\\.css){1}(.*&^\\s*)[[\"|\']\\s*]{1}.*/>", Pattern.CASE_INSENSITIVE); // | Pattern.DOTALL
	private static Pattern cssPattern = Pattern.compile("<link.*\\shref\\s*=(\\s*[\"|\'].+\\.css){1}(\\S*[\'|\"]\\s*){1}.*/>", Pattern.CASE_INSENSITIVE); // | Pattern.DOTALL
	/***
	 * 处理jsp文件中的css包含声明
	 * @author Feng.Xu
	 * @param sjsp
	 * @param ver
	 * @return
	 */
	public String resolveCssRefer(String sjsp, String ver) {
		//		String sjsp = TextFile.read("test.jsp");
		/*
		 * Matcher matcher = Pattern.compile("<link\\s.*href=(\"|\'){1}(.+\\.css)+(\"|\'){1}.*>",
																	Pattern.CASE_INSENSITIVE  ) // | Pattern.DOTALL
														.matcher(sjsp);
														*/
		Matcher matcher = cssPattern.matcher(sjsp);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String pfull = matcher.group();
			String cssPath = matcher.group(1);
//			String cssVer = matcher.group(2);
//			if(StringUtils.isBlank(cssPath)){
//				log.info("jsp: " + sjsp); 
//			}
//			log.info("cssPath: " + cssPath) ;
//			pfull = pfull.replace(psome, resolveStaticAddr(psome) + psome.trim() + ver);
			//			log.info("match: " + src) ;
			pfull = pfull.substring(0,matcher.start(1)-matcher.start())+"\""+resolveStaticAddr(cssPath.trim().substring(1)) 
//					+	pfull.substring(matcher.end(1)-matcher.start(),matcher.start(2)-matcher.start())
					+ ver +"\""+ pfull.substring(matcher.end(2)-matcher.start());
			matcher.appendReplacement(sb, pfull);//matcher.group(1) + "2");
		}
		matcher.appendTail(sb);
		//log.info("results: " + sb.toString());
		return sb.toString();
	}
	
	/**
	 * 获取一个映射，表示已在静态文件缓存目录中的文件的相对路径(键)及其最后修改时间(值)
	 * @author Feng.Xu 
	 * @since 1.0  2013-12-5 
	 * @param queue 需要递归的文件夹队列
	 * @param cachePath 值为"static"，或"dynamic"，分别表示静态缓存目录文件或动态文件缓存目录,否则存储绝对路径作为键
	 * @return
	 */
	private Map<String, Long> getFileLastmodifiedMap(Queue<File> queue,PathFilter pf) {
		Map<String, Long> map = null;
		if (queue == null || queue.isEmpty()) {
			return  Collections.emptyMap();
		}
		map = new HashMap<String, Long>();
		while (!queue.isEmpty()) {
			File sel = queue.poll();
			if(! sel.exists()){
				continue;
			}
			if(sel.isDirectory()){
				for (File file : listFiles(sel)) {
					if (file.isDirectory()) {
						if (listFiles(sel).length > 0) {
							queue.add(file);
						}
					} else {
//							addFileToMap(map,file,cachePath);
						map.put(pf.getRelativePath(file), file.lastModified());
					}
				}
			}
			else{
//					addFileToMap(map,sel,cachePath);
				map.put(pf.getRelativePath(sel), sel.lastModified());
			}
		}
		
		return map;
	}
	
	private  void addFileToMap(Map<String, Long> map,File file,String cachePath){
		if("static".equalsIgnoreCase(cachePath)){
			map.put(getCacheStaticFileRelativePath(file), file.lastModified());
		}
		else if("dynamic".equalsIgnoreCase(cachePath)){
			map.put(getJspOutputPath(file), file.lastModified());
		}
		else{
			map.put(file.getAbsolutePath(), file.lastModified());
		}
	}

	private void compressFile(File inFile, File outFile) throws Exception {
		String suffix = inFile.getName().lastIndexOf(".") > 0 ? inFile.getName().substring(	inFile.getName().lastIndexOf(".")) : null;
		log.debug("suffix:" + suffix) ;
		if (suffix != null) {
			if (".js".equalsIgnoreCase(suffix)) {
				suffix = ".js";
			} else if (".css".equalsIgnoreCase(suffix)) {
				suffix = ".css";
			}
		}
		if (getLog().isDebugEnabled()) {
			getLog().debug("compress file :" + inFile + " to " + outFile);
		}

		InputStreamReader in = null;
		OutputStreamWriter out = null;
		File outFileTmp = new File(outFile.getAbsolutePath() + ".tmp");
		File outFileTmp2 = null;//new File(outFile.getAbsolutePath() + ".tmp2");
		outFileTmp.delete();
//		FileUtils.forceDelete(outFileTmp);
		try {
			if(".js".equalsIgnoreCase(suffix) || ".css".equalsIgnoreCase(suffix)){
				String sjsp = TextFile.read(inFile);
//				String rpath = getJspRelativePath(inFile);
//				log.info("正在处理文件的'images'：" + rpath);
				sjsp = resolveImagesRefer(sjsp, ver);
//				outFileTmp2 = new File(outFile.getAbsolutePath() + ".tmp2");
//				FileWriter fw = new FileWriter(outFileTmp2);
//				fw.write(sjsp);
//				fw.flush();
//				fw.close();
////				log.info("处理文件的'images'完成：" + rpath);
//				in = new InputStreamReader(new FileInputStream(outFileTmp2), encoding);
				in = new InputStreamReader(new ByteArrayInputStream(sjsp.getBytes(encoding)), encoding);
			}
			else{
				in = new InputStreamReader(new FileInputStream(inFile));
			}
			if (!outFile.getParentFile().exists() && !outFile.getParentFile().mkdirs()) {
				throw new MojoExecutionException("Cannot create resource output directory: " + outFile.getParentFile());
			}
			//            getLog().debug("use a temporary outputfile (in case in == out)");

			//            getLog().debug("start compression");
			out = new OutputStreamWriter(new FileOutputStream(outFileTmp), encoding);
			if (".js".equalsIgnoreCase(suffix)) {
				JavaScriptCompressor compressor = new JavaScriptCompressor(in, jsErrorReporter_);
				compressor.compress(out, linebreakpos, !nomunge, jswarn, preserveAllSemiColons, disableOptimizations);
			} else if (".css".equalsIgnoreCase(suffix)) {
				compressCss(in, out);
			} else {
				getLog().info("No compression is enabled");
				IOUtil.copy(in, out);
			}
		}catch(Exception e){
			e.printStackTrace();
			log.info("出错的文件：" + getJspRelativePath(inFile));
			//            getLog().debug("end compression");
		} finally {
			IOUtil.close(in);
			IOUtil.close(out);
		}
//		FileUtils.forceDelete(outFile);
		if(outFileTmp2 != null && outFileTmp2.exists()){
			outFileTmp2.delete();
		}
		outFile.delete();
		FileUtils.moveFile(outFileTmp, outFile);
	}

	public File[] listFiles(File dir){
		return dir.listFiles(new FileFilter(){
			@Override
			public boolean accept(File file) {
				return checkSourceFile(file);
			}
		});
//		return arrays;
	}
	
	private boolean checkSourceFile(String fpath){
		File file  = new File(getJspAbsolutePath(fpath));
		return checkSourceFile(file);
	}
	
	private boolean checkSourceFile(File file){
		if(ignoreHide){
			if(! file.exists() || file.isHidden()){
				return false;
			}
		}
		return true;
	}
	
	private void checkTargetDir(String dir){
		
	}
	
	private void compressCss(InputStreamReader in, OutputStreamWriter out) throws IOException {
		try {
			CssCompressor compressor = new CssCompressor(in);
			compressor.compress(out, linebreakpos);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(
					"Unexpected characters found in CSS file. Ensure that the CSS file does not contain '$', and try again",
					e);
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public String getStaticAddr() {
		return staticAddr;
	}

	public void setStaticAddr(String staticAddr) {
		this.staticAddr = staticAddr;
	}
	
}

interface PathFilter {
	public String getRelativePath(File file);
}
/*class JmFileFilter implements FileFilter{
	
//	private 
	@Override
	public boolean accept(File file) {
		
		return false;
	}
	
}*/
