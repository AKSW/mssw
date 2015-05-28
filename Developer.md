In the following text _$CHECKOUT\_PATH_ stands for the path to the mercurial-folder to which you have cloned the mssw-repository. It contains four folders: libs, logo, mssw and msw.

# What you need #
  * [Eclipse](http://www.eclipse.org/downloads/) (optional but recommended)
  * [Android SDK](http://developer.android.com/sdk/index.html)
  * [MSSW Source](http://code.google.com/p/mssw/source/checkout)

# Setting up the Environment #
After [installing Eclipse, Android SDK and configuring it both](http://developer.android.com/sdk/installing.html) you should checkout the MSSW Sources

## Creating a new Project ##
Select "File > New > Project..." in the dialog, which appears select "Android > Android Project" and press "Next >". In the next dialog enter as "Project name" "msw" and select "Create project from existing source" and select "_$CHECKOUT\_PATH/msw_" it should adjust the Build Target automatically, press "Finish" because we don't need a Test Project at this point. Repeat this process but now with "mssw" as "Project name" and select "_$CHECKOUT\_PATH/mssw_" as source.

## Configure Build Path ##
Right-click on the "msw"-project and select "Build Path > Configure Build Path..." from the context-menu. Select the tab called "Libraries" and push the button "Add External JARs...". Select all .jar-Files from the directory "_$CHECKOUT\_PATH/libs_" and press "OK" and again "OK".

Now go on with "mssw"-project and open the Build Path Configuration dialog. But select the tab "Projects" in this case and push the button "Add...", check the box in front of "msw" and press "OK" and again "OK".

# Building and Running the App #
Now you can try to build and run the Application

## Installing Platforms ##
Is described in [Adding Platforms and Other Components](http://developer.android.com/sdk/installing.html#AddingComponents) and [Adding SDK Components](http://developer.android.com/sdk/adding-components.html)

## Creating a new Virtual Device ##
Select "Window > Android SDK and AVD Manager" click "New..." and give the new Android Virtual Device (AVD) a name, as target select a version above 2.1 (API Level 7) to test MSSW, select a SD Card size as you like.
You can create more AVDs as you need.

## Run the App ##
Select the mssw-project and click on the green play-button (green circle with a white right arrow) in the Eclipse-toolbar. In the window "Run As" select "Android Application" and wait while it is booting the AVD. If you have multiple AVDs it automatically selects a device which fits the required API level. But you can also select it manually by selecting "Run > Run Configurations..." from the main menu, choose the "Target" tab and set it to "Manual", click "Apply" and "Run" if you want to run it, else "Close".


# Additional Information #
Read also about the [Android Debug Bridge](http://developer.android.com/guide/developing/tools/adb.html) (ADB) especially about [logcat](http://developer.android.com/guide/developing/tools/adb.html#logcat)

## Troubleshooting ##

If you experience problems with building the project deactivate "Project > Build Automatically" and build the project manually by right-clicking on the project and selecting "Build Project".

If you get an error saying something like dx is null you should increase the memory which is used by eclipse by setting "-Xms1024m" (or higher) in the "eclipse.ini".

If you've just imported the project into eclipse and you have trouble with @Override: goto "Project > Properties > Java Compiler" and change "Compiler compliance level" to "1.6". See also [on stackoverflow](http://stackoverflow.com/questions/1678122/must-override-a-superclass-method-errors-after-importing-a-project-into-eclipse).

If you have more problems read [The Developer's Guide](http://developer.android.com/guide/index.html) and search on [Stack Overflow](http://stackoverflow.com/).