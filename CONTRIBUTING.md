Contributing to NoCheatPlus
---------

This page describes how to contribute code to the project by means of pull requests.

Other types of contribution:
* [Tickets (all sorts)] (http://dev.bukkit.org/server-mods/nocheatplus/tickets/) are currently hosted on BukkitDev, but will be migrated to GitHub later on.
* [Tickets for the Wiki] (https://github.com/NoCheatPlus/Docs/issues) are on GitHub, these are only for the purpose of improving the Wiki itself.

Pull Requests
---------

This is a quick go, not covering all the areas.

Do comply with the [GPLv3 License] (https://github.com/NoCheatPlus/NoCheatPlus/blob/master/LICENSE.txt) of the project, ensure contributed code can be used in a GPLv3 project.

Code conventions haven't really been followed, however we try to go with some rules and change existing files once touching them.

Indentation, braces, split statements:
* Use spaces, 4 for a level of indentation, no tab characters.
* Curly braces rather like the Stoustrup variant of K&R indentation style (see wikipedia). Exceptions may be empty bodies for try-catch (compact). Open brace in the same line of a method signature or a condition, close in an extra line, place "else if" on the same level as "if" in an extra line.
* Splitting long lines only when semantically appealing. Don't use magic on splitting. Keep a (too) long line, rather than obfuscating the logic by splitting it.
* Indentation of split lines is out of our reach (use auto-indent), is that even configurable (eclipse)?
* In order to not mess everything up, we use auto-indentation but not auto-format, until decided.

* Comments:
* Document special cases and purpose, where necessary, especially for workarounds.
* For special case conditions place a comment between split lines, if there is a lot of cases in one statement.
* Don't format/split single line comments automatically.

Other:
* We do use "final" modifiers, especially for variables in lengthy method bodies, but also for not-to-be-changed instance members. Occasionally for very often used static methods, rather not for instance methods.
