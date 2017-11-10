# OpenPOWER Host OS infrastructure as code

This git repository keeps automated scripts to set up an infrastructure capable
of building OpenPOWER Host OS artifacts, i.e. packages and ISO image.

## Infrastructure description

The scripts assume you have an organization/user on GitHub with forks of
four of the OpenPOWER Host OS git repositories:
- infrastructure :: this repository
- builds :: build scripts
- versions :: packages metadata (source URL, version, etc) for creating builds
- open-power-host-os.github.io :: web page with build summary/release notes

They will prepare a Jenkins instance capable of:
- manually start a build
- automatically start a build when there are pull requests to the "builds" and
"versions" git repositories in GitHub
- create nightly builds, which must have some extra artifacts

The Jenkins instance is composed of at least two nodes: a master one which
centralizes administrative tasks, builds queuing, build execution status and
build results, and a slave one which executes the build itself and validates
build system source code. You may install both nodes in a single system or
even install only the slave node and use it to build via command line.

### GitHub integration

The Jenkins instance polls GitHub for status changes on the corresponding
repositories' pull requests. It will start a batch of tests whenever an
authorized user opens or updates a pull request containing a
[Jenkinsfile](https://jenkins.io/doc/book/pipeline/jenkinsfile/) in the
"infrastructure", "builds" or "versions" repositories. Currently, tests are:
building all packages, building ISO, Pylint validation of Python code, yamllint
validation of all package metadata YAML files and rpmlint validation of all RPM
packages specification files.

### Periodic builds process

A Jenkins job will trigger periodically to check the versions of the packages
available in OpenPOWER Host OS GitHub organization, update the corresponding
packages' versions in the "versions" git repository, create a build with all
packages available and create release notes, by executing the corresponding
commands from the build scripts in the "builds" git repository. Commits will
be created for the local "versions" and "open-power-host-os.github.io" git
repositories and pushed to the designated GitHub user's git repository.
Ideally, this should be a "bot" user. The process will then hang, waiting for
an administrator to check the commits created and confirm the push to the
organization's repositories. Those commits will also be tagged and the
resulting build artifacts will be made available in the upload server.
The default is to execute those periodic builds once a week (every Wednesday
at 11 AM, Jenkins master's timezone).

There's another periodic job similar to the one above, which differs by not
pushing commits to remote git repositories. It allows developers of the
packages to have their new commits tested and to have a build with those
commits as soon as possible. The default is to execute those periodic builds
nightly (daily at 8 AM UTC, Jenkins master's timezone).

## Infrastructure setup

If the whole infrastructure setup is desired, you should do the following steps:
- Prepare CentOS system(s)
- Setup SSH credentials used to access upload server and GitHub from Jenkins slaves
- Automatically setup Jenkins master and slave(s) using Ansible playbooks
- Manually setup Jenkins via web UI
 - Create administrative jobs
 - Setup SSH credentials used to access Jenkins slaves from Jenkins master
 - Create credentials used to access GitHub API from Jenkins
 - Create Jenkins slaves in Jenkins web UI
 - Create builds jobs

They are described in detail below.

### Prepare CentOS system(s)

First, you will need to prepare at least one CentOS system. If the Jenkins
instance will be used for testing, one system is enough; production instances
should use more systems. Virtual machines are recommended, but you could
optionally use bare-metal or containers instead. 

If you do not have a direct network connection to the system(s), you may
setup an SSH proxy in ~/.ssh/config in your workstation to make the SSH
proxy transparent. Example:

Host open-power-hostos-builds
    HostName <target_server>
    ProxyCommand ssh <myuser>@<proxy_server> nc <target_server> 22

After the SSH proxy is configured, simply use the following to connect to the system:
`ssh <myuser>@open-power-hostos-builds`

### Setup SSH credentials used to access upload server and GitHub from Jenkins slaves

To automate SSH authentication between 2 hosts, it is necessary to:
 - create an SSH key pair if you still do not have one
 - add the SSH private key to ~/.ssh/ in the source host
 - add the SSH public key to ~/.ssh/authorized_keys file in the target host

To be able to upload the results of builds to a server and push commits to
GitHub automatically, you must automate SSH authentication from Jenkins slaves
to those two servers. To have this being done automatically at the setup, make sure you have 
both upload server and GitHub host keys stored in a local `~/.ssh/known_hosts` file. For 
production servers, it is recommended that you provide a separate file with only 
those two keys.
The private SSH key is already installed in Jenkins nodes
by Jenkins Ansible playbooks, so you do not need to do it manually.

### Automatically setup Jenkins master and slave(s) using Ansible playbooks

There are three Ansible playbooks: one that sets up a Jenkins master node,
and two that set up Jenkins slave nodes, either for building or executing
Build Verification Tests. Read the
[Ansible instructions](ansible/README.md) for details on how to execute
the playbooks.

If you wish to have a single system hosting the entire Jenkins instance, the
playbooks can be executed in the same system. Execute first the Jenkins master
playbook, stop Jenkins service (systemctl stop jenkins) and then execute the
other playbooks.

Note: The Jenkins playbooks may fail due to network errors. If you see HTTP
request errors, try executing them again.

#### Jenkins LDAP authentication

There's an optional Ansible role that configures Jenkins authentication through
an LDAP server. You'll first need to enable the `configure_ldap` variable in
the [Jenkins master variables file](ansible/vars-master.yaml) and set the
variables in the
[LDAP configuration file](ansible/roles/jenkins-ldap/defaults/main.yaml),
making sure there's at least one admin user so you are able to access and
customize Jenkins using the UI. Execute the master playbook as usual and
answer with `Y` when prompted if you'd like to configure LDAP.

### Manually setup Jenkins via web UI

If you do not have a direct network connection to the system(s), you may
create an SSH tunnel in your workstation to be able to access the Jenkins
web UI, which is accessible via HTTPS. Example:

`sudo ssh -o "ProxyCommand ssh <user>@<proxy_server> -W %h:%p" -NL 443:localhost:443 root@<jenkins_master>`

<proxy_server> is the hostname or IP address of the server which will be the
intermediate (proxy) between you and the Jenkins server. While this connection
is alive, you can access then Jenkins web UI at https://localhost using a web
browser.

#### Create credentials in Jenkins

The Credentials plugin allows you to store and manage credentials in Jenkins.
There are two credentials required to run the build scripts: a pair of user and
SSH key to access Jenkins slaves from the master node; and a pair of user and
token to access GitHub.

The SSH key pair for master-slaves access must have been created beforehand. It
is recommended that this key pair be used exclusively for this purpose.

The Jenkins pipeline GitHub API needs read access to check out pull
requests for building and validating and write access to update the pull
requests statuses, informing the developers of the job results.

To create an API token, go to
[Create a token](https://github.com/settings/tokens/new), select
"repo" scope and press the "Generate token" button. Refer to
https://github.com/blog/1509-personal-api-tokens for more information on
how to create those tokens.

To create the credentials, you can execute the job at
`https://<jenkins_server>/job/create_credentials`, filling the necessary
parameters.

#### Create slaves in Jenkins web UI

The default behavior is for administrative Jenkins jobs to execute only on the
master node, and remaining jobs to execute only on slave nodes labeled
"builds_slave_label" or "validation_slave_label". You will then need to add at
least one slave node to execute the non-administrative jobs. To do this, you can execute
the job at `https://<jenkins-server>/job/create_slave_node`. Set the IP address or hostname
of the slave in IP_ADDRESS job parameter. The other job parameters values do not
need to be modified. You should have already executed the Jenkins slave playbook(s)
on those slaves.

##### Create BVT slave in Jenkins web UI

The Build Verification Tests (BVT) use [Avocado](https://avocado-framework.github.io/)
to execute tests on virtual machines. The tests require passwordless access to
sudo, so it is recommended that a separate machine is used forthat purpose. It
may be a virtual machine, the tests will then run in a nested virtualized
guest.

To create the slave node from the Jenkins UI, follow the same instructions
above, setting the IP_ADDRESS and, additionally, changing the NODE_LABEL to
"bvt_slave_label".

#### Create builds jobs

When the credentials are configured, execute the seed job at
`https://<jenkins_server>/job/seed_job` with the default
"JOB_DESCRIPTORS_FILES" parameter. This will create all the build jobs
configured to check out from the GitHub organization/user repositories.
