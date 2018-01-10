NoCheatPlus
---------
NoCheatPlus is a fork of the famous anti-cheat plugin [NoCheat](https://dev.bukkit.org/server-mods/nocheat/) created by [Evenprime](https://github.com/Evenprime). NoCheatPlus attempts to enforce "vanilla Minecraft" mechanics, as well as preventing players from abusing weaknesses in Minecraft or its protocol, making your server more safe. Organized in different sections, various checks are performed to test players doing, covering a wide range including flying and speeding, fighting hacks, fast block breaking and nukers, inventory hacks, chat spam and other types of malicious behaviour. For a more complete list have a look at the always outdated [Features Page](https://github.com/NoCheatPlus/Docs/wiki/Features).

Installation
---------
* Get [CraftBukkit](http://wiki.bukkit.org/Setting_up_a_server) or [Spigot](https://www.spigotmc.org/threads/buildtools-updates-information.42865/)
* Download NoCheatPlus from either [BukkitDev (staff approved)](https://dev.bukkit.org/server-mods/nocheatplus/files/) or [Jenkins (development versions)](https://ci.md-5.net/job/NoCheatPlus/).
* Drop the NoCheatPlus.jar in to your plugins folder.
* Start your Spigot/CraftBukkit server. (Using /reload can have unwanted side effects with players still online, but also with complex plugins and cross-plugin dependencies, so we don't recommend it. Usually it should work with NCP.)

Tips
---------
* Be sure that your Spigot/CraftBukkit and NoCheatPlus versions match together.
* Don't use tabs in the config.yml file.
* Use [ProtocolLib](https://dev.bukkit.org/bukkit-plugins/protocollib) for full efficiency of the fight checks and other. Using version of ProtocolLib that is supported by NCP is essential, as otherwise some checks will be disabled.
* For compatibility with other plugins such as mcMMO, citizens and more check out [CompatNoCheatPlus](https://dev.bukkit.org/server-mods/compatnocheatplus-cncp/).

Compiling NoCheatPlus
---------
* NoCheatPlus used to be compiled with java 6 compliance (note OpenJDK, possibly we'll switch to 8 directly, once appropriate, e.g. with ProtocolLib dropping support for 7.).
* We use [Maven](http://maven.apache.org/download.cgi) 3 to handle the dependencies.
* You can compile with this Maven goal: `mvn clean package`, for a standard build. To also (re-) build "non free" compatibility modules that depend on Spigot or the like, Maven profiles can be chosen. For reference on profiles see the table below. See the NCPCompatNonFree sub module for reference on how to add custom modules. If you add custom modules with a different package naming than `fr.neatmonster`, you might have to add the source inclusion to the NoCheatPlus/pom.xml as well.
* Jar files for the dedicated compat modules, which your local maven repository might be missing, can be installed manually.
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
| _none_ | _none_ | The "non free" modules are included via a pre-built jar file from the repository. |
| -P nonfree_build | _none_ | Enable building "non free" compatibility modules. The NCPCompatNonFree jar file is built/installed/(deployed). |
| _none_ | -Dno_nonfree | The "non free" modules are neither built, nor included via a pre-built jar file. |

Profiles for choice of "non free" compatibility modules to build:

| Profile | Description |
| :------------------ | :-------------- |
| _none_ | Default build without any of the native access modules, might pose compatibility issues with latest Minecraft versions. The reflection based module is included here. |
| `-P all` | All compatibility modules. |
| `-P spigot1_7_r4` | Spigot 1.7 R4 (MC 1.7.10). |
| `-P spigot1_8_r3` | Spigot 1.8 R3 (MC 1.8.4-1.8.8). |
| `-P spigot1_9_r2` | Spigot 1.9 R2 (MC 1.9.4). |
| `-P spigot1_10_r1` | Spigot 1.10 R1 (MC 1.10-1.10.2). |
| `-P spigot1_11_r1` | Spigot 1.11 R1 (MC 1.11-1.11.2). |
| `-P spigot1_12_r1` | Spigot 1.12 R1 (MC 1.12-1.12.2). |
| `-P cbdev` | The latest version in development. |

(On the long run, only the latest module for a major Minecraft release may be be kept, such as 1_8_r3 for all of 1.8.x.)

Links
---------

Project
* [NoCheatPlus at BukkitDev](https://dev.bukkit.org/server-mods/nocheatplus/)

Download
* [BukkitDev (staff approved)](https://dev.bukkit.org/server-mods/nocheatplus/files/)
* [Jenkins (development versions)](https://ci.md-5.net/job/NoCheatPlus/)

Support and Documentation
* [Issues/Tickets](https://github.com/NoCheatPlus/Issues/issues)
* [Wiki](https://github.com/NoCheatPlus/Docs/wiki)
* [Configuration](https://github.com/NoCheatPlus/Docs/wiki/Configuration)
* [Permissions](https://github.com/NoCheatPlus/Docs/wiki/Permissions)
* [Commands](https://github.com/NoCheatPlus/Docs/wiki/Commands)

Developers
* [License (GPLv3)](https://github.com/NoCheatPlus/NoCheatPlus/blob/master/LICENSE.txt)
* [API](https://github.com/NoCheatPlus/Docs/wiki/API)
* [Contribute](https://github.com/NoCheatPlus/NoCheatPlus/blob/master/CONTRIBUTING.md)

TODO: BLEEDING REPO

Related
* [ProtocolLib at BukkitDev](https://dev.bukkit.org/bukkit-plugins/protocollib)
* [CompatNoCheatPlus at BukkitDev](https://dev.bukkit.org/server-mods/compatnocheatplus-cncp/)
