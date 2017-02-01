# Ansible

This directory contains all [Ansible](http://docs.ansible.com/) playbooks and
files to setup, configure and deploy services automatically.

## Running Ansible playbooks

You can run Ansible playbooks by executing `ansible-playbook` command:

`ansible-playbook --private-key=~/.ssh/id_rsa -i hosts.ini --tags=setup
                  jenkins-master.yaml`

If you are not using SSH keys to login in the target host, you can execute
`ansible-playbook` command specifying the `-k` option and you will be prompted
to enter the SSH password:

`ansible-playbook -k -i hosts.ini --tags=setup
                  jenkins-master.yaml`

The `--tags` option tells Ansible to only execute tasks with the specified tag.
In the previous examples, only tasks with the `setup` tag would be executed.
