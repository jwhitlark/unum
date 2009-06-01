#! /usr/bin/env python

import gzip
import subprocess
import sys

def get_installed():
    """Return a list of all installed packages.
    """
    p = subprocess.Popen(['dpkg', '--get-selections'], stdout=subprocess.PIPE)
    p.wait()
    full_installed = p.stdout.readlines()
    installed = [ x.split()[0] for x in full_installed]
    return installed

def get_dependencies(target):
    """Return a list of dependencies for target.
    """
    p = subprocess.Popen(['apt-cache', 'depends', target], stdout=subprocess.PIPE)
    p.wait()
    full_depends = [x.strip() for x in p.stdout.readlines()]
    depends = [x.split('Depends:')[1].strip() for x in full_depends if 'Depends:' in x]
    return depends

def get_description(target):
    """Return a a tuple consisting of the section and a one line description of the specified package.

    TODO: should be split, and memoized against a cache of targets?
    TODO: how to get when it was installed?  Read & parse /var/log/apt/* for this info.
    """
    p = subprocess.Popen(['apt-cache', 'show', target], stdout=subprocess.PIPE)
    p.wait()
    full_description = [x.strip() for x in p.stdout.readlines()]
    desc = [x.split('Description:')[1].strip() for x in full_description if x.startswith('Description:')][0]
    section = [x.split('Section:')[1].strip() for x in full_description if x.startswith('Section:')][0]
    return (section, desc)

def get_leaves():
    """TODO: multithread this
    """
    installed = set(get_installed())
    base_installed = set(get_base_installed())
    installed_non_base = installed.difference(base_installed)
    all_deps = set()
    for pkg in installed_non_base:
        pkg_deps = get_dependencies(pkg)
        all_deps = all_deps.union(set(pkg_deps))

    needed = installed_non_base.difference(all_deps)
    return needed

def get_base_installed():
    """ TODO: Not sure if I'm getting quite what I want, look at the format again.
    """
    base_full = gzip.open('/var/log/installer/initial-status.gz').readlines()
    # Perhaps get provides?  prob not...
    base_pkgs = [x.split(':')[1].strip() for x in base_full if x.startswith('Package:')]
    return base_pkgs

def write_mod_file(filename):
    """
    TODO: add timestamp to top of file, commented, for comparison
    """
    needed = get_leaves()
    output_lines = ['%s: %s\n' % (x, get_description(x)) for x in needed]
    output_lines.sort()
    fh = open(filename, 'w')
    fh.writelines(output_lines)
    fh.close()

def install_mod_file(filename):
    """Needs to be run as root.  Assumes yes for installing packages

    TODO: replace with --set-selections, and separate trigger.
    TODO: Handle timestamp at top of file.
    """
    pkgs = [x.split(':')[0] for x in open(filename).readlines()]
    args = ['apt-get', 'install', '--yes']
    args.extend(pkgs)
    p = subprocess.Popen(args, stdout=subprocess.PIPE)
    p.wait()
    return p.stdout.readlines()


################################################################################

import unittest

class Test_Get_Installed(unittest.TestCase):
    def test_get_installed(self):
        canned = [x.strip() for x in open('test_data.txt').readlines()]
        actual = get_installed()
        self.assertEquals(canned, actual)

    def test_get_dependencies(self):
        canned = [x.strip() for x in open('test_dependencies.txt').readlines()]
        actual = get_dependencies('vim-gnome')
        self.assertEquals(canned, actual)


if __name__ == '__main__':
    # TODO add error checking, etc.
    if sys.argv[1] == 'test':
        unittest.main()
    if sys.argv[1] == 'install':
        print install_mod_file(sys.argv[2])
    if sys.argv[1] == 'get':
        print write_mod_file(sys.argv[2])


#### notes
## dpkg
# --set-selections
#    Set package selections using file read from stdin. This file should be  in  the  format  <package>  <state>,
#    where  state is one of install, hold, deinstall or purge. Blank lines and comment lines beginning with # are
#    also permitted.
