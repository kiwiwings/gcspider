# Setup
- rename src\main\resources\gcuser-dist.properties to src\main\resources\gcuser.properties
- rename src\test\resources\ListingData-dist.xls to src\test\resources\ListingData.xls
- check the gcuser.properties. You need to change a few fields.
- the parse.gsuserid can be determined by your profile page: http://www.geocaching.com/profile/
  its on the end of the Link "See the Forum Posts for This User": ...CODE=getalluser&amp;mid=<your gs user id>
- currently the jsoup-Parser is hard-coded in the main class ... in case you want to use the
  htmlcleaner variant, you'll need to remove that line of code

# Running

- the main method is in the class de.kiwiwings.gccom.ListingParser.GCSpider
- the parse speed can be change - look into the listingparser.<parser>.properties under "parse.sleep"
  it defaults to 10 seconds
- if you logged pm-only caches, these logs can be accessed currently only with a premium member account
  so maybe you ask your friends for a login.
- There's currently a bug when you have new caches which haven't been spiders in list mode first and then
  switching immediatly to the pm-account. So first spider all list pages with your own account and after
  the tool has filled all the non-pm caches in the listingdata.xls, you can switch to your friends pm-account ...

# other infos

- the chart for your hides stats can be found under src\test\resources\hides_chart.html

# how it works

The spider tool has the followings process steps:
- login
- iterate over the finds list page (www.geocaching.com/seek/nearest.aspx?ul=<your nick>) and 
  update the general information in the database -
  you can configure which index-pages to be spidered, in case you forgot to log a cache
  for quite some time ... usually it just checks your first page
- scan the database for empty detailed informations or out-of-date entries
  fetch the corresponding detail pages (http://www.geocaching.com/seek/cache_details.aspx?wp=<waypoint>)
- iterate over the log-entries and find your log
- iterate over the hides list page (www.geocaching.com/seek/nearest.aspx?u=<your nick>) and
  compare the last logged date with the entries in the database
- fetch the corresponding detail pages
- iterate over finds/hides database entries to scan for missing groundspeak user ids and
  fetch it from the user pages (http://www.geocaching.com/profile/?guid=<guid of user from log-entry>)
- get some gc-vote data of out-of-date entries
- generate a google static charts link of your hides stats and save it to src\test\resources\hides_chart.html
