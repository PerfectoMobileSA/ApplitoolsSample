# Perfecto + Applitools: 

This Sample Maven project will showcase how to integrate Perfecto with Applitools as well as cover some interesting features in both the tools.

# Instructions
Steps to setup & execute the scripts are as follows. 
1. Clone this project.
2. Import this project as an existing maven project and run the following maven goals:

clean install test -Dversion=v1 -DAPPLITOOLS_KEY={key} -DcloudName={cloud name, e.g. demo} -DsecurityToken={Perfecto token} -DtestNGSuite={xml file  e.g. native.xml} 

Note: 
1. APPLITOOLS_KEY should be a valid applitools API key (obviously) :)
2. version can be either v1/v2. Both are different versions of the same demo site.