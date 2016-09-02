# asset-cache-control
###基于maven插件的缓存控制工具，通过修改资源url的请求参数，比如在url后面添加版本号或者时间戳的形式，来有效的防止浏览器缓存。

目前该功能可用于避免js、css、image 三种文件类型缓存

###用法：

1.添加插件asset-cache-control 到pom文件中：

```xml
<build>
	<plugins>
		<plugin>
			<groupId>org.zt</groupId>
			<artifactId>asset-cache-control</artifactId>
			<version>1.0.2</version>
			<executions>
				<execution>
					<id>version</id>
					<phase>prepare-package</phase>
					<goals>
						<goal>version</goal>
					</goals>
				</execution>
			</executions>
		</plugin>
	</plugins>
</build>
```
####其他配置：

 - ***resourcesURL***：可选，定义资源URL前缀，如果不填，则不修改前缀
 - ***suffixs***：动态模板后缀，可选，允许多个，用于指定哪些后缀的动态模板可以做打版本号操作，默认支持jsp、html、htm、ftl，如果填写则覆盖默认文件后缀，只会处理填写的文件后缀
 - ***version***：版本号，可选，用于指定给静态资源url添加的版本号值，如果为空，则打上当前时间戳，也可以指定${project.version}即可
 - ***webappDir***:webapp根目录,可选,默认为src/main/webapp，如果不是当前目录，则需要指定
 - ***pageDirs***:webapp目录下，动态模板的目录。可选 ，允许多个 。默认：/ 即webapp目录 
 - ***resourcesDirs***：资源目录，允许多个。如果指定，则只打webappDir/resourcesDir目录下面的文件到静态资源包中


2.执行命令：
执行maven命令，用来替换工程中所有的动态文件中引用的静态资源URL路径。
```html
打版本命令： mvn asset-cache-control:version

打静态资源到独立war包命令：mvn asset-cache-control:package
```

该命令会自动添加版本号或者时间戳到静态资源URL后面，自动添加静态资源域名在url前面（如果有配置静态资源域名），例如 ：

原始：
```html
<script type="text/javascript" src="/javascripts/jquery-1.10.2.min.js"></script>
<link href="/css/bootstrap.min.css" rel="stylesheet">
```

执行后效果：

版本号模式(在版本号不变更的情况下发版，需要刷新浏览器端缓存，所以版本号的规则是"${project.version}-5位随机字母或数字")
```html
	<script type="text/javascript" src="http://res.github.com/javascripts/jquery-1.10.2.min.js?v=1.1.0-2543d"></script>
	<link href="http://res.github.com/css/bootstrap.min.css?v=1.1.0-2543d" rel="stylesheet">
```

时间戳模式
```html
  <script type="text/javascript" src="http://res.github.com/javascripts/jquery-1.10.2.min.js?v=14298124845"></script>
  <link href="http://res.github.com/css/bootstrap.min.css?v=14298124845" rel="stylesheet">
```

**示例：**

    <plugin>
		<groupId>org.zt</groupId>
		<artifactId>asset-cache-control</artifactId>
		<version>1.0.2</version>
		<executions>
			<execution>
				<id>version</id>
				<phase>prepare-package</phase>
				<goals>
					<goal>version</goal>
				</goals>
				<configuration>
					<!-- 版本号，给资源url添加的版本号，如果为空，则打上当前时间戳 -->
					<version>${project.version}</version>
				</configuration>
			</execution>
			<execution>
				<id>package</id>
				<phase>prepare-package</phase>
				<goals>
					<goal>package</goal>
				</goals>
				<configuration>
					<!-- 版本号，给资源url添加的版本号，如果为空，则打上当前时间戳 -->
					<version>${project.version}</version>
					<resourcesDirs>
						<resourcesDir>css</resourcesDir>
						<resourcesDir>images</resourcesDir>
						<resourcesDir>js</resourcesDir>
					</resourcesDirs>
				</configuration>
			</execution>
		</executions>
	</plugin>

**打上版本号：**
将以上配置添加到web工程根目录下的pom 文件的plugins节点中 ，然后执行 `mvn asset-cache-control:version` 命令，则该web工程src/main/webapp目录下的动态模板文件例如jsp文件的内容将会发生改变。这些文件中静态资源的链接地址后面会添加一个v参数 ，这说明我们的命令执行成功了。

**打包：**
接着执行`mvn package`打包命令，将会把修改后的文件打入war包中，该war包发布到线上时已经达到我们的目的了——去除浏览器缓存。

**静态资源独立包**
执行`mvn asset-cache-control:package` 后 ，将会看到工程输出目录target下面会产生一个新的war包 xxx-resources-xxx.war ,该war包中将只存放我们在上面配置中指定目录下的文件 css 、js、images 等 。