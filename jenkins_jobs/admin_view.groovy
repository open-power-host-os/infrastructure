listView('Admin') {
    description('Administrative jobs')
    jobs {
        name('create_node')
        name('create_ssh_credentials')
        name('create_user_pass_credentials')
        name('seed_job')
    }
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        buildButton()
    }
}
