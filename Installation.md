# Installation #

![http://mssw.googlecode.com/hg/mssw/res/drawable-mdpi/icon.png](http://mssw.googlecode.com/hg/mssw/res/drawable-mdpi/icon.png)
**FOAF/WebID Provider & Browser** (a.k. MSSW, mssw.apk)

## Install from Market ##
  1. Search for "aksw"
  1. install ["FOAF/WebID Provider & Browser"](https://market.android.com/details?id=org.aksw.mssw&feature=search_result)

## Download from Project-Homepage ##

  1. Download the packages mssw.apk from the [Downloads-Section](http://code.google.com/p/mssw/downloads/list)
  1. Install it on your mobile device

NOTE: Since version 0.9.5-unstable-8 there is no "Semantic Web Core"(a.k. as msw) package anymore. If you have still installed msw remove it. "FOAF/WebID Provider & Browser" (a.k. as mssw) works now on its own.
You can remove msw with following command over adb:
```
adb uninstall org.aksw.msw
```

NOTE: If you get problems installing the version from Market or a version higher than 0.9.5 and you had installed a version lower than 0.9.5 from the [Downloads-Section](http://code.google.com/p/mssw/downloads/list), than it is possible that you have to remove the application before updating.
You can do this with following command over adb:
```
adb uninstall org.aksw.mssw
```
But don't forget to re-install it after this step.