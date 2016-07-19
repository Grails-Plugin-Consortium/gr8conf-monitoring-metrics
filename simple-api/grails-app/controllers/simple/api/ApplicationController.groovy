package simple.api

class ApplicationController {

    DataService dataService

    //This will randomly fail on timeout
    def index() {
        [name: dataService.getName()]
    }

    //This will cause circuits to close
    def slow() {
        [name: dataService.getNameLongRunning()]
    }

    //Seperate pool, should (ALMOST) always succeed
    def fast() {
        [name: dataService.getNameFast()]
    }
}
