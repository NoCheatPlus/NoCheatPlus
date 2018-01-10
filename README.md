NoCheatPlus
---------
NoCheatPlus is a fork of the famous anti-cheat plugin [NoCheat](https://dev.bukkit.org/server-mods/nocheat/) created by [Evenprime](https://github.com/Evenprime). NoCheatPlus attempts to enforce "vanilla Minecraft" mechanics, as well as preventing players from abusing weaknesses in Minecraft or its protocol, making your server more safe. Organized in different sections, various checks are performed to test players doing, covering a wide range including flying and speeding, fighting hacks, fast block breaking and nukers, inventory hacks, chat spam and other types of malicious behaviour. For a more complete list have a look at the always outdated [Features Page](https://github.com/NoCheatPlus/Docs/wiki/Features).

Installation
---------
* [Install a Spigot server](https://github.com/NoCheatPlus/NoCheatPlus/#obtain-a-build-of-spigot)
* [Download NoCheatPlus](https://github.com/NoCheatPlus/NoCheatPlus/#download)
* Drop the NoCheatPlus.jar in to the plugins folder.
* Start your Spigot/CraftBukkit server. (Using /reload can have unwanted side effects with players still online, but also with complex plugins and cross-plugin dependencies, so we don't recommend it. Usually it should work with NCP.)

Hints
---------
* Be sure that your Spigot/CraftBukkit and NoCheatPlus versions match together. The latest version of NCP is compatible with a wide range of CraftBukkit/Spigot versions ([See the notable builds page for a quick overview.]()).
* Don't use tabs in the config.yml file.
* Use [ProtocolLib](https://dev.bukkit.org/bukkit-plugins/protocollib) for full efficiency of the fight checks and other. Using a version of ProtocolLib that is supported by NCP is essential, as otherwise some checks will be disabled.
* For compatibility with other plugins such as mcMMO, citizens and more check out [CompatNoCheatPlus](https://dev.bukkit.org/server-mods/compatnocheatplus-cncp/).

Links
---------

###### Project
* [NoCheatPlus at SpigotMC](https://www.spigotmc.org/resources/nocheatplus2015-07-25.26/)
* [NoCheatPlus at BukkitDev](https://dev.bukkit.org/server-mods/nocheatplus/)

###### Download
* [BukkitDev (approved by staff)](https://dev.bukkit.org/server-mods/nocheatplus/files/)
* [SpigotMC (not inspected by staff)](https://www.spigotmc.org/resources/nocheatplus2015-07-25.26/updates)
* [Jenkins (all builds including development versions)](https://ci.md-5.net/job/NoCheatPlus/)

###### Support and Documentation
* [Issues/Tickets](https://github.com/NoCheatPlus/Issues/issues)
* [Wiki](https://github.com/NoCheatPlus/Docs/wiki)
* [Configuration](https://github.com/NoCheatPlus/Docs/wiki/Configuration)
* [Permissions](https://github.com/NoCheatPlus/Docs/wiki/Permissions)
* [Commands](https://github.com/NoCheatPlus/Docs/wiki/Commands)

###### Developers
* [License (GPLv3)](https://github.com/NoCheatPlus/NoCheatPlus/blob/master/LICENSE.txt)
* [API](https://github.com/NoCheatPlus/Docs/wiki/API)
* [Contribute](https://github.com/NoCheatPlus/NoCheatPlus/blob/master/CONTRIBUTING.md)

###### Related Plugins
* [ProtocolLib at BukkitDev](https://dev.bukkit.org/bukkit-plugins/protocollib)
* [CompatNoCheatPlus at BukkitDev](https://dev.bukkit.org/server-mods/compatnocheatplus-cncp/)

###### Obtain a build of Spigot
* [Get the latest BuildTools.jar](https://hub.spigotmc.org/jenkins/job/BuildTools/)
* [Run according to instructions](https://www.spigotmc.org/wiki/buildtools/)
* ([Server installation instructions](https://www.spigotmc.org/wiki/spigot-installation/))

Compiling NoCheatPlus
---------
* NoCheatPlus used to be compiled with java 6 compliance (note OpenJDK, possibly we'll switch to 8 directly, once appropriate, e.g. with ProtocolLib dropping support for 7.).
* We use [Maven](http://maven.apache.org/download.cgi) 3 to handle the dependencies. Tested both with Eclipse and Jenkins is Maven 3.3.9.
* You can compile with this Maven goal: `mvn clean package`, for a build without any of the "non free" modules, which depened on not publicly downloadable resources, such as the CraftBukkit/Spigot server jar - the reflection based compatibility module is still contained. 
* In order to include a pre-built jar with the "non free" modules contained, use `-P nonfree_include`.
* To also (re-) build "non free" compatibility modules, use `-P nonfree_build` as well as activating the appropriate module to build via a profile such as `-P cbdev` - see the tables below for reference.
* Have a look at the NCPCompatNonFree sub module for reference on how to add custom compatibility modules. If you add custom modules with a different package naming than `fr.neatmonster`, you might have to update the includes section within  NCPCompatNonFreeJar as well. The related factories are located within the NCPPlugin module (e.g. MCAccessFactory).
* "Non free" jar file dependencies needed for the dedicated compat modules, which your local maven repository might be missing, can be installed manually.
Example for Eclipse with embedded maven:
Add a new maven build run configuration, name it appropriately, e.g. ```Install CB 1.7.5```.
Set goals to: ```install:install-file -Dfile=<PATH TO JAR> -DgroupId=org.bukkit -DartifactId=craftbukkit -Dversion=1.7.5-R0.1-SNAPSHOT -Dpackaging=jar```
On Windows the <PATH TO JAR> might look like:  ```X:\...\craftbukkit\3042\craftbukkit-1.7.5-R0.1-20140408.020329-16.jar```
To let it run you might have to set the base directory, e.g. to ```${workspace_loc}```, it does not seem to have significance.
Do set the correct version alongside the file name. On newer version of maven, you might do with much simplified goals, because the pom files inside the jars are parsed.
  * **The latest versions of BuildTools.jar will automatically install the created server jars into the local .m2 repository (e.g. on linux) - provided configuration paths are standard. Thus you don't need to do this manually anymore, if you then build NCP with the specific profile, if you have run BuildTools.jar to generate the server jars on that machine/environment.**

Options and profiles related to enabling/disabling including/building "non free" compatibility modules.

| Profile | Parameter | Description |
| :------------------ | :-------------- | :-------------- |
| `-P nonfree_include` | _none_ | The "non free" modules are included via a pre-built jar file from the repository. |
| `-P nonfree_build` | _none_ | Enable building "non free" compatibility modules. The NCPCompatNonFree jar file is built/installed/(deployed). |
| _none_ | _none_ | The "non free" modules are neither built, nor included via a pre-built jar file. |

Profiles for choice of "non free" compatibility modules to build:

| Profile | Description |
| :------------------ | :-------------- |
| _none_ | Default build without any of the native access modules, might pose compatibility issues with latest Minecraft versions. The reflection based module is included here. |
| `-P all` | All compatibility modules. |
| `-P cbdev` | The latest version in development. |
| `-P spigot1_12_r1` | Spigot 1.12 R1 (MC 1.12-1.12.2). |
| `-P spigot1_11_r1` | Spigot 1.11 R1 (MC 1.11-1.11.2). |
| `-P spigot1_10_r1` | Spigot 1.10 R1 (MC 1.10-1.10.2). |
| `-P spigot1_9_r2` | Spigot 1.9 R2 (MC 1.9.4). |
| `-P spigot1_8_r3` | Spigot 1.8 R3 (MC 1.8.4-1.8.8). |
| `-P spigot1_7_r4` | Spigot 1.7 R4 (MC 1.7.10). |
| `-P cblegacy` | The pre-DMCA CraftBukkit builds. |

(On the long run, only the latest module for a major Minecraft release may be be kept, such as 1_8_r3 for all of 1.8.x.)
