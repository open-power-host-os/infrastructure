# Ansible

This directory contains all [Ansible](http://docs.ansible.com/) playbooks and
files required to setup services automatically.

## Installing Ansible

If you still do not have Ansible installed in your local system, setup EPEL yum
repository and run:

### In CentOS:
`yum install -y ansible`

### In Ubuntu:
`pip install ansible`

## Running Ansible playbooks

First, in your source host, update all the hosts in the hosts.ini file, paying attention to the groups they belong to.
When executing a playbook, only the hosts in the corresponding group will be updated (eg if you
execute the jenkins-master playbook, a Jenkins master instance will be installed to all
hosts in [jenkins-master] section in hosts.ini).

Then, update the variables values in vars.yaml. If you are creating a production
Jenkins server, provide the production GitHub organization in github_organization_name
and ghprb_admin_organization variables and leave ghprb_admin_user empty. If you
are creating a test Jenkins server, provide your user name in github_organization_name
and ghprb_admin_user variables and leave ghprb_admin_organization empty.

Finally, you can run Ansible playbooks by executing `ansible-playbook` command.
If you are using SSH keys:

`ansible-playbook --private-key=~/.ssh/id_rsa -i hosts.ini --tags=setup
                  jenkins-master.yaml`

If you are not using SSH keys to login in the target host, you can execute
`ansible-playbook` command specifying the `-k` option and you will be prompted
to enter the SSH password:

`ansible-playbook -k -i hosts.ini --tags=setup
                  jenkins-master.yaml`

The `--tags` option tells Ansible to only execute tasks with the specified tag.
In the previous examples, only tasks with the `setup` tag would be executed.

### List of manual input data

Some Ansible playbooks will prompt you to input data to setup Jenkins master and slave nodes.

- `jenkins-master.yaml` playbook

	- Username and password to access Jenkins web UI with administrative privileges. Note that
a passwordless `jenkins` user will also be created in master and slave nodes.
	- Paths to SSH private and public keys used to communicate between master and slave nodes.

- `jenkins-slave.yaml` playbook

	- Paths to SSH private and public keys used to communicate between master and slave nodes.
	- Path to the SSH private key used to upload 
artifacts to the configured remote server.
	- Path to the SSH private key used to push commits to GitHub.
	- Path to the SSH known keys for remote hosts, usually `~/.ssh/known_hosts`. 
The local file is just copied to remote target.

- `bvt-host.yaml` playbook

	- Paths to SSH private and public keys used to communicate between master
    and slave nodes.
	- Path to the SSH private key used to upload artifacts to the configured
    remote server.
	- Path to the SSH known keys for remote hosts, usually
    `~/.ssh/known_hosts`. The local file is just copied to remote target.

Provide the data requested by the playbooks (e.g. Jenkins admin user name/password
and SSH keys locations) and wait for automatic setup to finish.

## Baremetal provisioning via Ansible

The `bm-deploy.yaml` playbook allows the provisioning of a POWER
(baremetal) machine using PXE. A "controller node" that can serve DHCP
to the machine being provisioned is required.

The deployment consists of two steps:

### Preparation of the controller node

This is a one time operation needed to prepare the machine that will
be used as controller for the provisioning. This machine should be in
the same network as the to-be provisioned (target) machine (this is
due to the requirement of serving DHCP to the target machine). Using a
virtual machine is OK.

The controller node is referred to in the `hosts.ini` file as
`[baremetal-ctrl]`.

Run once for each controller setup:
 ```
 ansible-playbook -i hosts.ini --tags=setup bm-deploy.yaml
 ```

After that, the controller node will be capable of serving files to
the target machine.

For detailed info, see the [baremetal-ctrl](roles/baremetal-ctrl) role.

### Deployment

This step is executed each time a machine needs to be provisioned. It
uses IPMI to power the machine on/off, DHCP for setting up PXE and
HTTP for serving files. Services on the controller node are started on
demand.

Prerequisites:

 - The variables file vars-baremetal.yaml should be edited with
information about the baremetal machine prior to execution of the
"deploy" tag.

 - A .iso file named after the MAC address of the network interface of
the target machine (<mac_address>_deploy.iso) is expected to be at
/tmp.

 ```
 cp <iso_file> /tmp/<ma:ca:dd:re:ss>_deploy.iso
 ```

Run every time a deploy is required:
 ```
 ansible-playbook -i hosts.ini --tags=deploy bm-deploy.yaml
 ```

#### Deployment flow

The flow of the deploy after the user runs the playbook with the
"deploy" tag (C - controller node, B - baremetal node) is:

- C: Mount ISO file inside the HTTP server directory
- C: Using IPMI, set machine to boot via network
- C: Using IPMI, turn the baremetal machine on
- C: Serve DHCP along with PXE configuration file location


- B: Boot and download PXE configuration file from HTTP server containing kickstart location and boot params
- B: Install via kickstart
- B: Run post script that adds authorized SSH keys and starts SSH server
- C: Detect that installation has finished by SSH server presence
- C: Fetch installed filesystem UUID
- C: Copy kernel/initramfs to the controller node
- C: Reconfigure PXE to boot the new installation


- C: Using IPMI, turn the machine off
- C: Using IPMI, turn the back machine on

Deployment is finished and machine is accessible via SSH

For detailed info, see the [deploy-baremetal](roles/deploy-baremetal) role.
