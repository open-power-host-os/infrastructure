#!/bin/env python2

import argparse
import logging
import shlex
import shutil
import subprocess

AVOCADO_RUN_COMMAND = (
    "/usr/bin/avocado run --vt-type {vt_type} --vt-guest-os {vt_guest_os} "
    "--failfast on {tests}")
# TODO: Parameterize those values
VT_TYPE = "libvirt"
VT_GUEST_OS = "CentOS.7.4"

IMPORT_GUEST_TEST = ("io-github-autotest-qemu.unattended_install.import.import"
                     ".default_install.aio_native")
REMOVE_GUEST_TEST = "io-github-autotest-libvirt.remove_guest.without_disk"
INSTALL_BASE_OS_TEST = ("io-github-autotest-qemu.unattended_install.url.http_ks"
                        ".default_install.aio_native")
UPDATE_BASE_OS_TEST = "io-github-autotest-qemu.yum_update"
INSTALL_HOST_OS_TEST = "host-os-bvt.yum_install"
UPDATE_HOST_OS_TEST = "host-os-bvt.yum_update"

# TODO: Parameterize those values
DISK_PATH = "/root/avocado/data/avocado-vt/images/centos74-ppc64le.qcow2"
DISK_BACKUP_PATH = DISK_PATH + ".backup"

LOGGING_FORMAT = "%(message)s"
logging.basicConfig(level=logging.DEBUG, format=LOGGING_FORMAT)


def parse_cli_options():
    """
    Parse CLI options

    Returns:
        Namespace: CLI options. Valid attributes: previous_yum_config_file,
            current_yum_config_file.
    """

    parser = argparse.ArgumentParser()
    parser.add_argument("--current-yum-config-file",
                        help="yum configuration file pointing to the current "
                        "Host OS repository",
                        default="host_os.repo")
    parser.add_argument("--previous-yum-config-file",
                        help="yum configuration file pointing to a previous "
                        "Host OS repository",
                        default=None)
    args = parser.parse_args()
    return args


def get_vt_extra_params(parameter_values):
    """
    Get a string to be appended to the 'avocado run' command setting
    extra parameters for avocado-vt.

    Args:
        parameter_values (dict): dictionary mapping parameter names to
            their values
    """
    vt_extra_params = " --vt-extra-params"
    for name, value in parameter_values.items():
        vt_extra_params += " {name}={value}".format(**locals())
    return vt_extra_params


def execute_avocado_command(tests, vt_extra_parameter_values=None):
    """
    Execute the tests using the Avocado framework.

    Args:
        tests ([str]): name of the tests to be executed
        vt_extra_parameter_values (dict): dictionary mapping avocado-vt
            parameter names to their values
    """
    cmd = AVOCADO_RUN_COMMAND.format(
        vt_type=VT_TYPE, vt_guest_os=VT_GUEST_OS, tests=" ".join(tests))
    if vt_extra_parameter_values:
        cmd += get_vt_extra_params(vt_extra_parameter_values)

    logging.debug("Executing: " + cmd)
    subprocess.check_call(shlex.split(cmd), stderr=subprocess.STDOUT)


def update_guest():
    """
    Create a libvirt domain from an existing disk image and updates
    the system using yum.
    """
    logging.info("Updating an existing guest")
    tests = [IMPORT_GUEST_TEST, UPDATE_BASE_OS_TEST]
    extra_params = dict(
        restore_image_after_testing=False,
        yum_update_timeout=1800)
    execute_avocado_command(tests, extra_params)


def install_guest():
    """
    Create a libvirt domain, installs the OS in an empty disk image and
    updates the system using yum.
    """
    logging.info("Installing the OS and updating a new guest")
    tests = [INSTALL_BASE_OS_TEST, UPDATE_BASE_OS_TEST]
    extra_params = dict(
        restore_image_after_testing=False,
        install_timeout=9000,
        yum_update_timeout=1800)
    execute_avocado_command(tests, extra_params)


def install_host_os(yum_config_file_path):
    """
    Execute the test that installs the Host OS packages on top of a
    working system.

    Args:
        yum_config_file_path (str): path to a yum configuration file
            pointing to a repository containing Host OS packages
    """
    logging.info("Installing Host OS packages")
    tests = [INSTALL_HOST_OS_TEST]
    extra_params = dict(
        restore_image_after_testing=False,
        yum_install_timeout=1800,
        yum_config_file_path=yum_config_file_path)
    execute_avocado_command(tests, extra_params)


def update_host_os(yum_install_config_file_path, yum_update_config_file_path):
    """
    Execute the test that installs the Host OS packages on top of a
    working system and updates them to more recent version.

    Args:
        yum_install_config_file_path (str): path to a yum configuration
            file pointing to a repository containing outdated Host OS
            packages
        yum_update_config_file_path (str): path to a yum configuration
            file pointing to a repository containing updated Host OS
            packages
    """
    logging.info("Updating a system with Host OS")
    tests = [UPDATE_HOST_OS_TEST]
    extra_params = dict(
        restore_image_after_testing=False,
        yum_install_timeout=1800,
        yum_install_config_file_path=yum_install_config_file_path,
        yum_update_timeout=1200,
        yum_update_config_file_path=yum_update_config_file_path)
    execute_avocado_command(tests, extra_params)


def remove_guest():
    """
    Remove a libvirt domain, leaving its disk image as is.
    """
    logging.info("Removing the guest")
    tests = [REMOVE_GUEST_TEST]
    execute_avocado_command(tests)


def backup_disk_image():
    """
    Back up the disk image to a predefined path expected by avocado-vt.
    """
    logging.info("Backing up disk image")
    logging.debug("Copying from '{source}' to '{dest}'".format(
        source=DISK_PATH, dest=DISK_BACKUP_PATH))
    shutil.copy(DISK_PATH, DISK_BACKUP_PATH)


def restore_disk_image():
    """
    Restore a previously backed up disk image.
    """
    logging.info("Restoring disk backup")
    logging.debug("Copying from '{source}' to '{dest}'".format(
        source=DISK_BACKUP_PATH, dest=DISK_PATH))
    shutil.copy(DISK_BACKUP_PATH, DISK_PATH)


def execute_bvt(current_yum_config_file, previous_yum_config_file=None):
    """
    Prepare a guest with the base OS and execute the Host OS install and
    update tests on top of it.

    Args:
        current_yum_config_file (str): path to a yum configuration
            file pointing to a repository containing updated Host OS
            packages
        previous_yum_config_file (str): path to a yum configuration
            file pointing to a repository containing outdated Host OS
            packages
    """
    try:
        try:
            update_guest()
        except subprocess.CalledProcessError as error:
            logging.warning(error)
            try:
                remove_guest()
            except subprocess.CalledProcessError as remove_error:
                logging.debug(remove_error)
            install_guest()
        backup_disk_image()
        install_host_os(current_yum_config_file)
        restore_disk_image()
        if previous_yum_config_file:
            update_host_os(previous_yum_config_file, current_yum_config_file)
            restore_disk_image()
        else:
            logging.info(
                "No previous repository provided, skipping update test")
    except:
        raise
    finally:
        remove_guest()


if __name__ == '__main__':
    args = parse_cli_options()
    execute_bvt(args.current_yum_config_file, args.previous_yum_config_file)
