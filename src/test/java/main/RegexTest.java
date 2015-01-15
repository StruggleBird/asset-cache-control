package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.mindview.util.TextFile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.zt.cachecontrol.MinMojo;


public class RegexTest {


    // @Test
    public void test() {
        String sjsp = TextFile.read("test.jsp");
        /*
         * Matcher matcher =
         * Pattern.compile("<script\\s.*src=.*(\"|\'){1}(.+\\.js)+(\"|\'){1}.*>.*</script>",
         * Pattern.CASE_INSENSITIVE ) // | Pattern.DOTALL .matcher(sjsp);
         */
        Matcher matcher = Pattern.compile("<script.*(.+\\.js){1}.*>.*</script>", Pattern.CASE_INSENSITIVE) // |
                                                                                                           // Pattern.DOTALL
                        .matcher(sjsp);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String pfull = matcher.group();
            String psome = matcher.group(1);
            // System.out.println("match: " + s) ;
            pfull = pfull.replace(psome, psome.trim() + "?23");
            // System.out.println("match: " + src) ;
            matcher.appendReplacement(sb, pfull);// matcher.group(1) + "2");
        }
        matcher.appendTail(sb);
        System.out.println("results: " + sb.toString());

    }

    // @Test
    public void testCss() {
        String sjsp = TextFile.read("test.jsp");
        /*
         * Matcher matcher = Pattern.compile("<link\\s.*href=(\"|\'){1}(.+\\.css)+(\"|\'){1}.*>",
         * Pattern.CASE_INSENSITIVE ) // | Pattern.DOTALL .matcher(sjsp);
         */
        Matcher matcher = Pattern.compile("<link.*(.+\\.css){1}.*/>", Pattern.CASE_INSENSITIVE) // |
                                                                                                // Pattern.DOTALL
                        .matcher(sjsp);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String pfull = matcher.group();
            String psome = matcher.group(1);
            // System.out.println("match: " + s) ;
            pfull = pfull.replace(psome, psome.trim() + "?23");
            // System.out.println("match: " + src) ;
            matcher.appendReplacement(sb, pfull);// matcher.group(1) + "2");
        }
        matcher.appendTail(sb);
        System.out.println("results: " + sb.toString());
    }

    // @Test
    public void testimbarcode() {
        String imbarcode = "900965-SPA-0098";
        Pattern pattern = Pattern.compile("\\d{6,}-\\S+");
        if (pattern.matcher(imbarcode).matches()) {
            System.out.println("匹配成功！");
        } else {
            System.out.println("失败！");
        }
    }

    // @Test
    public void testseclon() {
        Pattern pattern = Pattern.compile("[\'](.*)[\']");
        Matcher matcher = pattern.matcher("sdsd'ddd'sdsf");
        while (matcher.find()) {
            System.out.println(matcher.group());
        }
        pattern = Pattern.compile("\\w");
        System.out.println("match all?: " + pattern.matcher("../js/sensitive-word-custom.js").matches());
        System.out.println("match part?: " + pattern.matcher("../js/sensitive-word-custom.js").find());
    }

    private static Pattern jsPattern = Pattern.compile("<script.*\\ssrc\\s*=(\\s*[\"|\'].+\\.js){1}(\\S*[\'|\"]\\s*){1}.*>.*</script>",
                    Pattern.CASE_INSENSITIVE);
    private static Pattern cssPattern = Pattern.compile("<link.*\\shref\\s*=(\\s*[\"|\'].+\\.css){1}(\\S*[\'|\"]\\s*){1}.*/>",
                    Pattern.CASE_INSENSITIVE); // | Pattern.DOTALL

    // @Test

    public void renametest() {
        // System.out.println("reesult: " + new File("t1").renameTo(new File("t2")));
        String js1 = "3453535435434<script type=\"text/javascript\" src=\"js/jquery.jmutil.js\"></script>345345345";
        String js2 = "345eterterter<script  src=\"js/JsonUtil.js\" type=\"text/javascript\"></script>ertertedgdfgdf";
        String css1 = "erte<link href=\"/css/ac.css?${jmversion}\" rel=\"stylesheet\" type=\"text/css\" />ergdf";
        String css2 = "34535435<link type=\"text/css\" rel=\"stylesheet\" href=\"/js/jBox/Skins/Default/jbox.css\"/>345345";
        // System.out.println("js1: " + js1);
        System.out.println("处理js 1");
        resolveJsRefer(js1, "12");
        System.out.println("处理js2");
        resolveJsRefer(js2, "12");
        System.out.println("处理css1:");
        resolveCssRefer(css1, "?13");
        System.out.println("处理css2:");
        resolveCssRefer(css2, "?13");
        // Pattern p = Pattern.compile("[\\s*|.*]");
        // System.out.println("1: " + p.matcher("sd").find());
        // System.out.println("2: " + p.matcher("\\r\\n").find());
        // System.out.println("3: " + p.matcher(" ").find());
    }

    public String resolveJsRefer(String sjsp, String ver) {
        // String sjsp = TextFile.read("test.jsp");
        /*
         * Matcher matcher =
         * Pattern.compile("<script\\s.*src=.*(\"|\'){1}(.+\\.js)+(\"|\'){1}.*>.*</script>",
         * Pattern.CASE_INSENSITIVE ) // | Pattern.DOTALL .matcher(sjsp);
         */
        Matcher matcher = jsPattern.matcher(sjsp);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String pfull = matcher.group();
            String jsdirPart = matcher.group(1);
            String versionPart = matcher.group(2);
            System.out.println("group(1) js path part :" + jsdirPart);
            System.out.println("group(2) js version part: " + versionPart);
            System.out.println("declare js: " + pfull);
            // if(StringUtils.isNotBlank(versionPart)){
            // pfull = pfull.replace(versionPart, ver);
            // }
            // System.out.println("start 0 :" + matcher.start(0));
            // System.out.println("end 0: " + matcher.end(0));
            // System.out.println("start 1: " + matcher.start(1));
            // System.out.println("end 1: " + matcher.end(1));
            // System.out.println(pfull.substring(0,matcher.start(1)-matcher.start()));
            // System.out.println(pfull.substring(matcher.end(1)-matcher.start(),matcher.start(2)-matcher.start()));
            // System.out.println(pfull.substring(matcher.end(2)-matcher.start()));
            pfull = pfull.substring(0, matcher.start(1) - matcher.start()) + " \"" + jsdirPart.trim().substring(1)
            // + pfull.substring(matcher.end(1)-matcher.start(),matcher.start(2)-matcher.start())
            + ver + "\"" + pfull.substring(matcher.end(2) - matcher.start());
            // pfull = pfull.replace(jsdirPart, resolveStaticAddr(jsdirPart.trim()));
            // System.out.println("match: " + src) ;
            matcher.appendReplacement(sb, pfull);// matcher.group(1) + "2");
        }
        matcher.appendTail(sb);
        System.out.println("js results: " + sb.toString());
        return sb.toString();
    }

    public String resolveCssRefer(String sjsp, String ver) {
        // String sjsp = TextFile.read("test.jsp");
        /*
         * Matcher matcher = Pattern.compile("<link\\s.*href=(\"|\'){1}(.+\\.css)+(\"|\'){1}.*>",
         * Pattern.CASE_INSENSITIVE ) // | Pattern.DOTALL .matcher(sjsp);
         */
        Matcher matcher = cssPattern.matcher(sjsp);
        StringBuffer sb = new StringBuffer();
        while (true) {
            if (!matcher.find()) {
                break;
            }
            String pfull = matcher.group();
            String cssPath = matcher.group(1);
            String cssVer = matcher.group(2);
            System.out.println("group(1) css path part :" + cssPath);
            System.out.println("group(2) css version part: " + cssVer);
            if (StringUtils.isBlank(cssPath)) {
                System.out.println("jsp: " + sjsp);
            }
            System.out.println("cssPath: " + cssPath);
            // pfull = pfull.replace(psome, resolveStaticAddr(psome) + psome.trim() + ver);
            // System.out.println("match: " + src) ;
            pfull = pfull.substring(0, matcher.start(1) - matcher.start()) + "\"" + cssPath.trim().substring(1)
            // + pfull.substring(matcher.end(1)-matcher.start(),matcher.start(2)-matcher.start())
            + ver + "\"  " + pfull.substring(matcher.end(2) - matcher.start());
            matcher.appendReplacement(sb, pfull);// matcher.group(1) + "2");
        }
        matcher.appendTail(sb);
        System.out.println("css results: " + sb.toString());
        return sb.toString();
    }

    private static Pattern imagesPattern = Pattern.compile("[\\(|\'|\"]([\\./]*images/)\\S*[\\)|\'|\"]", Pattern.CASE_INSENSITIVE); // |
                                                                                                                                    // Pattern.DOTALL

    // @Test
    public void testimage() {
        String s = ".th_bg{ background:url(../images/iconexpand.png) repeat-x 0 -87px; height:31px; border-bottom:1px solid #e6e6e6; border-top:1px solid #e6e6e6;}" + ".checking{ width:98px; height:21px; background:url(/images/btn_system.png) 0 -177px; text-align:center;color:#333; border:none; line-height:22px;}" + ".promptly{ width:74px; height:21px; background:url(/images/btn_system.png) 0 -206px; text-align:center; color:#333; border:none; line-height:22px;}" + ".recharge, .recharge:hover{ width:75px; height:31px; background:url(/images/btn_system.png) no-repeat 0 -79px; text-align:center; color:#fff;font:14px/31px \"微软雅黑\",Arial,Lucida,Verdana,Helvetica,sans-serif; display:block; border:none; text-decoration:none;}" + ".carry, .carry:hover{width:75px; height:31px;background:url(/images/btn_system.png) no-repeat -78px -79px; text-align:center; color:#333; font:14px/31px \"微软雅黑\",Arial,Lucida,Verdana,Helvetica,sans-serif;display:block; border:none; text-decoration:none;}" + "" + ".btn_confirm,.btn_confirm:hover{ width:104px; height:31px; background:url(images/btn_system.png) no-repeat 0 -112px;text-align:center; color:#333; font:14px/31px \"微软雅黑\",Arial,Lucida,Verdana,Helvetica,sans-serif;display:block; border:none; text-decoration:none;}";
        String s2 = "<img width='243' border='0' height='64' src=\"/page/site/portal/seller/style1/images/logo.png\">";
        MinMojo mojo = new MinMojo();
        mojo.setStaticAddr("localhost/");
        s = mojo.resolveImagesRefer(s2, "22");
        System.out.println(s);
    }


    public void testinput() throws Exception {
        // String[] x = {"1","2","3"};
        // x[2] = null;
        // System.out.println("length: " + x.length);
        File file = new File("E:\\x.png");
        InputStreamReader isr = new InputStreamReader(new FileInputStream(file));
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file));
        IOUtils.copy(isr, osw);
    }

    @Test
    public void testReplace() {
        System.out.println("xx");
        String s = "js\\jquery-window-503\\jquery.window.js";
        String result = s.replaceAll("\\\\", "/");
        System.out.println("replace: " + result);
    }
}
