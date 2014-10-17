from controlscript_addon import *

ControlScript([
	JavaProcess( "PlayBack-Interface",
				 mainClassOrJar="com.qspin.qtaste.sutuidemo.Interface",
				 workingDir="../demo",
				 classPath="../kernel/target/qtaste-kernel-deploy.jar:testapi/target/qtaste-testapi-deploy.jar",		
				 jmxPort=10101,
				 vmArgs="-Duser.language=en -Duser.region=EN",
				 checkAfter=5,
				 useJavaGUI=True,
				 useJacoco=True),
	JavaProcess( "PlayBack-InterfaceBis",
				 mainClassOrJar="com.qspin.qtaste.sutuidemo.Interface",
				 workingDir="../demo",
				 classPath="../kernel/target/qtaste-kernel-deploy.jar:testapi/target/qtaste-testapi-deploy.jar",		
				 jmxPort=10102,
				 vmArgs="-Duser.language=en -Duser.region=EN",
				 checkAfter=5,
				 useJavaGUI=True,
				 useJacoco=True),
])
