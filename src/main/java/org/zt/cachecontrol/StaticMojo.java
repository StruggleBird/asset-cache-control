package org.zt.cachecontrol;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.mindview.util.TextFile;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;


/**
 * @goal staticproc
 * @phase process-resources
 * @author Ternence
 * @date 2015年1月15日
 */
public class StaticMojo extends AbstractMojo {

    /**
     * @parameter expression="${staticprc.staticURL}" alias="staticURL" default-value=""
     */
    private String staticURL = ""; // 静态资源URL

    /**
     * @parameter expression="${staticprc.webappDir}" alias="webappDir"
     *            default-value="src/main/webapp"
     */
    private String webappDir; // webapp根目录

    /**
     * @parameter expression="${staticprc.pageDirs}" alias="pageDirs" default-value="/"
     */
    private String[] pageDirs; // 需要替换静态URL的文件目录
    /**
     * @parameter expression="${staticprc.staticDirs}" alias="staticDirs"
     */
    private String[] staticDirs; // 需要打包的静态资源目录
    /**
     * @parameter expression="${staticprc.ver}" alias="ver"
     */
    private String ver;


    public StaticMojo() {
        // 设置版本默认值
        if (StringUtils.isEmpty(ver)) {
            ver = new Date().getTime() + "";
        }
    }

    static final int BUFFER = 2048;

    private static List<String> SUFIX = new ArrayList<String>();

    private Log log = getLog();

    private static Pattern imagesPattern = Pattern.compile(
                    "<img.*\\ssrc\\s*=\\s*[\"|\']\\s*([/images|images].+\\.[(png)|(jpg)|(gif)|(bmp)|(jpeg)]+){1}", Pattern.CASE_INSENSITIVE); // |
                                                                                                                                              // Pattern.DOTALL
    private static Pattern jsPattern = Pattern.compile("<script.*\\ssrc\\s*=\\s*[\"|\'](.+\\.js){1}", Pattern.CASE_INSENSITIVE); // |
                                                                                                                                 // Pattern.DOTALL
    private static Pattern cssPattern = Pattern.compile("<link.*\\shref\\s*=\\s*[\"|\'](.+\\.css){1}", Pattern.CASE_INSENSITIVE); // |
                                                                                                                                  // Pattern.DOTALL
    static {
        SUFIX.add("jsp");
        SUFIX.add("htm");
    }

    /**
     * @parameter default-value="${basedir}"
     */
    private String baseDir;

    /**
     * 构建输出目录，缺省为target
     * 
     * @parameter default-value="${project.build.directory}"
     */
    private String target;

    /**
     * 静态包名称
     * @parameter default-value="${project.artifactId}"
     */
    private String statifFile;



    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        webappDir = baseDir + File.separator + webappDir;
        replacePageStaticUrl();
        // zipStaticFile();

    }

    private void replacePageStaticUrl() {

        for (String pageDir : pageDirs) {
            String absDir = webappDir + File.separator + pageDir;
            File f = new File(absDir);
            iterateDir(f);
        }
    }

    // 遍历处理
    private void iterateDir(File f) {
        if (f.isDirectory()) {
            File[] fs = f.listFiles();
            for (File file : fs) {
                iterateDir(file);
            }
        } else {
            String fileName = f.getName();
            int dot = fileName.lastIndexOf(".");
            if (dot > 0) {
                String sufix = fileName.substring(dot + 1);
                if (SUFIX.contains(sufix.toLowerCase())) {
                    log.info("processing file:" + f.getAbsolutePath());
                    String text = TextFile.read(f, "UTF-8");;
                    text = handleFile(text);
                    TextFile.write(f, text, "UTF-8");
                }
            }
        }

    }

    public String handleFile(String jsp) {
        jsp = resolveJsRefer(jsp);
        jsp = resolveCssRefer(jsp);
        jsp = resolveImageRefer(jsp);
        return jsp;
    }

    public String resolveJsRefer(String sjsp) {
        Matcher matcher = jsPattern.matcher(sjsp);
        StringBuffer sb = new StringBuffer();

        while (true) {
            if (!matcher.find()) {
                break;
            }
            String pfull = matcher.group();
            String jsdirPart = matcher.group(1);
            pfull = pfull.substring(0, matcher.start(1) - matcher.start()) + resolveStaticAddr(jsdirPart.trim().substring(0));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(pfull));// matcher.group(1) +
                                                                           // "2");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public String resolveImageRefer(String sjsp) {
        Matcher matcher = imagesPattern.matcher(sjsp);
        StringBuffer sb = new StringBuffer();

        while (true) {
            if (!matcher.find()) {
                break;
            }
            String pfull = matcher.group();

            String jsdirPart = matcher.group(1);
            pfull = pfull.substring(0, matcher.start(1) - matcher.start()) + resolveStaticAddr(jsdirPart.trim().substring(0));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(pfull));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public String resolveCssRefer(String sjsp) {

        Matcher matcher = cssPattern.matcher(sjsp);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String pfull = matcher.group();
            String cssPath = matcher.group(1);
            pfull = pfull.substring(0, matcher.start(1) - matcher.start()) + resolveStaticAddr(cssPath.trim().substring(0));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(pfull));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String resolveStaticAddr(String addr) {
        String newURL = "";
        addr = addr.trim();
        if (addr.startsWith("/")) {
            // log.info("start with / : " + addr);
            newURL = staticURL + addr.substring(1);
        } else if (addr.indexOf("http://") != -1) {
            newURL = addr;
        } else {
            newURL = staticURL + addr;
        }

        if (newURL.contains("?")) {
            newURL += "&v=" + ver;
        } else {
            newURL += "?v=" + ver;
        }

        return newURL;
    }


    private void zipStaticFile() {
        try {
            FileOutputStream dest = new FileOutputStream(baseDir + File.separator + statifFile + "-static-" + ver + ".zip");
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte[] buffere = new byte[8192];
            int length;
            BufferedInputStream bis;


            for (String staticDir : staticDirs) {
                String absDir = webappDir + File.separator + staticDir;
                List<?> fileList = loadFilename(new File(absDir));
                for (int i = 0; i < fileList.size(); i++) {
                    File file = (File) fileList.get(i);
                    out.putNextEntry(new ZipEntry(getEntryName(file)));
                    bis = new BufferedInputStream(new FileInputStream(file));
                    log.info("正在压缩" + file.getAbsolutePath());
                    while (true) {
                        length = bis.read(buffere);
                        if (length == -1)
                            break;
                        out.write(buffere, 0, length);
                    }
                    bis.close();
                    out.closeEntry();
                }
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println(new StaticMojo().resolveImageRefer("<img  border='0'  class='story-img' src=\"images/front/man.jpg\"  />"));
        System.out.println(new StaticMojo().resolveCssRefer("<link href=\"css/left_category.css\" rel=\"stylesheet\" type=\"text/css\" />"));
        System.out.println(new StaticMojo()
                        .resolveJsRefer("<script language=\"javascript\" type=\"text/javascript\"	src=\"js/jquery/jquery.jqzoom.js\"></script>"));
    }

    private String getEntryName(File file) {
        File base = new File(webappDir);
        String ret = file.getName();
        File real = file;
        while (true) {
            real = real.getParentFile();
            if (real == null)
                break;
            if (real.equals(base))
                break;
            else {
                ret = real.getName() + "/" + ret;
            }
        }
        return ret;
    }

    /**
     * 递归获得该文件下所有文件名(不包括目录名)
     * 
     * @param file
     * @return
     */
    private List<File> loadFilename(File file) {
        List<File> filenameList = new ArrayList<File>();
        if (file.isFile()) {
            filenameList.add(file);
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                filenameList.addAll(loadFilename(f));
            }
        }
        return filenameList;
    }

    /**
     * @return the staticURL
     */
    public String getStaticURL() {
        return staticURL;
    }

    /**
     * @param staticURL the staticURL to set
     */
    public void setStaticURL(String staticURL) {
        this.staticURL = staticURL;
    }

    /**
     * @return the pageDirs
     */
    public String[] getPageDirs() {
        return pageDirs;
    }

    /**
     * @param pageDirs the pageDirs to set
     */
    public void setPageDirs(String[] pageDirs) {
        this.pageDirs = pageDirs;
    }

    /**
     * @return the staticDirs
     */
    public String[] getStaticDirs() {
        return staticDirs;
    }

    /**
     * @param staticDirs the staticDirs to set
     */
    public void setStaticDirs(String[] staticDirs) {
        this.staticDirs = staticDirs;
    }

    /**
     * @return the ver
     */
    public String getVer() {
        return ver;
    }

    /**
     * @param ver the ver to set
     */
    public void setVer(String ver) {
        this.ver = ver;
    }

    /**
     * @return the webappDir
     */
    public String getWebappDir() {
        return webappDir;
    }

    /**
     * @param webappDir the webappDir to set
     */
    public void setWebappDir(String webappDir) {
        this.webappDir = webappDir;
    }

    /**
     * @return the statifFile
     */
    public String getStatifFile() {
        return statifFile;
    }

    /**
     * @param statifFile the statifFile to set
     */
    public void setStatifFile(String statifFile) {
        this.statifFile = statifFile;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }



}
