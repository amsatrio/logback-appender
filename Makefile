github_release:
	gh release create v0.0.2 ./target/logback-appender-0.0.2.jar --title "v0.0.2" --notes "Second Release"
maven_deploy:
	mvn clean deploy
maven_local_install:
	mvn clean install
maven_test:
	mvn clean test
maven_package:
	mvn clean package
