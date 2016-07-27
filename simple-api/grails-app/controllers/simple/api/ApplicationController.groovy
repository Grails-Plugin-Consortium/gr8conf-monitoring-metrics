package simple.api

import io.prometheus.client.Counter
import io.prometheus.client.Summary

class ApplicationController {

    DataService dataService

    static
    final Summary requestLatency = Summary.build().name("requests_latency_seconds").help("Request latency in seconds.").register();
    static
    final Summary requestLatencySlow = Summary.build().name("requests_slow_latency_seconds").help("Request latency in seconds.").register();
    static
    final Summary requestLatencyFast = Summary.build().name("requests_fast_latency_seconds").help("Request latency in seconds.").register();

    static final Counter requests = Counter.build()
            .name("requests_total")
            .help("Requests.").register();

    static final Counter requestsSlow = Counter.build()
            .name("requests_slow")
            .help("Requests.").register();

    static final Counter requestsFast = Counter.build()
            .name("requests_fast")
            .help("Requests.").register();

    //This will randomly fail on timeout
    def index() {
        Summary.Timer timer = requestLatency.startTimer()
        requests.inc()
        respond([name: dataService.getName()])
        timer.observeDuration()
    }

    //This will cause circuits to close
    def slow() {
        Summary.Timer timer = requestLatencySlow.startTimer()
        requests.inc()
        requestsSlow.inc()
        respond([name: dataService.getNameLongRunning()])
        timer.observeDuration()
    }

    //Seperate pool, should (ALMOST) always succeed
    def fast() {
        Summary.Timer timer = requestLatencyFast.startTimer()
        requests.inc()
        requestsFast.inc()
        respond([name: dataService.getNameFast()])
        timer.observeDuration()
    }
}
