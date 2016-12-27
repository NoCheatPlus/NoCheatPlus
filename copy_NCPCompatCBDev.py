'''
LICENSE:
This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  
CONTENT:
    * Rude script to create a new dedicated compatibility module using NCPCompatCBDev.
    * Currently missing:
        * MCAccessFactory entry.
        * EntityAccessFactory entry.
        * AttributeAccessFactory entry.
        * Update root+NCPPlugin pom (module/dependency with profile all 
            + point an existing profile for this version to the new module).
'''

import sys
import os
import shutil
import traceback


def is_ok_dir(path):
    """
    Is a directory and not a link.
    """
    return os.path.isdir(path) and not os.path.islink(path)

def replace(item, repl_def):
    for s, r in repl_def:
        item = item.replace(s, r)
    return item

def copy_and_replace_content(full_src, full_dst, repl_content):
    # Read.
    f = open(full_src, "rb")
    content = f.read() # .decode("utf-8")
    f.close()
    # TODO: Above leaks on errors (suggested use is command line).
    content = replace(content, repl_content)
    f = open(full_dst, "w")
    f.write(content)
    f.close()
    # TODO: Above leaks on errors (suggested use is command line).

def copy_and_replace(src_dir, dst_dir, repl_filename, repl_content, 
                     filter_filename = ("target", ), filter_ext = (".class",)):
    """
    All exact case replacements.
    @param src_dir: 
    @param dst_dir:
    @param repl_filename: Also includes directories.
    @param repl_content: 
    @param filter_filename: Filter for file names and directories (exact match).
    @param filter_extension: filter for extensions (exact match).
    """
    names = os.listdir(src_dir)
    for name in names:
        full_src = os.path.join(src_dir, name)
        if name in filter_filename or os.path.splitext(name)[1] in filter_ext:
            print("Skip filter: " + full_src)
            continue
        if os.path.islink(full_src):
            print("[WARNING] Skip link: " + full_src)
            continue
        repl_name = replace(name, repl_filename)
        full_dst = os.path.join(dst_dir, repl_name)
        if os.path.exists(full_dst):
            raise RuntimeError("Destination should not exist: " + full_dst)
        if os.path.isdir(full_src):
            os.mkdir(full_dst)
            copy_and_replace(full_src, full_dst, repl_filename, repl_content)
        elif os.path.isfile(full_src):
            copy_and_replace_content(full_src, full_dst, repl_content)
        else:
            print("[WARNING] Not a supported type: " + full_src)

def main_interactive(path):
    """
    Locate source (NCPCompatCBDev) and ask for the name to rename things to.
    @param path: The NoCheatPlus root project directory.
    """
    if is_ok_dir(path):
        print("Path: " + path)
    else:
        print("[ERROR] Bad path: " + path)
        return False
    # Locate NCPCompatCBDev
    src_name = "CBDev"
    src_dir = os.path.join(path, "NCPCompat" + src_name)
    if not is_ok_dir(src_dir):
        print("[ERROR] Source directory not found: " + src_dir)
        return False
    # Ask for replacement properties.
    # TODO: Determine by version tag of MCAccess, or a specific comment in there (if left empty)?
    dst_name = raw_input("Significant module name (after NCPCompat) excluding revision (e.g. _R1): ")
    if not dst_name.strip():
        print("[ERROR] Can't be empty.")
        return False
    dst_rev = raw_input("Revision (kept exact case for package naming, no leading '_', default is none): ")
    dst_rev = dst_rev.replace(".", "_").strip()
    dst_name_with_rev = dst_name + (("_" + dst_rev) if dst_rev else "")
    dst_dir = os.path.join(path, "NCPCompat" + dst_name_with_rev)
    if os.path.exists(dst_dir):
        print("[ERROR] Destination already exists: " + dst_dir)
        return False
    # Create target directory.
    os.mkdir(dst_dir)
    # Create replacement definitions.
    repl_filename = [(src_name, dst_name_with_rev), (src_name.lower(), dst_name.lower() + (("_" + dst_rev) if dst_rev else ""))]
    repl_content = [
        # TODO: DOESNT FUCKING WORK artifactId
        ("the development version (latest of the supported Minecraft versions)", dst_name_with_rev),
        ] + repl_filename + [
        ("<artifactId>ncpcompat" + dst_name.lower() + (("_" + dst_rev) if dst_rev else "") + "</artifactId>", "<artifactId>ncpcompat" + dst_name_with_rev.lower() + "</artifactId>"),
        ]
    # Run.
    """
    TODO: Factory entries. Adapt build profiles. 
    Perhaps just create a text file with all typical entries for copy and paste.
    """
    try:
        # TODO: May leak file descriptors :p.
        copy_and_replace(src_dir, dst_dir, repl_filename, repl_content)
        return True
    except:
        traceback.print_exc()
        sys.stderr.flush()
        print("[ERROR] Module creation failed, remove destination directory...")
        # Remove the target directory on failures.
        if os.path.exists(dst_dir):
            shutil.rmtree(dst_dir)
        return False

def main():
    """
    Parse command line parameter(s).
    """
    if len(sys.argv) > 1:
        path = "".join(sys.argv[1:])
        if path and path[0] == path[-1] == "\"":
            path = path[1:-1]
    else:
        path = os.path.split(sys.argv[0])[0]
    main_interactive(os.path.normpath(path))
    print("Finished.")

if __name__ == "__main__":
    main()
