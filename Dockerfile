FROM eclipse-temurin:17

COPY . .

RUN mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"

CMD ['mvn', 'test', "--file pom.xml"]
