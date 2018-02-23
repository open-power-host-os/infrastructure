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

Provide the data requested by the playbooks (e.g. Jenkins admin user name/password
and SSH keys locations) and wait for automatic setup to finish.
