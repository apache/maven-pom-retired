import continuum
import sys

baseurl = "scm:svn:http://svn.apache.org/repos/asf/directory"
configuration = {}

continuum.addProjectFromScm( baseurl + "/apacheds/trunk", "maven-1", "", "nobody@localhost", "1.0", configuration )

sys.exit()

continuum.addProjectFromScm( baseurl + "/apacheds/trunk/core", "maven-1", "", "nobody@localhost", "1.0", configuration )
continuum.addProjectFromScm( baseurl + "/apacheds/trunk/main", "maven-1", "", "nobody@localhost", "1.0", configuration )
continuum.addProjectFromScm( baseurl + "/apacheds/trunk/plugin", "maven-1", "", "nobody@localhost", "1.0", configuration )
continuum.addProjectFromScm( baseurl + "/apacheds/trunk/shared", "maven-1", "", "nobody@localhost", "1.0", configuration )

continuum.addProjectFromScm( baseurl + "/asn1/trunk/ber", "maven-1", "", "nobody@localhost", "1.0", configuration )
continuum.addProjectFromScm( baseurl + "/asn1/trunk/codec", "maven-1", "", "nobody@localhost", "1.0", configuration )
continuum.addProjectFromScm( baseurl + "/asn1/trunk/der", "maven-1", "", "nobody@localhost", "1.0", configuration )
continuum.addProjectFromScm( baseurl + "/asn1/trunk/stub-compiler", "maven-1", "", "nobody@localhost", "1.0", configuration )

continuum.addProjectFromScm( baseurl + "/authx/trunk/core", "maven-1", "", "nobody@localhost", "1.0", configuration )
continuum.addProjectFromScm( baseurl + "/authx/trunk/example", "maven-1", "", "nobody@localhost", "1.0", configuration )
continuum.addProjectFromScm( baseurl + "/authx/trunk/jdbc", "maven-1", "", "nobody@localhost", "1.0", configuration )
continuum.addProjectFromScm( baseurl + "/authx/trunk/script", "maven-1", "", "nobody@localhost", "1.0", configuration )

