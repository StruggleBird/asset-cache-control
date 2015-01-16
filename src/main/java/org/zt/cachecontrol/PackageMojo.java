
package org.zt.cachecontrol;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

public class PackageMojo extends AbstractMojo {

    private Log log = getLog(); // LogFactory.getLog(JmMinMojo.class);
    /**
     * web目录,默认的maven工程web目录为：src/main/webapp。
     * 
     * @parameter alias="webAppSourceDirectory" expression="${pkg.webAppSourceDirectory}"
     *            default-value="src/main/webapp"
     */
    private String webAppSourceDirectory;

    /**
     * @parameter default-value="${basedir}"
     */
    private String baseDir;

    /**
     * 目标webapp目录
     * 
     * @parameter alias="targetWebAppDirectory" expression="${pkg.targetWebAppDirectory}"
     *            default-value="${project.build.directory}/${project.build.finalName}"
     */
    private String targetWebAppDirectory;

    /**
     * @parameter alias="copyCompileOutputDir" expression="${pkg.copyCompileOutputDir}"
     *            default-value="false"
     */
    public boolean copyCompileOutputDir;

    /**
     * 默认的编译输出目录，maven-compiler-plugin一般会把class和resource输出到target/classess中
     * 
     * @parameter alias="compileOutputDirectory" expression="${pkg.compileOutputDirectory}"
     *            default-value="${project.build.directory}/classes"
     */
    private String compileOutputDirectory;

    /**
     * 是否复制WEB-INF/lib目录下的jar包
     * 
     * @parameter alias="copylib" expression="${pkg.copylib}" default-value="false"
     */
    private boolean copylib;

    /***
     * 是否忽略隐藏文件
     * 
     * @parameter default-value=true
     */
    private boolean ignoreHide;

    /**
     * 移动WEB-INF下的文件到目标文件夹中
     * 
     * @throws MojoExecutionException
     */
    public void cpWebinfos() throws MojoExecutionException {

        baseDir += File.separator;

        webAppSourceDirectory = baseDir + webAppSourceDirectory;
        targetWebAppDirectory = baseDir + targetWebAppDirectory;

        String webinfo = webAppSourceDirectory + "/WEB-INF";
        File targetdir = new File(targetWebAppDirectory + "/WEB-INF");

        File webinfodir = new File(webinfo);
        if (!webinfodir.exists() || !webinfodir.isDirectory()) {
            log.info("目录不存在或不是一个目录：" + webinfodir.getAbsolutePath());
            throw new MojoExecutionException("错误的webAppSourceDirectory参数" + webAppSourceDirectory + "：没有找到 WEB-INF 子目录！");
        }
        if (targetdir.exists()) {
            FileUtils.deleteQuietly(targetdir);
        }

        File[] webinfofiles = listFiles(webinfodir);
        for (int index = 0; index < webinfofiles.length; index++) {
            if (webinfofiles[index] == null) {
                continue;
            }
            if ("classes".equals(webinfofiles[index].getName())) { // 过虑掉WEB-INF目录下的classes子目录
                webinfofiles[index] = null;
            }
        }
        for (File file : webinfofiles) {
            if (file == null) {
                continue;
            }
            if ("lib".equals(file.getName()) && !copylib) {// 忽略lib及其下的jar包
                continue;
            }
            try {
                if (file.isDirectory()) {
                    FileUtils.copyDirectory(file, targetdir, false);
                } else {
                    FileUtils.copyFileToDirectory(file, targetdir);
                }
                log.info("移动目录/文件：" + file.getAbsolutePath());
            } catch (Exception e) {
                log.info("目录/文件复制失败：" + file.getAbsolutePath());
            }
        }
        if (copyCompileOutputDir) {
            File classDir = new File(compileOutputDirectory);
            try {
                FileUtils.copyDirectory(classDir, new File(targetdir.getAbsolutePath() + File.separator + "classes"), false);
                log.info("移动目录/文件：" + compileOutputDirectory);
            } catch (Exception e) {
                log.info("移动classes文件夹失败！");
                e.printStackTrace();
            }
        }
        /*
         * if(compileOutputDirectory.indexOf("classes") == -1){
         * if(compileOutputDirectory.endsWith("/")){ compileOutputDirectory += "classes"; } else{
         * compileOutputDirectory += "/classes"; } }
         */


    }

    public void execute() throws MojoExecutionException {
        cpWebinfos();
    }

    public File[] listFiles(File dir) {
        return dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (ignoreHide && file.isHidden()) {
                    return false;
                }
                return true;
            }
        });
        // return arrays;
    }
}
