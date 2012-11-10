# Running

- check the gcuser.properties. You need to change a few fields.
- the parse.gsuserid can be determined by your profile page: http://www.geocaching.com/profile/
  its on the end of the Link "See the Forum Posts for This User": ...CODE=getalluser&amp;mid=<your gs user id>
- the parse speed can be change - look into the listingparser.properties under "parse.sleep"
  it defaults to 15 seconds
- if you logged pm-only caches, these logs can be accessed currently only with a premium member account
  so maybe you ask your friends for a login.
- There's currently a bug when you have new caches which haven't been spiders in list mode first and then
  switching immediatly to the pm-account. So first spider all list pages with you own account and after
  the tool has filled all the non-pm caches in the listingdata.xls, you can switch to your friends pm-account ...


# Developing

If you want to use maven to compile the project, you need to manually install jtidy,
as the official available maven version of it is out of date.

Either use the jtidy-r938.jar in the lib directory or get a fresh copy from
http://sourceforge.net/projects/jtidy/files/

mvn install:install-file -Dfile=jtidy-r938.jar -DgroupId=jtidy -DartifactId=jtidy -Dversion=r938 -Dpackaging=jar -DgeneratePom=true