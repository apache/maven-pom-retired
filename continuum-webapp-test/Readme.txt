Test Continuum with Tomcat 5.x and firefox
    'mvn clean install' or 'mvn clean install -Ptomcat5x,firefox'

Test Continuum with Tomcat 5.x and Internet Explorer
    'mvn clean install -Ptomcat5x,iexplore'

Test Continuum with Tomcat 5.x and a specific browser
    'mvn clean install -Ptomcat5x,otherbrowser -DbrowserPath=PATH_TO_YOUR_BROWSER'

Test Continuum with Tomcat 5.x and firefox wherein your firefox executable is not in the default installation directory
    'mvn clean install' or 'mvn clean install -Ptomcat5x,firefox -Dbrowser="*firefox <full path of firefox executable>'

Test Continuum with Tomcat 5.x and Internet Explorer wherein your Internet Explorer executable is not in the default installation directory
    'mvn clean install' or 'mvn clean install -Ptomcat5x,firefox -Dbrowser="*iexplore <full path of Internet Explorer executable>'

WARNING: If you specify your own custom browser, it's up to you to configure it correctly. At a minimum, you'll need to configure your browser to use the Selenium Server as a proxy, and disable all browser-specific prompting.
http://release.openqa.org/selenium-remote-control/nightly/doc/java/com/thoughtworks/selenium/DefaultSelenium.html#DefaultSelenium(java.lang.String,%20int,%20java.lang.String,%20java.lang.String)
