# ~ coding: UTF-8 ~
'''
- Find files that don't start with the GPLv3 header.
- As a secondary product, count files and size and lines.
- Add a GPLv3 header to each .java file where it may be missing.
@license: See #LICENSE below.
'''
from cgitb import text

LICENSE = """/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */"""

import sys
import os

class ResultCollector:
    
    def __init__(self, lc_exts = (".java",), ignore_startswith = ()):
        self.lc_exts = tuple(map(lambda x: x.lower(), lc_exts))
        self.max_len_inspect = max(map(len, ignore_startswith)) + 50 if ignore_startswith else 0
        self.ignore_startswith = tuple(map(self.transform_special, ignore_startswith))
        self.collected_files = []
        self.not_collected_files = 0
        self.lines_total = 0
        self.bytes_total = 0
    
    def transform_special(self, s):
        """
        Transform string for comparison in a special way.
        """
        for remove in ("\r\n", "\n", "\r", " ", "\t"):
            s = s.replace(remove, "")
        return s
    
    def check_ignore_startswith(self, content):
        """
        Return True if the content starts with any of the ignore_startswith parts.
        """
        content = self.transform_special(content[:self.max_len_inspect])
        for ref in self.ignore_startswith:
            if content.startswith(ref):
                return True
        return False
        
    def stats_str(self):
        collected = len(self.collected_files)
        return "collected=" + repr(collected) + " total_files=" + repr(collected + self.not_collected_files) + " total_lines=" + repr(self.lines_total) + " total_bytes=" + repr(self.bytes_total)
    806895
    
    def collect_filename(self, fp):
        """
        Only "collect" if the file doesn't start with 'ignore_startswith'.
        """
        f = open(fp, "rb")
        c = f.read().decode("UTF-8") # Assume UTF-8 sources.
        f.close()
        if self.check_ignore_startswith(c):
            self.not_collected_files += 1
        else:
            self.collected_files.append(fp)
        self.bytes_total += len(c)
        if c:
            self.lines_total += 1 + c.count("\n")

    def collect_filenames(self, path):
        """
        Recursively collect full paths of files with lower case extension in lc_exts,
        not starting with ignore_startswith.
        """
        items = os.listdir(path)
        for item in items:
            fp = os.path.join(path, item)
            if os.path.islink(fp):
                continue
            if os.path.isfile(fp) and os.path.splitext(item)[1].lower() in self.lc_exts:
                self.collect_filename(fp)
            elif os.path.isdir(fp):
                self.collect_filenames(fp)

def main():
    path = os.path.abspath(os.path.normpath(os.path.split(sys.argv[0])[0]))
    print("Working on path: " + path)
    
    items = os.listdir(path)
    license = unicode(LICENSE)
    files_java = ResultCollector(lc_exts = (".java",), ignore_startswith = (license,))
    for item in items:
        fp = os.path.join(path, item)
        if os.path.isdir(fp) and not os.path.islink(path) and item.startswith("NCP") or item == "NoCheatPlus":
            files_java.collect_filenames(fp)
            # TODO: Also collect other contained file types (pom.xml, ...).
    print(".java: " + files_java.stats_str())
    print("Update license:")
    for fp in files_java.collected_files:
        rp = os.path.relpath(fp, path)
        f = open(fp, "rb")
        c = f.read().decode("UTF-8")
        f.close()
        c = license + "\n" + c
        f = open(fp, "wb")
        f.write(c)
        f.close()
        print(rp)

if __name__ == "__main__":
    main()