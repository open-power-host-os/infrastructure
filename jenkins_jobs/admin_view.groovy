listView('Admin') {
    description('Administrative jobs')
    jobs {
        name('create_credentials')
        name('create_node')
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
