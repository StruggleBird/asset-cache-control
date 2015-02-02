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
			<version>1.0.0</version>
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
###其他配置：
resourcesURL：定义资源URL前缀
suffixs：文件后缀，允许多个，用于声明哪些后缀的文件可以做打版本操作，默认支持jsp、html、htm、ftl，如果填写则覆盖默认文件后缀，只会处理指定的文件后缀
version：版本号，给资源url添加的版本号，如果为空，则打上当前时间戳
resourcesDirs：待处理的资源目录，允许多个。如果指定，则只打当前指定目录下面的文件到静态资源包中


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

版本号模式
```html
	<script type="text/javascript" src="http://res.github.com/javascripts/jquery-1.10.2.min.js?v=1.1.0"></script>
	<link href="http://res.github.com/css/bootstrap.min.css?v=1.1.0" rel="stylesheet">
```

时间戳模式
```html
  <script type="text/javascript" src="http://res.github.com/javascripts/jquery-1.10.2.min.js?v=14298124845"></script>
  <link href="http://res.github.com/css/bootstrap.min.css?v=14298124845" rel="stylesheet">
```

