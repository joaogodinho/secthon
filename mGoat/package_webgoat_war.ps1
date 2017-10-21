mvn clean install -DskipTests
mvn -f ../Lockup-Challenges/ clean package -DskipTests
cp ../Lockup-Challenges/target/plugins/*.jar webgoat-container/src/main/webapp/plugin_lessons/
mvn clean package -DskipTests
cp webgoat-container/target/webgoat-container-7.1.war ../WebGoat.war