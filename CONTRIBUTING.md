Contributing to NoCheatPlus
---------

This page describes how to contribute code to the project by means of pull requests.

Other types of contribution:
* [Issues/Tickets] (https://github.com/NoCheatPlus/Issues/issues) are hosted on GitHub.

Pull Requests
---------

This is a quick go, not covering all the areas.

Do comply with the [GPLv3 License] (https://github.com/NoCheatPlus/NoCheatPlus/blob/master/LICENSE.txt) of the project, ensure contributed code can be used in a GPLv3 project.

Pull request conventions:
* Do provide some reason for a pull request, if that feels better. Currently, we don't demand anything in terms of elaboration and formatting. This may change with receiving more pull requests.
* We prefer you to compile and test code changes.
* Split off larger unrelated chunks of changes into separate pull requests for better discussion.
* Cosmetic changes (e.g. code conventions, not changing the logic) should be a separate PR, so discussion can be focused, especially if those are a matter of taste.

Conventions for commits:
* Related changes should rather be in one commit, especially if the whole would not compile or not make sense with either commit removed. Exception may be splitting extensive amount of changes for readability, larger changes of different sub-systems, provided the pull requests is confined to one overall topic/change.
* Split off formatting larger areas into separate commits. Formatting several files without changing any logic is ok.
* Split off (unrelated) java-doc/comment changes into separate commits.
* In general split off unrelated changes into separate commits.

Code conventions haven't really been followed, however we try to go with some rules and change existing files once touching them.

Indentation, braces, split statements:
* Use spaces, 4 for a level of indentation, no tab characters.
* Curly braces rather like the Stoustrup variant of the K&R indentation style (see wikipedia). Exceptions may be empty bodies for try-catch (compact). Open brace in the same line of a method signature or a condition, close in a separate line, place "else if" on the same level as "if" in a separate line.
* Splitting long lines only when semantically appealing. Don't use magic on splitting. Keep a (too) long line, rather than obfuscating the logic by splitting it.
* Indentation of split lines is out of our reach (use auto-indent), is that even configurable (eclipse)?
* In order to not mess everything up, we use auto-indentation but not auto-format, until decided.

Comments:
* Document special cases and purpose, where necessary, especially for workarounds.
* For special case conditions place a comment between split lines, if there is a lot of cases in one statement.
* Don't format/split single line comments automatically.
* Do use auto-format for java-doc comments. 

Other:
* We do use 'final' modifiers, especially for variables in lengthy method bodies, but also for not-to-be-changed instance members. Occasionally for very often used static methods, rather not for instance methods.
