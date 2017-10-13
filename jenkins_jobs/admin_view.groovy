listView('Admin') {
    description('Administrative jobs')
    jobs {
        name('create_credentials')
        name('create_slave_node')
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
