# ZaJavaNIO
Java NIO

Refer to:
[Java NIO？看这一篇就够了！](https://blog.csdn.net/forezp/article/details/88414741)

java -cp target/za-java-nio-0.0.1-SNAPSHOT.jar info.ziang.nio.ZaSocketClientNIO
java -cp target/za-java-nio-0.0.1-SNAPSHOT.jar info.ziang.netty.NettyClient

Netty 材料：
- https://www.jianshu.com/p/a4e03835921a
- https://blog.csdn.net/wangmx1993328/article/details/83036285


# Maven

<!-- Maven Assembly Plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <version>2.4.1</version>
                <configuration>
                    <!-- get all project dependencies -->
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>

                    <!-- MainClass in mainfest make a executable jar -->
                    <archive>
                        <manifest>
                            <mainClass>info.ziang.netty.NettyClient</mainClass>
                        </manifest>
                    </archive>
                </configuration>

                <executions>
                    <execution>
                        <id>make-assembly</id>
                        <!-- bind to the packaging phase -->
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>