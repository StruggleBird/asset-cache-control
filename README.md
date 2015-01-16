# asset-cache-control
###基于maven插件的缓存控制工具，通过修改资源url的参数，比如添加版本号或者hash参数的形式，有效的防止浏览器缓存

目前该功能可用于避免js、css、image 三种文件类型缓存

###用法：

1.添加插件asset-cache-control 到pom文件中：

```xml
<build>
	<plugins>
		<plugin>
			<groupId>org.zt</groupId>
			<artifactId>asset-cache-control</artifactId>
			<version>0.0.2</version>
			<executions>
				<execution>
					<id>version</id>
					<phase>prepare-package</phase>
					<goals>
						<goal>version</goal>
					</goals>
					<configuration>
						<!-- 后缀 -->
						<suffixs>
							<suffix>jsp</suffix>
						</suffixs>
						<!-- 需要替换静态路径文件的目录 -->
						<!-- <version>${project.version}</version> -->
						<!-- 需要打包的静态资源目录 -->
						<resourcesDirs>
							<resourcesDir>css</resourcesDir>
							<resourcesDir>font</resourcesDir>
							<resourcesDir>html</resourcesDir>
							<resourcesDir>images</resourcesDir>
							<resourcesDir>js</resourcesDir>
						</resourcesDirs>
					</configuration>
				</execution>
			</executions>
		</plugin>
	</plugins>
</build>
```


2.执行命令：
执行maven命令，用来替换工程中所有的动态文件中引用的静态资源URL路径。
```html
mvn asset-cache-control:flushcache 
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
  <script type="text/javascript" src="http://res.github.com/javascripts/jquery-1.10.2.min.js?t=14298124845"></script>
  <link href="http://res.github.com/css/bootstrap.min.css?t=14298124845" rel="stylesheet">
```

