listView('Pipelines') {
    description('Host OS builds pipelines')
    jobs {
        name('infrastructure')
        name('builds')
        name('versions')
        name('devel')
        name('release')
    }
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}
