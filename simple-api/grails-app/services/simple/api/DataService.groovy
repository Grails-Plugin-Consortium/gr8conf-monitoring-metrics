package simple.api

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty

class DataService {

    static transactional = false

    @HystrixCommand(fallbackMethod = "getNameFallback",
            commandProperties = [@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "100")], threadPoolProperties = [
                    @HystrixProperty(name = "coreSize", value = "5"), @HystrixProperty(name = "maxQueueSize", value = "10"),
                    @HystrixProperty(name = "queueSizeRejectionThreshold", value = "10")], threadPoolKey = "DataService")
    String getName() {
        Thread.sleep((long)(Math.random() * 150));
        return UUID.randomUUID().toString()
    }

    @HystrixCommand(fallbackMethod = "getNameFallback",
            commandProperties = [@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "100")], threadPoolProperties = [
                    @HystrixProperty(name = "coreSize", value = "5"), @HystrixProperty(name = "maxQueueSize", value = "10"),
                    @HystrixProperty(name = "queueSizeRejectionThreshold", value = "10")], threadPoolKey = "DataService")
    String getNameLongRunning(){
        Thread.sleep((long)(Math.random() * 350));
        return 'SLOW ' + UUID.randomUUID().toString()
    }

    @HystrixCommand(fallbackMethod = "getNameFallback",
            commandProperties = [@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "100")], threadPoolProperties = [
                    @HystrixProperty(name = "coreSize", value = "5"), @HystrixProperty(name = "maxQueueSize", value = "10"),
                    @HystrixProperty(name = "queueSizeRejectionThreshold", value = "10")], threadPoolKey = "DataServiceSecondPool")
    String getNameFast(){
        return 'FAST ' + UUID.randomUUID().toString()
    }

    String getNameFallback() {
        return "FALLBACK NAME"
    }
}
